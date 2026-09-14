import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface FilialContabilidade {
  id: string;
  empresaId: string;
  empresaNome: string;
  nome: string;
  cnpj?: string | null;
}

export interface XmlResumo {
  total: number;
  arquivados: number;
  pendentes: number;
  falhas: number;
}

export interface SpedResumo {
  total: number;
  concluidos: number;
  pendentes: number;
  falhas: number;
  cancelados: number;
}

export interface LivroCaixaResumo {
  lancamentos: number;
  entradas: number;
  saidas: number;
  saldo?: number;
}

export interface InventarioResumo {
  concluidos: number;
  ajustados: number;
  comDivergencias: number;
}

export interface FechamentoMensal {
  competencia: string;
  filialId?: string | null;
  xml: XmlResumo;
  sped: SpedResumo;
  livroCaixa: LivroCaixaResumo;
  inventario: InventarioResumo;
}

export interface SpedExportacao {
  id: string;
  filialId: string;
  tipo: 'EFD_ICMS_IPI' | 'EFD_CONTRIBUICOES';
  competencia: string;
  status: 'PENDENTE' | 'PROCESSANDO' | 'CONCLUIDO' | 'FALHOU' | 'CANCELADO';
  hashSha256?: string | null;
  versaoLayout?: string | null;
  erroCodigo?: string | null;
  criadoEm: string;
  atualizadoEm: string;
  concluidoEm?: string | null;
  retencaoAte?: string | null;
}

export interface SpedAcaoResultado {
  id: string;
  filialId?: string | null;
  status: string;
}

export interface SpedProntidao {
  workerHabilitado: boolean;
  geradorHomologadoConfigurado: boolean;
  repositorioConfigurado: boolean;
  homologacaoConfigurada: boolean;
  provedorHomologado: boolean;
  prontoParaProcessar: boolean;
  pendenciaCodigo?: string | null;
}

export interface XmlArquivo {
  arquivoId: string;
  documentoId: string;
  filialId: string;
  tentativaId?: string | null;
  modelo?: string | null;
  serie?: number | null;
  numero?: number | null;
  tipo: string;
  hashSha256?: string | null;
  status: 'PENDENTE' | 'ARQUIVANDO' | 'ARQUIVADO' | 'FALHOU';
  tentativasEnvio: number;
  criadoEm: string;
  arquivadoEm?: string | null;
  retencaoAte?: string | null;
  downloadDisponivel: boolean;
}

export interface XmlConsultaResultado {
  inicio: string;
  fim: string;
  status?: string | null;
  limite: number;
  totalRetornado: number;
  itens: XmlArquivo[];
}

export interface LivroCaixaLancamento {
  id: string;
  filialId: string;
  contaFinanceiraId: string;
  contaNome: string;
  contaTipo: string;
  tipo: 'ENTRADA' | 'SAIDA';
  valor: number;
  descricao?: string | null;
  origemTipo?: string | null;
  origemId?: string | null;
  ocorridoEm: string;
}

export interface LivroCaixaResultado {
  inicio: string;
  fim: string;
  totalLancamentos: number;
  totalDisponivel: number;
  pagina: number;
  totalPaginas: number;
  totalEntradas: number;
  totalSaidas: number;
  saldoPeriodo: number;
  lancamentos: LivroCaixaLancamento[];
}

export interface InventarioPosicao {
  inventarioId: string;
  filialId: string;
  descricao: string;
  concluidoEm: string;
  ajustadoEm?: string | null;
  totalItens: number;
  itensDivergentes: number;
}

export interface InventarioConsultaResultado {
  inicio: string;
  fim: string;
  filialId?: string | null;
  totalNaPagina: number;
  totalDisponivel: number;
  pagina: number;
  totalPaginas: number;
  inventarios: InventarioPosicao[];
}

@Injectable({ providedIn: 'root' })
export class ContabilidadeService {
  private readonly baseUrl = '/api/v1/contabilidade';

  constructor(private readonly http: HttpClient) {}

  listarFiliais(): Observable<FilialContabilidade[]> {
    return this.http.get<FilialContabilidade[]>(
      `${this.baseUrl}/filiais`
    );
  }

  consultarFechamento(
    competencia: string,
    filialId?: string
  ): Observable<FechamentoMensal> {
    let params = new HttpParams().set('competencia', competencia);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<FechamentoMensal>(
      `${this.baseUrl}/fechamento-mensal`,
      { params }
    );
  }

