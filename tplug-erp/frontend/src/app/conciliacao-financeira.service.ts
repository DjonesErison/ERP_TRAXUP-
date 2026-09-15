import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ContaFinanceira {
  id: string;
  filialId: string;
  nome: string;
  tipo: 'CAIXA' | 'BANCO';
  saldo: number;
  ativo: boolean;
  criadoEm: string;
  atualizadoEm: string;
  versao: number;
}

export interface ConciliacaoFiltros {
  origem?: string;
  natureza?: string;
  status?: string;
  tipo?: string;
  inicio?: string;
  fim?: string;
}

export interface ConciliacaoLancamento {
  id: string;
  contaFinanceiraId: string;
  origem: string;
  referenciaExterna: string;
  tipo: 'ENTRADA' | 'SAIDA';
  valor: number;
  descricao?: string | null;
  natureza: 'NORMAL' | 'TAXA' | 'ANTECIPACAO' | 'ESTORNO' | 'CHARGEBACK';
  ocorridoEm: string;
  status: 'PENDENTE' | 'CONCILIADO';
  movimentoId?: string | null;
  conciliadoEm?: string | null;
  criadoEm: string;
}

export interface ConciliacaoResumo {
  totalLancamentos: number;
  valorTotal: number;
  pendentes: number;
  valorPendente: number;
  conciliados: number;
  valorConciliado: number;
  taxas: number;
  valorTaxas: number;
  antecipacoes: number;
  valorAntecipacoes: number;
  estornos: number;
  valorEstornos: number;
  chargebacks: number;
  valorChargebacks: number;
}

export interface MovimentoFinanceiro {
  id: string;
  contaFinanceiraId: string;
  tipo: 'ENTRADA' | 'SAIDA';
  valor: number;
  descricao?: string | null;
  origemTipo?: string | null;
  origemId?: string | null;
  origemReferencia?: string | null;
  ocorridoEm: string;
}

@Injectable({ providedIn: 'root' })
export class ConciliacaoFinanceiraService {
  private readonly financeiroUrl = '/api/v1/financeiro';
  private readonly conciliacaoUrl = `${this.financeiroUrl}/conciliacao`;

  constructor(private readonly http: HttpClient) {}

  listarContas(): Observable<ContaFinanceira[]> {
    return this.http.get<ContaFinanceira[]>(
      `${this.financeiroUrl}/contas-financeiras`
    );
  }

  listarLancamentos(
    contaId: string,
    filtros: ConciliacaoFiltros
  ): Observable<ConciliacaoLancamento[]> {
    return this.http.get<ConciliacaoLancamento[]>(
      `${this.conciliacaoUrl}/contas/${contaId}/lancamentos`,
      { params: this.parametros(filtros).set('limite', 100) }
    );
  }

  consultarResumo(
    contaId: string,
    filtros: ConciliacaoFiltros
  ): Observable<ConciliacaoResumo> {
    return this.http.get<ConciliacaoResumo>(
      `${this.conciliacaoUrl}/contas/${contaId}/resumo`,
      { params: this.parametros(filtros) }
    );
  }

  importarOfx(
    contaId: string,
    conteudo: string
  ): Observable<ConciliacaoLancamento[]> {
    return this.http.post<ConciliacaoLancamento[]>(
      `${this.conciliacaoUrl}/contas/${contaId}/ofx`,
      { conteudo }
    );
  }

  classificar(
    lancamentoId: string,
    natureza: ConciliacaoLancamento['natureza']
  ): Observable<ConciliacaoLancamento> {
    return this.http.post<ConciliacaoLancamento>(
      `${this.conciliacaoUrl}/lancamentos/${lancamentoId}/classificar`,
      { natureza }
    );
  }

  listarSugestoes(
    lancamentoId: string
  ): Observable<MovimentoFinanceiro[]> {
    return this.http.get<MovimentoFinanceiro[]>(
      `${this.conciliacaoUrl}/lancamentos/${lancamentoId}/sugestoes`,
      { params: new HttpParams().set('limite', 20) }
    );
  }

  conciliar(
    lancamentoId: string,
    movimentoId: string
  ): Observable<ConciliacaoLancamento> {
    return this.http.post<ConciliacaoLancamento>(
      `${this.conciliacaoUrl}/lancamentos/${lancamentoId}/conciliar`,
      { movimentoId }
    );
  }

  private parametros(filtros: ConciliacaoFiltros): HttpParams {
    let params = new HttpParams();
    if (filtros.origem?.trim())
      params = params.set('origem', filtros.origem.trim());
    if (filtros.natureza)
      params = params.set('natureza', filtros.natureza);
    if (filtros.status)
      params = params.set('status', filtros.status);
    if (filtros.tipo)
      params = params.set('tipo', filtros.tipo);
    if (filtros.inicio)
      params = params.set('inicio', `${filtros.inicio}T00:00:00Z`);
    if (filtros.fim)
      params = params.set('fim', `${filtros.fim}T23:59:59Z`);
    return params;
  }
}
