import { HttpClient, HttpParams } from '@angular/common/http';
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
  saldo: number;
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
}
