import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface TrialCadastroPayload {
  nomeCompleto: string; nomeEmpresa: string; razaoSocial: string; documento: string;
  telefone: string; email: string; segmento?: string; quantidadeLojas: number;
  aceitouTermos: boolean; termosVersao: string; idempotencyKey: string;
}
export interface TrialCadastroResponse { trialId: string; tenantId: string; expiraEm: string; status: string; proximoPasso: string; }

@Injectable({ providedIn: 'root' })
export class TrialService {
  constructor(private readonly http: HttpClient) {}
  cadastrar(payload: TrialCadastroPayload): Observable<TrialCadastroResponse> {
    return this.http.post<TrialCadastroResponse>('/api/public/trials', payload);
  }
}
