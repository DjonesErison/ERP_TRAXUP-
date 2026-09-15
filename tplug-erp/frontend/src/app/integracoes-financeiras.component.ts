import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import { forkJoin } from 'rxjs';
import {
  ConciliacaoFinanceiraService,
  IntegracaoFinanceiraPainel,
  IntegracaoFinanceiraResumo
} from './conciliacao-financeira.service';

@Component({
  selector: 'app-integracoes-financeiras',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="integrations card" *ngIf="contaId">
      <header class="integrations-head">
        <div>
          <p class="eyebrow">Bancos, adquirentes e PSPs</p>
          <h2>Saúde das integrações</h2>
          <p>Monitoramento operacional sem exposição de credenciais ou checkpoints.</p>
        </div>
        <button class="secondary" (click)="carregar()" [disabled]="loading">
          {{ loading ? 'Atualizando...' : 'Atualizar integrações' }}
        </button>
      </header>

      <div class="alert" *ngIf="error">{{ error }}</div>
      <div class="feedback" *ngIf="feedback">{{ feedback }}</div>

      <section class="integration-metrics" *ngIf="resumo as dados">
        <article><span>Total</span><strong>{{ dados.total }}</strong></article>
        <article class="healthy"><span>Saudáveis</span><strong>{{ dados.saudaveis }}</strong></article>
        <article class="attention"><span>Atenção</span><strong>{{ dados.atencao }}</strong></article>
        <article class="idle"><span>Sem execução</span><strong>{{ dados.semExecucao }}</strong></article>
      </section>

      <div class="integration-list">
        <article *ngFor="let item of itens; trackBy: trackIntegracao">
          <div class="provider">
            <span class="provider-mark">{{ iniciais(item.integracao.provedor) }}</span>
            <div>
              <strong>{{ item.integracao.provedor }}</strong>
              <small>{{ item.integracao.identificadorExterno }}</small>
            </div>
          </div>
          <div class="health">
            <span class="health-badge" [ngClass]="classeSaude(item.saude.status)">
              {{ nomeSaude(item.saude.status) }}
            </span>
            <small>{{ detalheExecucao(item) }}</small>
          </div>
          <div class="health-data">
            <span>Último sucesso<strong>{{ item.saude.ultimoSucessoEm ? (item.saude.ultimoSucessoEm | date:'dd/MM/yyyy HH:mm') : '—' }}</strong></span>
            <span>Falhas seguidas<strong>{{ item.saude.falhasConsecutivas }}</strong></span>
            <span>Duração média<strong>{{ item.saude.duracaoMediaMs == null ? '—' : item.saude.duracaoMediaMs + ' ms' }}</strong></span>
          </div>
          <button
            (click)="sincronizar(item)"
            [disabled]="!item.integracao.ativo || sincronizandoId === item.integracao.id">
            {{ sincronizandoId === item.integracao.id ? 'Sincronizando...' : 'Sincronizar agora' }}
          </button>
        </article>

        <div class="empty" *ngIf="!loading && itens.length === 0">
          Nenhuma integração está configurada para esta conta.
        </div>
      </div>
    </section>
  `,
  styleUrl: './integracoes-financeiras.component.css'
})
export class IntegracoesFinanceirasComponent implements OnChanges {
  @Input() contaId = '';
  @Output() sincronizada = new EventEmitter<void>();

  itens: IntegracaoFinanceiraPainel[] = [];
  resumo?: IntegracaoFinanceiraResumo;
  loading = false;
  sincronizandoId = '';
  error = '';
  feedback = '';

  constructor(private readonly service: ConciliacaoFinanceiraService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['contaId']) {
      this.itens = [];
      this.resumo = undefined;
      this.feedback = '';
      if (this.contaId) this.carregar();
    }
  }

  carregar(limparFeedback = true): void {
    if (!this.contaId || this.loading) return;
    this.loading = true;
    this.error = '';
    if (limparFeedback) this.feedback = '';
    forkJoin({
      itens: this.service.consultarPainelIntegracoes(this.contaId),
      resumo: this.service.consultarResumoIntegracoes(this.contaId)
    }).subscribe({
      next: (dados) => {
        this.itens = dados.itens;
        this.resumo = dados.resumo;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso às integrações financeiras.'
          : 'Não foi possível carregar a saúde das integrações.';
      }
    });
  }

  sincronizar(item: IntegracaoFinanceiraPainel): void {
    if (!item.integracao.ativo || this.sincronizandoId) return;
    this.sincronizandoId = item.integracao.id;
    this.error = '';
    this.feedback = '';
    this.service.sincronizarIntegracao(item.integracao.id).subscribe({
      next: (resultado) => {
        this.sincronizandoId = '';
        this.feedback = resultado.lancamentos.length
          + ' lançamento(s) recebidos do provedor.';
        this.carregar(false);
        this.sincronizada.emit();
      },
      error: (err) => {
        this.sincronizandoId = '';
        this.carregar(false);
        this.error = err?.status === 403
          ? 'Seu perfil não possui permissão para sincronizar.'
          : 'O adaptador não está disponível ou a sincronização falhou.';
      }
    });
  }

  nomeSaude(status: string): string {
    if (status === 'SAUDAVEL') return 'Saudável';
    if (status === 'ATENCAO') return 'Atenção';
    return 'Sem execução';
  }

  classeSaude(status: string): string {
    if (status === 'SAUDAVEL') return 'healthy';
    if (status === 'ATENCAO') return 'attention';
    return 'idle';
  }

  detalheExecucao(item: IntegracaoFinanceiraPainel): string {
    if (!item.integracao.ativo) return 'Integração desativada';
    if (!item.saude.ultimaTentativaEm) return 'Ainda não executada';
    return 'Última tentativa em '
      + new Date(item.saude.ultimaTentativaEm).toLocaleString('pt-BR');
  }

  iniciais(provedor: string): string {
    return provedor.trim().slice(0, 2).toUpperCase();
  }

  trackIntegracao(_: number, item: IntegracaoFinanceiraPainel): string {
    return item.integracao.id;
  }
}
