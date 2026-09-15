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
  status: string;
  documento?: string | null;
  observacao?: string | null;
  usuarioId?: string | null;
  recebidoEm: string;
  criadoEm: string;
}

export interface ComprasPainel {
  pedidos: PedidoCompra[];
  recebimentos: RecebimentoCompra[];
}

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly pedidosUrl = '/api/v1/compras/pedidos';
  private readonly recebimentosUrl = '/api/v1/compras/recebimentos';

  constructor(private readonly http: HttpClient) {}

  carregarPainel(): Observable<ComprasPainel> {
    return forkJoin({
      pedidos: this.http.get<PedidoCompra[]>(this.pedidosUrl),
      recebimentos: this.http.get<RecebimentoCompra[]>(this.recebimentosUrl)
    });
  }

  listarItens(pedidoId: string): Observable<PedidoCompraItem[]> {
    return this.http.get<PedidoCompraItem[]>(
      `${this.pedidosUrl}/${pedidoId}/itens`
    );
  }
}
