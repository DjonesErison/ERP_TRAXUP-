import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ComprasService,
  CriarPedidoCompra,
  FilialCompraOpcao,
  FornecedorCompraOpcao,
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
        <div class="header-actions">
          <button class="secondary" type="button" (click)="alternarNovoPedido()" *ngIf="cadastrosDisponiveis">
            {{ novoPedidoAberto ? 'Fechar cadastro' : 'Novo pedido' }}
          </button>
          <button type="button" (click)="carregarTudo()" [disabled]="loading || loadingRecebimentos">
            {{ loading || loadingRecebimentos ? 'Atualizando...' : 'Atualizar compras' }}
          </button>
        </div>
      </header>


      <form class="card create-panel" *ngIf="novoPedidoAberto" (ngSubmit)="criarPedido()">
        <div class="panel-head">
          <div><p class="eyebrow">Novo pedido</p><h2>Dados da compra</h2></div>
          <span>O pedido será criado como rascunho</span>
        </div>
        <div class="create-grid">
          <label>
            Filial
            <select name="filialId" [(ngModel)]="novoPedido.filialId" required>
              <option value="">Selecione a filial</option>
              <option *ngFor="let filial of filiaisAtivas" [value]="filial.id">
                {{ filial.nome }} · {{ filial.cnpj }}
              </option>
            </select>
          </label>
          <label>
            Fornecedor
            <select name="fornecedorId" [(ngModel)]="novoPedido.fornecedorId" required>
              <option value="">Selecione o fornecedor</option>
              <option *ngFor="let fornecedor of fornecedoresAtivos" [value]="fornecedor.id">
                {{ nomeFornecedor(fornecedor) }} · {{ fornecedor.cpfCnpj }}
              </option>
            </select>
          </label>
          <label>
            Número do pedido
            <input name="numero" [(ngModel)]="novoPedido.numero" required maxlength="40" placeholder="Ex.: PC-2026-001">
          </label>
          <label class="observation-field">
            Observação
            <textarea name="observacao" [(ngModel)]="novoPedido.observacao" maxlength="500" placeholder="Informações opcionais da compra"></textarea>
          </label>
        </div>
        <div class="form-actions">
          <button type="button" class="secondary" (click)="alternarNovoPedido()">Cancelar</button>
          <button type="submit" [disabled]="criandoPedido || !novoPedido.filialId || !novoPedido.fornecedorId || !novoPedido.numero.trim()">
            {{ criandoPedido ? 'Criando...' : 'Criar pedido' }}
          </button>
        </div>
      </form>

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
      <div class="success-alert page-message" *ngIf="sucessoPedido">{{ sucessoPedido }}</div>

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
                <td>
                  <div class="row-actions">
                    <button class="detail-button" (click)="abrirDetalhe(pedido)">Ver itens</button>
                    <button
                      class="action-button"
                      *ngIf="pedido.status === 'RASCUNHO'"
                      (click)="executarAcaoPedido(pedido, 'abrir')"
                      [disabled]="acaoPedidoId === pedido.id">
                      {{ acaoPedidoId === pedido.id ? 'Processando...' : 'Abrir pedido' }}
                    </button>
                    <button
                      class="danger-button"
                      *ngIf="pedido.status === 'RASCUNHO' || pedido.status === 'ABERTO'"
                      (click)="executarAcaoPedido(pedido, 'cancelar')"
                      [disabled]="acaoPedidoId === pedido.id">
                      Cancelar
                    </button>
                  </div>
                </td>
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
  filiais: FilialCompraOpcao[] = [];
  fornecedores: FornecedorCompraOpcao[] = [];
  novoPedido: CriarPedidoCompra = {
    filialId: '',
    fornecedorId: '',
    numero: '',
    observacao: ''
  };
  recebimentos: RecebimentoCompra[] = [];
  recebimentoSelecionado?: RecebimentoCompra;
  itensRecebimento: RecebimentoCompraItem[] = [];
  busca = '';
  status = '';
  loading = false;
  loadingItens = false;
  criandoPedido = false;
  novoPedidoAberto = false;
  cadastrosDisponiveis = false;
  loadingRecebimentos = false;
  loadingItensRecebimento = false;
  acessoRecebimentos = true;
  error = '';
  errorRecebimentos = '';
  sucessoRecebimento = '';
  sucessoPedido = '';
  integrandoId = '';
  acaoPedidoId = '';

  constructor(private readonly service: ComprasService) {}

  ngOnInit(): void {
    this.carregarTudo();
    this.carregarOpcoesPedido();
  }

  carregarTudo(): void {
    this.carregar();
    this.carregarRecebimentos();
  }

  get filiaisAtivas(): FilialCompraOpcao[] {
    return this.filiais.filter(filial => filial.ativo);
  }

  get fornecedoresAtivos(): FornecedorCompraOpcao[] {
    return this.fornecedores.filter(
      fornecedor => fornecedor.ativo && fornecedor.fornecedor
    );
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

  carregarOpcoesPedido(): void {
    this.service.carregarOpcoesPedido().subscribe({
      next: (opcoes) => {
        this.filiais = opcoes.filiais;
        this.fornecedores = opcoes.fornecedores;
        this.cadastrosDisponiveis =
          this.filiaisAtivas.length > 0 && this.fornecedoresAtivos.length > 0;
      },
      error: () => {
        this.cadastrosDisponiveis = false;
        this.novoPedidoAberto = false;
      }
    });
  }

  alternarNovoPedido(): void {
    this.novoPedidoAberto = !this.novoPedidoAberto;
    this.error = '';
    if (!this.novoPedidoAberto) this.limparNovoPedido();
  }

  criarPedido(): void {
    if (this.criandoPedido
        || !this.novoPedido.filialId
        || !this.novoPedido.fornecedorId
        || !this.novoPedido.numero.trim()) return;

    this.criandoPedido = true;
    this.error = '';
    this.sucessoPedido = '';
    const request: CriarPedidoCompra = {
      ...this.novoPedido,
      numero: this.novoPedido.numero.trim(),
      observacao: this.novoPedido.observacao?.trim() || undefined
    };
    this.service.criarPedido(request).subscribe({
      next: (pedido) => {
        this.criandoPedido = false;
        this.pedidos = [pedido, ...this.pedidos];
        this.aplicarFiltros();
        this.limparNovoPedido();
        this.novoPedidoAberto = false;
        this.sucessoPedido =
          'Pedido criado como rascunho. Adicione os itens antes de abri-lo.';
      },
      error: (err) => {
        this.criandoPedido = false;
        if (err?.status === 403) {
          this.error = 'Seu perfil não possui permissão para criar pedidos de compra.';
          return;
        }
        if (err?.status === 409) {
          this.error = 'Já existe um pedido de compra com este número.';
          return;
        }
        this.error = err?.status === 400
          ? 'Revise a filial, o fornecedor e o número informados.'
          : 'Não foi possível criar o pedido de compra.';
      }
    });
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

  executarAcaoPedido(
    pedido: PedidoCompra,
    acao: 'abrir' | 'cancelar'
  ): void {
    if (this.acaoPedidoId) return;
    const mensagem = acao === 'abrir'
      ? 'Abrir este pedido de compra? Depois disso, os itens não poderão mais ser alterados.'
      : 'Cancelar este pedido de compra? Esta ação altera o estado operacional do pedido.';
    if (!window.confirm(mensagem)) return;

    this.acaoPedidoId = pedido.id;
    this.error = '';
    this.sucessoPedido = '';
    const requisicao = acao === 'abrir'
      ? this.service.abrirPedido(pedido.id)
      : this.service.cancelarPedido(pedido.id);

    requisicao.subscribe({
      next: (atualizado) => {
        this.acaoPedidoId = '';
        this.pedidos = this.pedidos.map(item =>
          item.id === atualizado.id ? atualizado : item
        );
        this.aplicarFiltros();
        this.sucessoPedido = acao === 'abrir'
          ? 'Pedido aberto com sucesso e pronto para recebimento.'
          : 'Pedido cancelado com sucesso.';
      },
      error: (err) => {
        this.acaoPedidoId = '';
        if (err?.status === 403) {
          this.error =
            'Seu perfil não possui permissão para alterar pedidos de compra.';
          return;
        }
        if ([400, 409, 422].includes(err?.status)) {
          this.error = acao === 'abrir'
            ? 'O pedido precisa estar em rascunho e possuir itens para ser aberto.'
            : 'O pedido não pode ser cancelado no estado atual.';
          return;
        }
        this.error = 'Não foi possível atualizar o pedido de compra.';
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

  nomeFornecedor(fornecedor: FornecedorCompraOpcao): string {
    return fornecedor.nomeFantasia?.trim()
      || fornecedor.nomeRazaoSocial;
  }

  private limparNovoPedido(): void {
    this.novoPedido = {
      filialId: '',
      fornecedorId: '',
      numero: '',
      observacao: ''
    };
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
