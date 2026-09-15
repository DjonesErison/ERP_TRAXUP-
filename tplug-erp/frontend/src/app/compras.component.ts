import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ComprasService,
  PedidoCompra,
  PedidoCompraItem,
  RecebimentoCompra,
  RecebimentoCompraItem
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
        <button (click)="carregarTudo()" [disabled]="loading || loadingRecebimentos">
          {{ loading || loadingRecebimentos ? 'Atualizando...' : 'Atualizar compras' }}
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


      <section class="card receipts-list" *ngIf="acessoRecebimentos">
        <div class="panel-head">
          <div><p class="eyebrow">Recebimento</p><h2>Entradas de compras</h2></div>
          <span>{{ recebimentos.length }} registro(s)</span>
        </div>
        <div class="inline-alert" *ngIf="errorRecebimentos">{{ errorRecebimentos }}</div>
        <div class="success-alert" *ngIf="sucessoRecebimento">{{ sucessoRecebimento }}</div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>Documento</th><th>Recebido em</th><th>Pedido</th><th>Fornecedor</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let recebimento of recebimentos; trackBy: trackRecebimento">
                <td><strong>{{ recebimento.documento || 'Sem documento' }}</strong><small>{{ abreviar(recebimento.id) }}</small></td>
                <td>{{ recebimento.recebidoEm | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ numeroPedido(recebimento.pedidoCompraId) }}</td>
                <td>{{ abreviar(recebimento.fornecedorId) }}</td>
                <td><span class="status" [ngClass]="classeStatusRecebimento(recebimento.status)">{{ nomeStatusRecebimento(recebimento.status) }}</span></td>
                <td>
                  <div class="row-actions">
                    <button class="detail-button" (click)="abrirDetalheRecebimento(recebimento)">Conferir itens</button>
                    <button
                      class="action-button"
                      *ngIf="recebimento.status === 'CONFERIDO'"
                      (click)="integrarEstoque(recebimento)"
                      [disabled]="integrandoId === recebimento.id">
                      {{ integrandoId === recebimento.id ? 'Integrando...' : 'Integrar estoque' }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="!loadingRecebimentos && recebimentos.length === 0">
                <td colspan="6" class="empty">Nenhum recebimento registrado.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="permission-note" *ngIf="!acessoRecebimentos">
        Os recebimentos exigem a permissão COMPRA_RECEBIMENTO_LER. A consulta de pedidos continua disponível.
      </section>

      <section class="card receipt-detail" *ngIf="recebimentoSelecionado as recebimento">
        <div class="panel-head">
          <div><p class="eyebrow">Recebimento {{ recebimento.documento || abreviar(recebimento.id) }}</p><h2>Conferência recebida</h2></div>
          <button class="secondary" (click)="fecharDetalheRecebimento()">Fechar</button>
        </div>

        <div class="detail-summary">
          <span>Status<strong>{{ nomeStatusRecebimento(recebimento.status) }}</strong></span>
          <span>Itens<strong>{{ itensRecebimento.length }}</strong></span>
          <span>Quantidade recebida<strong>{{ quantidadeRecebidaTotal | number:'1.0-4':'pt-BR' }}</strong></span>
          <span>Valor recebido<strong>{{ valorRecebidoTotal | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></span>
        </div>

        <div class="items" *ngIf="!loadingItensRecebimento">
          <article *ngFor="let item of itensRecebimento; trackBy: trackItemRecebimento">
            <div>
              <strong>Produto {{ abreviar(item.produtoId) }}</strong>
              <small *ngIf="item.gradeId">Grade {{ abreviar(item.gradeId) }}</small>
            </div>
            <span>Pedido: {{ item.quantidadePedida | number:'1.0-4':'pt-BR' }} · Recebido: {{ item.quantidadeRecebida | number:'1.0-4':'pt-BR' }}</span>
            <strong>{{ valorItemRecebido(item) | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
          </article>
          <div class="empty" *ngIf="itensRecebimento.length === 0">Recebimento sem itens.</div>
        </div>

        <div class="loading" *ngIf="loadingItensRecebimento">Carregando conferência...</div>
        <p class="observation" *ngIf="recebimento.observacao">{{ recebimento.observacao }}</p>
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
  recebimentos: RecebimentoCompra[] = [];
  recebimentoSelecionado?: RecebimentoCompra;
  itensRecebimento: RecebimentoCompraItem[] = [];
  busca = '';
  status = '';
  loading = false;
  loadingItens = false;
  loadingRecebimentos = false;
  loadingItensRecebimento = false;
  acessoRecebimentos = true;
  error = '';
  errorRecebimentos = '';
  sucessoRecebimento = '';
  integrandoId = '';

  constructor(private readonly service: ComprasService) {}

  ngOnInit(): void {
    this.carregarTudo();
  }

  carregarTudo(): void {
    this.carregar();
    this.carregarRecebimentos();
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

  get quantidadeRecebidaTotal(): number {
    return this.itensRecebimento.reduce(
      (total, item) => total + Number(item.quantidadeRecebida), 0
    );
  }

  get valorRecebidoTotal(): number {
    return this.itensRecebimento.reduce(
      (total, item) => total + this.valorItemRecebido(item), 0
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

  carregarRecebimentos(): void {
    this.loadingRecebimentos = true;
    this.errorRecebimentos = '';
    this.sucessoRecebimento = '';
    this.service.listarRecebimentos().subscribe({
      next: (recebimentos) => {
        this.recebimentos = recebimentos;
        this.acessoRecebimentos = true;
        this.loadingRecebimentos = false;
      },
      error: (err) => {
        this.loadingRecebimentos = false;
        if (err?.status === 403) {
          this.acessoRecebimentos = false;
          this.recebimentos = [];
          return;
        }
        this.acessoRecebimentos = true;
        this.errorRecebimentos = 'Não foi possível carregar os recebimentos.';
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
    this.fecharDetalheRecebimento();
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

  integrarEstoque(recebimento: RecebimentoCompra): void {
    if (recebimento.status !== 'CONFERIDO' || this.integrandoId) return;
    const confirmado = window.confirm(
      'Integrar este recebimento ao estoque? Esta operação movimentará os saldos e não poderá ser repetida.'
    );
    if (!confirmado) return;

    this.integrandoId = recebimento.id;
    this.errorRecebimentos = '';
    this.sucessoRecebimento = '';
    this.service.integrarRecebimentoEstoque(recebimento.id).subscribe({
      next: (atualizado) => {
        this.integrandoId = '';
        this.recebimentos = this.recebimentos.map(item =>
          item.id === atualizado.id ? atualizado : item
        );
        if (this.recebimentoSelecionado?.id === atualizado.id) {
          this.recebimentoSelecionado = atualizado;
        }
        this.sucessoRecebimento =
          'Recebimento integrado ao estoque com sucesso.';
        this.carregar();
      },
      error: (err) => {
        this.integrandoId = '';
        if (err?.status === 403) {
          this.errorRecebimentos =
            'Seu perfil não possui permissão para integrar recebimentos ao estoque.';
          return;
        }
        if (err?.status === 409 || err?.status === 422) {
          this.errorRecebimentos =
            'O recebimento não pode ser integrado no estado atual. Atualize a consulta e tente novamente.';
          return;
        }
        this.errorRecebimentos =
          'Não foi possível integrar o recebimento ao estoque.';
      }
    });
  }

  abrirDetalheRecebimento(recebimento: RecebimentoCompra): void {
    this.fecharDetalhe();
    this.recebimentoSelecionado = recebimento;
    this.itensRecebimento = [];
    this.loadingItensRecebimento = true;
    this.errorRecebimentos = '';
    this.service.listarItensRecebimento(recebimento.id).subscribe({
      next: (itens) => {
        this.itensRecebimento = itens;
        this.loadingItensRecebimento = false;
      },
      error: () => {
        this.loadingItensRecebimento = false;
        this.errorRecebimentos = 'Não foi possível carregar a conferência deste recebimento.';
      }
    });
  }

  fecharDetalheRecebimento(): void {
    this.recebimentoSelecionado = undefined;
    this.itensRecebimento = [];
    this.loadingItensRecebimento = false;
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

  nomeStatusRecebimento(status: string): string {
    return status === 'INTEGRADO_ESTOQUE'
      ? 'Integrado ao estoque'
      : status === 'CONFERIDO' ? 'Conferido' : status;
  }

  classeStatusRecebimento(status: string): string {
    return status === 'INTEGRADO_ESTOQUE' ? 'received' : 'checked';
  }

  numeroPedido(pedidoId: string): string {
    return this.pedidos.find(pedido => pedido.id === pedidoId)?.numero
      ?? this.abreviar(pedidoId);
  }

  valorItemRecebido(item: RecebimentoCompraItem): number {
    return Number(item.quantidadeRecebida) * Number(item.precoUnitario);
  }

  abreviar(id: string): string {
    return id.slice(0, 8).toUpperCase();
  }

  trackPedido(_: number, pedido: PedidoCompra): string { return pedido.id; }
  trackItem(_: number, item: PedidoCompraItem): string { return item.id; }
  trackRecebimento(_: number, item: RecebimentoCompra): string { return item.id; }
  trackItemRecebimento(_: number, item: RecebimentoCompraItem): string { return item.id; }
}
