import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  ConciliacaoFinanceiraService,
  IntegracaoFinanceiraPainel,
  IntegracaoFinanceiraResumo,
  IntegracaoFinanceiraTentativa
} from './conciliacao-financeira.service';

@Component({
  selector: 'app-integracoes-financeiras',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="integrations card" *ngIf="contaId">
      <header class="integrations-head">
        <div>
          <p class="eyebrow">Bancos, adquirentes e PSPs</p>
          <h2>Saúde das integrações</h2>
          <p>Monitoramento operacional sem exposição de credenciais ou checkpoints.</p>
        </div>
        <div class="header-actions">
          <button class="secondary" (click)="mostrarCadastro = !mostrarCadastro">
            {{ mostrarCadastro ? 'Cancelar cadastro' : 'Nova integração' }}
          </button>
          <button class="secondary" (click)="carregar()" [disabled]="loading">
            {{ loading ? 'Atualizando...' : 'Atualizar integrações' }}
          </button>
        </div>
      </header>

      <form class="integration-form" *ngIf="mostrarCadastro" (ngSubmit)="criarIntegracao()">
        <div>
          <strong>Adicionar conexão operacional</strong>
          <small>Informe somente o provedor e o identificador público da conta. Não cole tokens, senhas ou chaves.</small>
        </div>
        <label>
          Provedor
          <input name="provedor" [(ngModel)]="novoProvedor" maxlength="40" required placeholder="Ex.: CIELO, REDE, BANCO_X">
        </label>
        <label>
          Identificador externo
          <input name="identificador" [(ngModel)]="novoIdentificador" maxlength="120" placeholder="Código público da conta">
        </label>
        <button type="submit" [disabled]="criando || !novoProvedor.trim()">
          {{ criando ? 'Adicionando...' : 'Adicionar integração' }}
        </button>
      </form>

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
          <div class="row-actions">
            <button class="secondary" (click)="abrirHistorico(item)">
              Histórico
            </button>
            <button
              (click)="sincronizar(item)"
              [disabled]="!item.integracao.ativo || sincronizandoId === item.integracao.id">
              {{ sincronizandoId === item.integracao.id ? 'Sincronizando...' : 'Sincronizar' }}
            </button>
            <button
              class="danger-button"
              *ngIf="item.integracao.ativo"
              (click)="desativar(item)"
              [disabled]="desativandoId === item.integracao.id">
              {{ desativandoId === item.integracao.id ? 'Desativando...' : 'Desativar' }}
            </button>
          </div>
        </article>

        <div class="empty" *ngIf="!loading && itens.length === 0">
          Nenhuma integração está configurada para esta conta.
        </div>
      </div>

      <section class="attempts" *ngIf="historicoIntegracao as integracao">
        <div class="attempts-head">
          <div>
            <p class="eyebrow">Auditoria operacional</p>
            <h3>Tentativas de {{ integracao.integracao.provedor }}</h3>
          </div>
          <div class="attempt-filters">
            <select [(ngModel)]="historicoStatus" (change)="carregarHistorico()">
              <option value="">Todas</option><option value="SUCESSO">Sucesso</option><option value="FALHA">Falha</option>
            </select>
            <input type="date" [(ngModel)]="historicoInicio" (change)="carregarHistorico()">
            <input type="date" [(ngModel)]="historicoFim" (change)="carregarHistorico()">
            <button class="secondary" (click)="fecharHistorico()">Fechar</button>
          </div>
        </div>
        <div class="attempt-list">
          <article *ngFor="let tentativa of tentativas; trackBy: trackTentativa">
            <span class="attempt-status" [class.failure]="tentativa.status === 'FALHA'">
              {{ tentativa.status === 'SUCESSO' ? 'Sucesso' : 'Falha' }}
            </span>
            <div>
              <strong>{{ tentativa.quantidadeLancamentos }} lançamento(s)</strong>
              <small>{{ tentativa.iniciadoEm | date:'dd/MM/yyyy HH:mm:ss' }} · {{ tentativa.duracaoMs }} ms</small>
            </div>
            <code *ngIf="tentativa.erroCodigo">{{ tentativa.erroCodigo }}</code>
          </article>
          <div class="empty" *ngIf="!loadingHistorico && tentativas.length === 0">Nenhuma tentativa encontrada.</div>
          <div class="empty" *ngIf="loadingHistorico">Carregando até 50 tentativas...</div>
        </div>
      </section>
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
  desativandoId = '';
  criando = false;
  mostrarCadastro = false;
  novoProvedor = '';
  novoIdentificador = '';
  historicoIntegracao?: IntegracaoFinanceiraPainel;
  tentativas: IntegracaoFinanceiraTentativa[] = [];
  historicoStatus = '';
  historicoInicio = '';
  historicoFim = '';
  loadingHistorico = false;
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

  criarIntegracao(): void {
    const provedor = this.novoProvedor.trim();
    if (!this.contaId || !provedor || this.criando) return;
    this.criando = true;
    this.error = '';
    this.feedback = '';
    this.service.criarIntegracao(
      this.contaId,
      provedor,
      this.novoIdentificador
    ).subscribe({
      next: () => {
        this.criando = false;
        this.mostrarCadastro = false;
        this.novoProvedor = '';
        this.novoIdentificador = '';
        this.feedback = 'Integração adicionada com sucesso.';
        this.carregar(false);
      },
      error: (err) => {
        this.criando = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui permissão para adicionar integrações.'
          : err?.status === 409
            ? 'Este provedor já está configurado ou a conta está inativa.'
            : 'Não foi possível adicionar a integração.';
      }
    });
  }

  desativar(item: IntegracaoFinanceiraPainel): void {
    if (!item.integracao.ativo || this.desativandoId) return;
    if (!window.confirm(
      'Desativar a integração ' + item.integracao.provedor
      + '? Novas sincronizações serão bloqueadas.'
    )) return;
    this.desativandoId = item.integracao.id;
    this.error = '';
    this.feedback = '';
    this.service.desativarIntegracao(item.integracao.id).subscribe({
      next: () => {
        this.desativandoId = '';
        if (this.historicoIntegracao?.integracao.id === item.integracao.id)
          this.fecharHistorico();
        this.feedback = 'Integração desativada com sucesso.';
        this.carregar(false);
      },
      error: (err) => {
        this.desativandoId = '';
        this.error = err?.status === 403
          ? 'Seu perfil não possui permissão para desativar integrações.'
          : 'Não foi possível desativar a integração.';
      }
    });
  }

  abrirHistorico(item: IntegracaoFinanceiraPainel): void {
    this.historicoIntegracao = item;
    this.historicoStatus = '';
    this.historicoInicio = '';
    this.historicoFim = '';
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    if (!this.historicoIntegracao || this.loadingHistorico) return;
    if (this.historicoInicio && this.historicoFim
        && this.historicoInicio > this.historicoFim) {
      this.error = 'No histórico, a data inicial deve ser anterior ou igual à final.';
      return;
    }
    this.loadingHistorico = true;
    this.error = '';
    this.service.listarTentativasIntegracao(
      this.historicoIntegracao.integracao.id,
      this.historicoStatus,
      this.historicoInicio,
      this.historicoFim
    ).subscribe({
      next: (tentativas) => {
        this.tentativas = tentativas;
        this.loadingHistorico = false;
      },
      error: (err) => {
        this.loadingHistorico = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso ao histórico das integrações.'
          : 'Não foi possível carregar o histórico.';
      }
    });
  }

  fecharHistorico(): void {
    this.historicoIntegracao = undefined;
    this.tentativas = [];
    this.loadingHistorico = false;
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

  trackTentativa(_: number, item: IntegracaoFinanceiraTentativa): string {
    return item.id;
  }

  trackIntegracao(_: number, item: IntegracaoFinanceiraPainel): string {
    return item.integracao.id;
  }
}
