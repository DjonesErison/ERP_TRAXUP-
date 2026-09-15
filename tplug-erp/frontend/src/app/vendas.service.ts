import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface VendaRecente {
  id: string;
  filialId: string;
  clienteId?: string | null;
  numero: string;
  status: 'RASCUNHO' | 'ABERTO' | 'FATURADO' | 'CANCELADO';
  totalLiquido: number;
  criadoEm: string;
  atualizadoEm: string;
}

export interface VendaPagina {
  conteudo: VendaRecente[];
  pagina: number;
  tamanho: number;
  totalRegistros: number;
  totalPaginas: number;
}

export interface Venda {
  id: string;
  filialId: string;
  clienteId?: string | null;
  numero: string;
  status: string;
  observacao?: string | null;
  usuarioId?: string | null;
  formaPagamentoId?: string | null;
  condicaoPagamentoId?: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface VendaComboOpcao {
  id: string;
  grupoId: string;
  opcaoId: string;
  produtoId: string;
  quantidade: number;
  valorAdicional: number;
}

export interface VendaItem {
  id: string;
  pedidoVendaId: string;
  produtoId: string;
  gradeId?: string | null;
  quantidade: number;
  precoUnitario: number;
  descontoValor: number;
  totalItem: number;
  criadoEm: string;
  atualizadoEm: string;
  comboOpcoes: VendaComboOpcao[];
}

export interface VendaTotais {
  subtotalBruto: number;
  descontoTotal: number;
  totalLiquido: number;
}

export interface VendaDetalhe {
  pedido: Venda;
  itens: VendaItem[];
  totais: VendaTotais;
}

export interface VendaFiltros {
  numero?: string;
  status?: string;
  inicio?: string;
  fim?: string;
}

@Injectable({ providedIn: 'root' })
export class VendasService {
  private readonly baseUrl = '/api/v1/vendas/pedidos';

  constructor(private readonly http: HttpClient) {}

  listarRecentes(
    pagina: number,
    tamanho: number,
    filtros: VendaFiltros
  ): Observable<VendaPagina> {
    let params = new HttpParams()
      .set('pagina', pagina)
      .set('tamanho', tamanho);
    if (filtros.numero?.trim())
      params = params.set('numero', filtros.numero.trim());
    if (filtros.status)
      params = params.set('status', filtros.status);
    if (filtros.inicio)
      params = params.set('inicio', `${filtros.inicio}T00:00:00Z`);
    if (filtros.fim)
      params = params.set('fim', `${filtros.fim}T23:59:59Z`);
    return this.http.get<VendaPagina>(
      `${this.baseUrl}/recentes`,
      { params }
    );
  }

  consultarDetalhe(pedidoId: string): Observable<VendaDetalhe> {
    return this.http.get<VendaDetalhe>(
      `${this.baseUrl}/${pedidoId}/detalhe`
    );
  }
}
