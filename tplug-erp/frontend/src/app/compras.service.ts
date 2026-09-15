import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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

  listarRecebimentos(): Observable<RecebimentoCompra[]> {
    return this.http.get<RecebimentoCompra[]>(this.recebimentosUrl);
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
