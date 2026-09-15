import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';

export interface PedidoCompra {
  id: string;
  filialId: string;
  fornecedorId: string;
  numero: string;
  status: 'RASCUNHO' | 'ABERTO' | 'CANCELADO' | 'RECEBIDO';
  observacao?: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface CriarPedidoCompra {
  filialId: string;
  fornecedorId: string;
  numero: string;
  observacao?: string;
}

export interface FilialCompraOpcao {
  id: string;
  nome: string;
  cnpj: string;
  ativo: boolean;
}

export interface FornecedorCompraOpcao {
  id: string;
  nomeRazaoSocial: string;
  nomeFantasia?: string | null;
  cpfCnpj: string;
  fornecedor: boolean;
  ativo: boolean;
}

export interface OpcoesPedidoCompra {
  filiais: FilialCompraOpcao[];
  fornecedores: FornecedorCompraOpcao[];
}

export interface PedidoCompraItem {
  id: string;
  pedidoCompraId: string;
  produtoId: string;
  gradeId?: string | null;
  quantidade: number;
  precoUnitario: number;
  totalItem: number;
  criadoEm: string;
  atualizadoEm: string;
}

export interface RecebimentoCompra {
  id: string;
  pedidoCompraId: string;
  filialId: string;
  fornecedorId: string;
  status: 'CONFERIDO' | 'INTEGRADO_ESTOQUE';
  documento?: string | null;
  observacao?: string | null;
  usuarioId?: string | null;
  recebidoEm: string;
  criadoEm: string;
}

export interface RecebimentoCompraItem {
  id: string;
  recebimentoId: string;
  pedidoItemId: string;
  produtoId: string;
  gradeId?: string | null;
  quantidadePedida: number;
  quantidadeRecebida: number;
  precoUnitario: number;
}

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly pedidosUrl = '/api/v1/compras/pedidos';
  private readonly recebimentosUrl = '/api/v1/compras/recebimentos';

  constructor(private readonly http: HttpClient) {}

  listarPedidos(): Observable<PedidoCompra[]> {
    return this.http.get<PedidoCompra[]>(this.pedidosUrl);
  }

  carregarOpcoesPedido(): Observable<OpcoesPedidoCompra> {
    return forkJoin({
      filiais: this.http.get<FilialCompraOpcao[]>('/api/v1/filiais'),
      fornecedores: this.http.get<FornecedorCompraOpcao[]>(
        '/api/v1/pessoas',
        { params: { papel: 'FORNECEDOR' } }
      )
    });
  }

  criarPedido(request: CriarPedidoCompra): Observable<PedidoCompra> {
    return this.http.post<PedidoCompra>(this.pedidosUrl, request);
  }

  abrirPedido(pedidoId: string): Observable<PedidoCompra> {
    return this.http.post<PedidoCompra>(
      `${this.pedidosUrl}/${pedidoId}/abrir`,
      {}
    );
  }

  cancelarPedido(pedidoId: string): Observable<PedidoCompra> {
    return this.http.post<PedidoCompra>(
      `${this.pedidosUrl}/${pedidoId}/cancelar`,
      {}
    );
  }

  listarRecebimentos(): Observable<RecebimentoCompra[]> {
    return this.http.get<RecebimentoCompra[]>(this.recebimentosUrl);
  }

  integrarRecebimentoEstoque(
    recebimentoId: string
  ): Observable<RecebimentoCompra> {
    return this.http.post<RecebimentoCompra>(
      `${this.recebimentosUrl}/${recebimentoId}/integrar-estoque`,
      {}
    );
  }

  listarItensRecebimento(
    recebimentoId: string
  ): Observable<RecebimentoCompraItem[]> {
    return this.http.get<RecebimentoCompraItem[]>(
      `${this.recebimentosUrl}/${recebimentoId}/itens`
    );
  }

  listarItens(pedidoId: string): Observable<PedidoCompraItem[]> {
    return this.http.get<PedidoCompraItem[]>(
      `${this.pedidosUrl}/${pedidoId}/itens`
    );
  }
}
