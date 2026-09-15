import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ComprasService,
  PedidoCompra,
  PedidoCompraItem
} from './compras.service';

@Component({
  selector: 'app-compras',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="purchases-page">
      <header class="purchases-head">
        <div>
          <p class="eyebrow">Compras</p>
          <h1>Pedidos de compra</h1>
          <p>Acompanhe pedidos e confira seus itens sem alterar o fluxo de recebimento.</p>
        </div>
        <button (click)="carregar()" [disabled]="loading">
          {{ loading ? 'Atualizando...' : 'Atualizar pedidos' }}
        </button>
      </header>

      <section class="filters card">
        <label>
          Buscar pedido
          <input [(ngModel)]="busca" placeholder="Número do pedido" (keyup.enter)="aplicarFiltros()">
        </label>
        <label>
          Status
          <select [(ngModel)]="status">
            <option value="">Todos</option>
            <option value="RASCUNHO">Rascunho</option>
            <option value="ABERTO">Aberto</option>
            <option value="RECEBIDO">Recebido</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
        </label>
        <button class="secondary" (click)="aplicarFiltros()">Aplicar filtros</button>
        <button class="link-button" (click)="limparFiltros()" *ngIf="busca || status">Limpar</button>
      </section>

      <div class="alert" *ngIf="error">{{ error }}</div>

      <section class="metrics">
        <article class="card"><span>Total de pedidos</span><strong>{{ pedidos.length }}</strong><small>no tenant autenticado</small></article>
        <article class="card"><span>Rascunhos</span><strong>{{ quantidadeStatus('RASCUNHO') }}</strong><small>em preparação</small></article>
        <article class="card"><span>Em aberto</span><strong>{{ quantidadeStatus('ABERTO') }}</strong><small>aguardando recebimento</small></article>
        <article class="card"><span>Recebidos</span><strong>{{ quantidadeStatus('RECEBIDO') }}</strong><small>entrada concluída</small></article>
      </section>

      <section class="card purchase-list">
        <div class="panel-head">
          <div><p class="eyebrow">Consulta operacional</p><h2>Pedidos recentes</h2></div>
          <span>{{ pedidosFiltrados.length }} registro(s)</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>Pedido</th><th>Data</th><th>Filial</th><th>Fornecedor</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let pedido of pedidosFiltrados; trackBy: trackPedido">
                <td><strong>{{ pedido.numero }}</strong><small>{{ abreviar(pedido.id) }}</small></td>
                <td>{{ pedido.criadoEm | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ abreviar(pedido.filialId) }}</td>
                <td>{{ abreviar(pedido.fornecedorId) }}</td>
                <td><span class="status" [ngClass]="classeStatus(pedido.status)">{{ nomeStatus(pedido.status) }}</span></td>
                <td><button class="detail-button" (click)="abrirDetalhe(pedido)">Ver itens</button></td>
              </tr>
              <tr *ngIf="!loading && pedidosFiltrados.length === 0">
                <td colspan="6" class="empty">Nenhum pedido encontrado.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card purchase-detail" *ngIf="pedidoSelecionado as pedido">
        <div class="panel-head">
          <div><p class="eyebrow">Pedido {{ pedido.numero }}</p><h2>Itens do pedido</h2></div>
          <button class="secondary" (click)="fecharDetalhe()">Fechar</button>
        </div>

        <div class="detail-summary">
          <span>Status<strong>{{ nomeStatus(pedido.status) }}</strong></span>
          <span>Itens<strong>{{ itens.length }}</strong></span>
          <span>Quantidade<strong>{{ quantidadeTotal | number:'1.0-4':'pt-BR' }}</strong></span>
          <span>Total<strong>{{ valorTotal | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></span>
        </div>

        <div class="items" *ngIf="!loadingItens">
          <article *ngFor="let item of itens; trackBy: trackItem">
            <div>
              <strong>Produto {{ abreviar(item.produtoId) }}</strong>
              <small *ngIf="item.gradeId">Grade {{ abreviar(item.gradeId) }}</small>
            </div>
            <span>{{ item.quantidade | number:'1.0-4':'pt-BR' }} × {{ item.precoUnitario | currency:'BRL':'symbol':'1.2-4':'pt-BR' }}</span>
            <strong>{{ item.totalItem | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
          </article>
          <div class="empty" *ngIf="itens.length === 0">Pedido sem itens cadastrados.</div>
        </div>

        <div class="loading" *ngIf="loadingItens">Carregando itens do pedido...</div>
        <p class="observation" *ngIf="pedido.observacao">{{ pedido.observacao }}</p>
      </section>
    </section>
  `,
  styleUrl: './compras.component.css'
})
export class ComprasComponent implements OnInit {
  pedidos: PedidoCompra[] = [];
  pedidosFiltrados: PedidoCompra[] = [];
  pedidoSelecionado?: PedidoCompra;
  itens: PedidoCompraItem[] = [];
  busca = '';
  status = '';
  loading = false;
  loadingItens = false;
  error = '';

  constructor(private readonly service: ComprasService) {}

  ngOnInit(): void {
    this.carregar();
  }

  get quantidadeTotal(): number {
    return this.itens.reduce(
      (total, item) => total + Number(item.quantidade), 0
    );
  }

  get valorTotal(): number {
    return this.itens.reduce(
      (total, item) => total + Number(item.totalItem), 0
    );
  }

  carregar(): void {
    this.loading = true;
    this.error = '';
    this.service.listarPedidos().subscribe({
      next: (pedidos) => {
        this.pedidos = pedidos;
        this.aplicarFiltros();
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso à consulta de pedidos de compra.'
          : 'Não foi possível carregar os pedidos de compra.';
      }
    });
  }

  aplicarFiltros(): void {
    const termo = this.busca.trim().toLocaleLowerCase('pt-BR');
    this.pedidosFiltrados = this.pedidos.filter(pedido =>
      (!termo || pedido.numero.toLocaleLowerCase('pt-BR').includes(termo))
      && (!this.status || pedido.status === this.status)
    );
    this.fecharDetalhe();
  }

  limparFiltros(): void {
    this.busca = '';
    this.status = '';
    this.aplicarFiltros();
  }

  abrirDetalhe(pedido: PedidoCompra): void {
    this.pedidoSelecionado = pedido;
    this.itens = [];
    this.loadingItens = true;
    this.error = '';
    this.service.listarItens(pedido.id).subscribe({
      next: (itens) => {
        this.itens = itens;
        this.loadingItens = false;
      },
      error: (err) => {
        this.loadingItens = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso aos itens deste pedido.'
          : 'Não foi possível carregar os itens deste pedido.';
      }
    });
  }

  fecharDetalhe(): void {
    this.pedidoSelecionado = undefined;
    this.itens = [];
    this.loadingItens = false;
  }

  quantidadeStatus(status: string): number {
    return this.pedidos.filter(pedido => pedido.status === status).length;
  }

  nomeStatus(status: string): string {
    const nomes: Record<string, string> = {
      RASCUNHO: 'Rascunho',
      ABERTO: 'Aberto',
      RECEBIDO: 'Recebido',
      CANCELADO: 'Cancelado'
    };
    return nomes[status] ?? status;
  }

  classeStatus(status: string): string {
    if (status === 'RECEBIDO') return 'received';
    if (status === 'CANCELADO') return 'cancelled';
    if (status === 'ABERTO') return 'open';
    return 'draft';
  }

  abreviar(id: string): string {
    return id.slice(0, 8).toUpperCase();
  }

  trackPedido(_: number, pedido: PedidoCompra): string { return pedido.id; }
  trackItem(_: number, item: PedidoCompraItem): string { return item.id; }
}
