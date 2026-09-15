import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  VendaDetalhe,
  VendaFiltros,
  VendaPagina,
  VendaRecente,
  VendasService
} from './vendas.service';

@Component({
  selector: 'app-vendas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="sales-page">
      <header class="sales-head">
        <div>
          <p class="eyebrow">Vendas</p>
          <h1>Últimas vendas</h1>
          <p>Consulte pedidos, valores e itens sem alterar a operação comercial.</p>
        </div>
        <button (click)="carregar()" [disabled]="loading">
          {{ loading ? 'Atualizando...' : 'Atualizar vendas' }}
        </button>
      </header>

      <section class="filters card">
        <label>Número<input [(ngModel)]="filtros.numero" placeholder="Número do pedido"></label>
        <label>
          Status
          <select [(ngModel)]="filtros.status">
            <option value="">Todos</option>
            <option value="RASCUNHO">Rascunho</option>
            <option value="ABERTO">Aberto</option>
            <option value="FATURADO">Faturado</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
        </label>
        <label>De<input type="date" [(ngModel)]="filtros.inicio"></label>
        <label>Até<input type="date" [(ngModel)]="filtros.fim"></label>
        <button class="secondary" (click)="aplicarFiltros()" [disabled]="loading">Aplicar filtros</button>
      </section>

      <div class="alert" *ngIf="error">{{ error }}</div>

      <section class="metrics">
        <article class="card"><span>Total encontrado</span><strong>{{ pagina?.totalRegistros || 0 }}</strong><small>pedidos nos filtros</small></article>
        <article class="card"><span>Valor da página</span><strong>{{ valorPagina | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong><small>{{ vendas.length }} pedidos exibidos</small></article>
        <article class="card"><span>Faturadas na página</span><strong>{{ quantidadeStatus('FATURADO') }}</strong><small>operações concluídas</small></article>
        <article class="card"><span>Em aberto na página</span><strong>{{ quantidadeStatus('ABERTO') + quantidadeStatus('RASCUNHO') }}</strong><small>rascunhos e pedidos abertos</small></article>
      </section>

      <section class="card sales-list">
        <div class="panel-head">
          <div><p class="eyebrow">Consulta operacional</p><h2>Pedidos recentes</h2></div>
          <span>Página {{ (pagina?.pagina || 0) + 1 }} de {{ pagina?.totalPaginas || 1 }}</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Pedido</th><th>Data</th><th>Filial</th><th>Cliente</th><th>Status</th><th>Total</th><th></th></tr></thead>
            <tbody>
              <tr *ngFor="let venda of vendas; trackBy: trackVenda">
                <td><strong>{{ venda.numero }}</strong><small>{{ abreviar(venda.id) }}</small></td>
                <td>{{ venda.criadoEm | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ abreviar(venda.filialId) }}</td>
                <td>{{ venda.clienteId ? abreviar(venda.clienteId) : 'Consumidor não identificado' }}</td>
                <td><span class="status" [ngClass]="classeStatus(venda.status)">{{ nomeStatus(venda.status) }}</span></td>
                <td><strong>{{ venda.totalLiquido | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></td>
                <td><button class="detail-button" (click)="abrirDetalhe(venda)">Ver detalhes</button></td>
              </tr>
              <tr *ngIf="!loading && vendas.length === 0"><td colspan="7" class="empty">Nenhuma venda encontrada.</td></tr>
            </tbody>
          </table>
        </div>
        <div class="pagination" *ngIf="pagina && pagina.totalPaginas > 1">
          <button class="secondary" (click)="mudarPagina(-1)" [disabled]="loading || pagina.pagina === 0">Anterior</button>
          <span>{{ pagina.totalRegistros }} registro(s)</span>
          <button class="secondary" (click)="mudarPagina(1)" [disabled]="loading || pagina.pagina + 1 >= pagina.totalPaginas">Próxima</button>
        </div>
      </section>

      <section class="card sale-detail" *ngIf="detalhe as dados">
        <div class="panel-head">
          <div><p class="eyebrow">Pedido {{ dados.pedido.numero }}</p><h2>Detalhes da venda</h2></div>
          <button class="secondary" (click)="fecharDetalhe()">Fechar</button>
        </div>
        <div class="detail-summary">
          <span>Status<strong>{{ nomeStatus(dados.pedido.status) }}</strong></span>
          <span>Subtotal<strong>{{ dados.totais.subtotalBruto | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></span>
          <span>Descontos<strong>{{ dados.totais.descontoTotal | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></span>
          <span>Total líquido<strong>{{ dados.totais.totalLiquido | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></span>
        </div>
        <div class="items">
          <article *ngFor="let item of dados.itens; trackBy: trackItem">
            <div>
              <strong>Produto {{ abreviar(item.produtoId) }}</strong>
              <small *ngIf="item.gradeId">Grade {{ abreviar(item.gradeId) }}</small>
              <small *ngIf="item.comboOpcoes.length">{{ item.comboOpcoes.length }} opção(ões) de combo selecionada(s)</small>
            </div>
            <span>{{ item.quantidade }} × {{ item.precoUnitario | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</span>
            <strong>{{ item.totalItem | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
          </article>
          <div class="empty" *ngIf="dados.itens.length === 0">Pedido sem itens.</div>
        </div>
        <p class="observation" *ngIf="dados.pedido.observacao">{{ dados.pedido.observacao }}</p>
      </section>

      <section class="card loading-detail" *ngIf="loadingDetalhe">Carregando detalhes da venda...</section>
    </section>
  `,
  styleUrl: './vendas.component.css'
})
export class VendasComponent implements OnInit {
  filtros: VendaFiltros = {};
  pagina?: VendaPagina;
  vendas: VendaRecente[] = [];
  detalhe?: VendaDetalhe;
  paginaAtual = 0;
  readonly tamanhoPagina = 20;
  loading = false;
  loadingDetalhe = false;
  error = '';

  constructor(private readonly service: VendasService) {}

  ngOnInit(): void {
    this.carregar();
  }

  get valorPagina(): number {
    return this.vendas.reduce(
      (total, venda) => total + Number(venda.totalLiquido), 0
    );
  }

  carregar(): void {
    if (this.filtros.inicio && this.filtros.fim
        && this.filtros.inicio > this.filtros.fim) {
      this.error = 'A data inicial deve ser anterior ou igual à data final.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.service.listarRecentes(
      this.paginaAtual,
      this.tamanhoPagina,
      this.filtros
    ).subscribe({
      next: (pagina) => {
        this.pagina = pagina;
        this.vendas = pagina.conteudo;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso à consulta de vendas.'
          : 'Não foi possível carregar as vendas com estes filtros.';
      }
    });
  }

  aplicarFiltros(): void {
    this.paginaAtual = 0;
    this.fecharDetalhe();
    this.carregar();
  }

  mudarPagina(delta: number): void {
    const destino = this.paginaAtual + delta;
    if (destino < 0 || destino >= Number(this.pagina?.totalPaginas || 1))
      return;
    this.paginaAtual = destino;
    this.fecharDetalhe();
    this.carregar();
  }

  abrirDetalhe(venda: VendaRecente): void {
    this.loadingDetalhe = true;
    this.detalhe = undefined;
    this.error = '';
    this.service.consultarDetalhe(venda.id).subscribe({
      next: (detalhe) => {
        this.detalhe = detalhe;
        this.loadingDetalhe = false;
      },
      error: (err) => {
        this.loadingDetalhe = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso ao detalhe da venda.'
          : 'Não foi possível carregar os detalhes desta venda.';
      }
    });
  }

  fecharDetalhe(): void {
    this.detalhe = undefined;
    this.loadingDetalhe = false;
  }

  quantidadeStatus(status: string): number {
    return this.vendas.filter(venda => venda.status === status).length;
  }

  nomeStatus(status: string): string {
    const nomes: Record<string, string> = {
      RASCUNHO: 'Rascunho', ABERTO: 'Aberto',
      FATURADO: 'Faturado', CANCELADO: 'Cancelado'
    };
    return nomes[status] ?? status;
  }

  classeStatus(status: string): string {
    if (status === 'FATURADO') return 'invoiced';
    if (status === 'CANCELADO') return 'cancelled';
    if (status === 'ABERTO') return 'open';
    return 'draft';
  }

  abreviar(id: string): string {
    return id.slice(0, 8).toUpperCase();
  }

  trackVenda(_: number, venda: VendaRecente): string { return venda.id; }
  trackItem(_: number, item: { id: string }): string { return item.id; }
}
