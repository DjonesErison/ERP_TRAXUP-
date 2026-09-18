import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
@Injectable({providedIn:'root'})
export class AtivacaoAdminService {
 constructor(private readonly http:HttpClient){}
 confirmar(token:string,novaSenha:string):Observable<void>{return this.http.post<void>('/api/v1/auth/ativacao-admin/confirmar',{token,novaSenha});}
}
