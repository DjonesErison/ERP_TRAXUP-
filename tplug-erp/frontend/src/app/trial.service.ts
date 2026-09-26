import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TrialRequest { empresa:string; razaoSocial?:string; documento:string; responsavel:string; email:string; telefone:string; segmento:string; quantidadeLojas:number; senha:string; aceiteTermos:boolean; }
export interface TrialResponse { trialId:string; tenantId:string; empresaId:string; filialId:string; email:string; expiraEm:string; proximoPasso:string; }

@Injectable({providedIn:'root'})
export class TrialService {
  constructor(private http:HttpClient){}
  criar(payload:TrialRequest):Observable<TrialResponse>{ return this.http.post<TrialResponse>('/api/v1/public/trials',payload); }
}
