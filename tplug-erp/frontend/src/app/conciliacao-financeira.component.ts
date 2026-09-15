import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  ConciliacaoFinanceiraService,
  ConciliacaoFiltros,
  ConciliacaoLancamento,
  ConciliacaoResumo,
  ContaFinanceira,
  MovimentoFinanceiro
} from './conciliacao-financeira.service';

@Component({
  selector: 'app-conciliacao-financeira',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="reconciliation-page">
      <header class="reconciliation-head">
        <div>
          <p class="eyebrow">Financeiro</p>
          <h1>Conciliação financeira</h1>
          <p>Compare lançamentos importados com os movimentos reais da conta.</p>
        </div>
        <button (click)="carregar()" [disabled]="loading || !contaId">
          {{ loading ? 'Atualizando...' : 'Atualizar conciliação' }}
        </button>
      </header>

      <section class="filters card">
        <label>
          Conta financeira
          <select [(ngModel)]="contaId" (change)="carregar()">
            <option value="">Selecione uma conta</option>
            <option *ngFor="let conta of contas; trackBy: trackConta" [value]="conta.id">
              {{ conta.nome }} · {{ conta.tipo }} · {{ conta.saldo | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}
            </option>
          </select>
        </label>
        <label>Origem<input [(ngModel)]="filtros.origem" placeholder="OFX, banco ou PSP"></label>
        <label>
          Status
          <select [(ngModel)]="filtros.status">
            <option value="">Todos</option><option value="PENDENTE">Pendente</option><option value="CONCILIADO">Conciliado</option>
          </select>
        </label>
        <label>
          Natureza
          <select [(ngModel)]="filtros.natureza">
            <option value="">Todas</option><option value="NORMAL">Normal</option><option value="TAXA">Taxa</option>
            <option value="ANTECIPACAO">Antecipação</option><option value="ESTORNO">Estorno</option><option value="CHARGEBACK">Chargeback</option>
          </select>
        </label>
        <label>
          Tipo
          <select [(ngModel)]="filtros.tipo">
            <option value="">Todos</option><option value="ENTRADA">Entrada</option><option value="SAIDA">Saída</option>
          </select>
        </label>
        <label>De<input type="date" [(ngModel)]="filtros.inicio"></label>
        <label>Até<input type="date" [(ngModel)]="filtros.fim"></label>
        <button class="secondary" (click)="carregar()" [disabled]="loading || !contaId">Aplicar filtros</button>
      </section>

      <div class="alert" *ngIf="error">{{ error }}</div>
      <div class="feedback" *ngIf="feedback">{{ feedback }}</div>
      <div class="empty card" *ngIf="!loadingContas && contas.length === 0 && !error">
        Nenhuma conta financeira disponível para este usuário.
      </div>

      <section class="metrics" *ngIf="resumo as dados">
        <article class="card metric"><span>Pendentes</span><strong>{{ dados.pendentes }}</strong><small>{{ dados.valorPendente | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</small></article>
        <article class="card metric"><span>Conciliados</span><strong>{{ dados.conciliados }}</strong><small>{{ dados.valorConciliado | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</small></article>
        <article class="card metric warning"><span>Taxas e antecipações</span><strong>{{ dados.taxas + dados.antecipacoes }}</strong><small>{{ dados.valorTaxas + dados.valorAntecipacoes | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</small></article>
        <article class="card metric danger"><span>Estornos e chargebacks</span><strong>{{ dados.estornos + dados.chargebacks }}</strong><small>{{ dados.valorEstornos + dados.valorChargebacks | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</small></article>
      </section>

      <section class="card panel" *ngIf="contaId">
        <div class="panel-head">
          <div><p class="eyebrow">Lançamentos externos</p><h2>Movimentos para conferência</h2></div>
          <span>Até 100 registros</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Data</th><th>Descrição</th><th>Origem</th><th>Natureza</th><th>Tipo</th><th>Valor</th><th>Status</th><th></th></tr></thead>
            <tbody>
              <tr *ngFor="let item of lancamentos; trackBy: trackLancamento">
                <td>{{ item.ocorridoEm | date:'dd/MM/yyyy' }}</td>
                <td><strong>{{ item.descricao || 'Sem descrição' }}</strong><small>{{ item.referenciaExterna }}</small></td>
                <td>{{ item.origem }}</td>
                <td><span class="tag">{{ nomeNatureza(item.natureza) }}</span></td>
                <td>{{ item.tipo === 'ENTRADA' ? 'Entrada' : 'Saída' }}</td>
                <td [class.negative]="item.tipo === 'SAIDA'">{{ item.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
                <td><span class="status" [class.done]="item.status === 'CONCILIADO'">{{ item.status === 'CONCILIADO' ? 'Conciliado' : 'Pendente' }}</span></td>
                <td><button class="link-button" *ngIf="item.status === 'PENDENTE'" (click)="abrirSugestoes(item)">Conciliar</button></td>
              </tr>
              <tr *ngIf="!loading && lancamentos.length === 0"><td colspan="8" class="empty">Nenhum lançamento encontrado com os filtros informados.</td></tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="suggestions card" *ngIf="selecionado as lancamento">
        <div class="panel-head">
          <div><p class="eyebrow">Matching seguro · ±3 dias</p><h2>Sugestões para {{ lancamento.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</h2></div>
          <button class="close" (click)="fecharSugestoes()">Fechar</button>
        </div>
        <p class="suggestion-note">Somente movimentos da mesma conta, filial, tipo e valor são apresentados.</p>
        <div class="suggestion-list">
          <article *ngFor="let movimento of sugestoes; trackBy: trackMovimento">
            <div>
              <strong>{{ movimento.descricao || 'Movimento financeiro' }}</strong>
              <span>{{ movimento.ocorridoEm | date:'dd/MM/yyyy HH:mm' }} · {{ movimento.origemTipo || 'Manual' }}</span>
            </div>
            <button (click)="confirmarConciliacao(movimento)" [disabled]="conciliando">
              {{ conciliando ? 'Confirmando...' : 'Confirmar vínculo' }}
            </button>
          </article>
          <div class="empty" *ngIf="!loadingSugestoes && sugestoes.length === 0">Nenhum movimento compatível foi encontrado.</div>
          <div class="empty" *ngIf="loadingSugestoes">Buscando movimentos compatíveis...</div>
        </div>
      </section>
    </section>
  `,
  styleUrl: './conciliacao-financeira.component.css'
})
export class ConciliacaoFinanceiraComponent implements OnInit {
  contas: ContaFinanceira[] = [];
  contaId = '';
  filtros: ConciliacaoFiltros = {};
  lancamentos: ConciliacaoLancamento[] = [];
  resumo?: ConciliacaoResumo;
  selecionado?: ConciliacaoLancamento;
  sugestoes: MovimentoFinanceiro[] = [];
  loadingContas = false;
  loading = false;
  loadingSugestoes = false;
  conciliando = false;
  error = '';
  feedback = '';

  constructor(private readonly service: ConciliacaoFinanceiraService) {}

  ngOnInit(): void {
    this.loadingContas = true;
    this.service.listarContas().subscribe({
      next: (contas) => {
        this.contas = contas;
        this.loadingContas = false;
        this.contaId = contas.find(conta => conta.ativo)?.id ?? contas[0]?.id ?? '';
        if (this.contaId) this.carregar();
      },
      error: (err) => {
        this.loadingContas = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso às contas financeiras.'
          : 'Não foi possível carregar as contas financeiras.';
      }
    });
  }

  carregar(): void {
    if (!this.contaId) return;
    if (this.filtros.inicio && this.filtros.fim && this.filtros.inicio > this.filtros.fim) {
      this.error = 'A data inicial deve ser anterior ou igual à data final.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.feedback = '';
    this.fecharSugestoes();
    forkJoin({
      resumo: this.service.consultarResumo(this.contaId, this.filtros),
      lancamentos: this.service.listarLancamentos(this.contaId, this.filtros)
    }).subscribe({
      next: (dados) => {
        this.resumo = dados.resumo;
        this.lancamentos = dados.lancamentos;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso à conciliação financeira.'
          : 'Não foi possível carregar a conciliação com estes filtros.';
      }
    });
  }

  abrirSugestoes(lancamento: ConciliacaoLancamento): void {
    this.selecionado = lancamento;
    this.sugestoes = [];
    this.loadingSugestoes = true;
    this.feedback = '';
    this.service.listarSugestoes(lancamento.id).subscribe({
      next: (sugestoes) => {
        this.sugestoes = sugestoes;
        this.loadingSugestoes = false;
      },
      error: () => {
        this.loadingSugestoes = false;
        this.error = 'Não foi possível buscar sugestões para este lançamento.';
      }
    });
  }

  confirmarConciliacao(movimento: MovimentoFinanceiro): void {
    if (!this.selecionado || this.conciliando) return;
    this.conciliando = true;
    this.error = '';
    this.service.conciliar(this.selecionado.id, movimento.id).subscribe({
      next: () => {
        this.conciliando = false;
        this.fecharSugestoes();
        this.carregar();
        this.feedback = 'Lançamento conciliado com sucesso.';
      },
      error: (err) => {
        this.conciliando = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui permissão para confirmar conciliações.'
          : 'O lançamento mudou ou já foi conciliado. Atualize e tente novamente.';
      }
    });
  }

  fecharSugestoes(): void {
    this.selecionado = undefined;
    this.sugestoes = [];
    this.loadingSugestoes = false;
  }

  nomeNatureza(natureza: string): string {
    const nomes: Record<string, string> = {
      NORMAL: 'Normal', TAXA: 'Taxa', ANTECIPACAO: 'Antecipação',
      ESTORNO: 'Estorno', CHARGEBACK: 'Chargeback'
    };
    return nomes[natureza] ?? natureza;
  }

  trackConta(_: number, conta: ContaFinanceira): string { return conta.id; }
  trackLancamento(_: number, item: ConciliacaoLancamento): string { return item.id; }
  trackMovimento(_: number, item: MovimentoFinanceiro): string { return item.id; }
}