  baixarXml(
    competencia: string,
    filialId?: string,
    parte = 1
  ): Observable<HttpResponse<Blob>> {
    const [ano, mes] = competencia.split('-').map(Number);
    const ultimoDia = new Date(ano, mes, 0).getDate();
    let params = new HttpParams()
      .set('inicio', `${competencia}-01`)
      .set('fim', `${competencia}-${String(ultimoDia).padStart(2, '0')}`)
      .set('parte', parte);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get('/api/v1/fiscal/arquivos/exportacao-contabilidade', {
      params,
      observe: 'response',
      responseType: 'blob'
    });
  }

  listarSped(
    competencia: string,
    filialId: string
  ): Observable<SpedExportacao[]> {
    const params = new HttpParams()
      .set('filialId', filialId)
      .set('competenciaInicio', competencia)
      .set('competenciaFim', competencia)
      .set('limite', 100);
    return this.http.get<SpedExportacao[]>(
      `${this.baseUrl}/sped/exportacoes`,
      { params }
    );
  }

  solicitarSped(
    filialId: string,
    tipo: string,
    competencia: string
  ): Observable<SpedExportacao> {
    return this.http.post<SpedExportacao>(
      `${this.baseUrl}/sped/exportacoes`,
      { filialId, tipo, competencia }
    );
  }

  baixarSped(id: string): Observable<HttpResponse<Blob>> {
    return this.http.get(
      `${this.baseUrl}/sped/exportacoes/${id}/arquivo`,
      { observe: 'response', responseType: 'blob' }
    );
  }

  cancelarSped(id: string): Observable<SpedAcaoResultado> {
    return this.http.post<SpedAcaoResultado>(
      `${this.baseUrl}/sped/exportacoes/${id}/cancelamento`,
      {}
    );
  }

  reprocessarSped(id: string): Observable<SpedAcaoResultado> {
    return this.http.post<SpedAcaoResultado>(
      `${this.baseUrl}/sped/exportacoes/${id}/reprocessamento`,
      {}
    );
  }


  consultarProntidaoSped(): Observable<SpedProntidao> {
    return this.http.get<SpedProntidao>(
      `${this.baseUrl}/sped/exportacoes/prontidao`
    );
  }

  listarXml(
    competencia: string,
    filialId?: string,
    status?: string
  ): Observable<XmlConsultaResultado> {
    const [ano, mes] = competencia.split('-').map(Number);
    const ultimoDia = new Date(ano, mes, 0).getDate();
    let params = new HttpParams()
      .set('inicio', `${competencia}-01`)
      .set('fim', `${competencia}-${String(ultimoDia).padStart(2, '0')}`)
      .set('limite', 200);
    if (filialId) params = params.set('filialId', filialId);
    if (status) params = params.set('status', status);
    return this.http.get<XmlConsultaResultado>(
      '/api/v1/fiscal/arquivos',
      { params }
    );
  }

  baixarXmlIndividual(arquivoId: string): Observable<HttpResponse<Blob>> {
    return this.http.get(
      `/api/v1/fiscal/arquivos/${arquivoId}/download`,
      { observe: 'response', responseType: 'blob' }
    );
  }


  consultarLivroCaixa(
    competencia: string,
    filialId?: string,
    pagina = 1
  ): Observable<LivroCaixaResultado> {
    let params = this.parametrosCompetencia(competencia)
      .set('limite', 50)
      .set('pagina', pagina);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<LivroCaixaResultado>(
      `${this.baseUrl}/livro-caixa`,
      { params }
    );
  }

  consultarInventarios(
    competencia: string,
    filialId?: string,
    pagina = 1
  ): Observable<InventarioConsultaResultado> {
    let params = this.parametrosCompetencia(competencia)
      .set('limite', 50)
      .set('pagina', pagina);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<InventarioConsultaResultado>(
      `${this.baseUrl}/inventarios`,
      { params }
    );
  }

  private parametrosCompetencia(competencia: string): HttpParams {
    const [ano, mes] = competencia.split('-').map(Number);
    const ultimoDia = new Date(ano, mes, 0).getDate();
    return new HttpParams()
      .set('inicio', `${competencia}-01`)
      .set('fim', `${competencia}-${String(ultimoDia).padStart(2, '0')}`);
  }

}
