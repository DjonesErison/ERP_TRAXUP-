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

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly pedidosUrl = '/api/v1/compras/pedidos';

  constructor(private readonly http: HttpClient) {}

  listarPedidos(): Observable<PedidoCompra[]> {
    return this.http.get<PedidoCompra[]>(this.pedidosUrl);
  }

  listarItens(pedidoId: string): Observable<PedidoCompraItem[]> {
    return this.http.get<PedidoCompraItem[]>(
      `${this.pedidosUrl}/${pedidoId}/itens`
    );
  }
}
