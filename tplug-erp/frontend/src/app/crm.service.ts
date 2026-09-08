import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { ClienteFollowUp, ClienteInativo, ClienteInteracao, ClienteRfv } from './crm.models';

@Injectable({ providedIn: 'root' })
export class CrmService {
  private readonly baseUrl = '/api/v1/crm';

  constructor(private readonly http: HttpClient) {}

  carregarPainel(filialId?: string, diasInatividade = 30): Observable<{
    inativos: ClienteInativo[];
    rfv: ClienteRfv[];
    followups: ClienteFollowUp[];
    interacoes: ClienteInteracao[];
  }> {
    return forkJoin({
      inativos: this.listarInativos(filialId, diasInatividade),
      rfv: this.listarRfv(filialId),
      followups: this.listarFollowUps(filialId),
      interacoes: this.listarInteracoes(filialId)
    });
  }

  listarInativos(filialId?: string, diasInatividade = 30): Observable<ClienteInativo[]> {
    let params = new HttpParams().set('diasInatividade', diasInatividade).set('limite', 20);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<ClienteInativo[]>(`${this.baseUrl}/clientes/inativos`, { params });
  }

  listarRfv(filialId?: string): Observable<ClienteRfv[]> {
    let params = new HttpParams().set('limite', 10);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<ClienteRfv[]>(`${this.baseUrl}/clientes/rfv`, { params });
  }

  listarFollowUps(filialId?: string): Observable<ClienteFollowUp[]> {
    let params = new HttpParams().set('status', 'PENDENTE').set('limite', 20);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<ClienteFollowUp[]>(`${this.baseUrl}/followups`, { params });
  }

  listarInteracoes(filialId?: string): Observable<ClienteInteracao[]> {
    let params = new HttpParams().set('limite', 20);
    if (filialId) params = params.set('filialId', filialId);
    return this.http.get<ClienteInteracao[]>(`${this.baseUrl}/interacoes`, { params });
  }
}
