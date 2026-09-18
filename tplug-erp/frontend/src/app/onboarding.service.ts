import { HttpClient } from '@angular/common/http'; import { Injectable } from '@angular/core'; import { Observable } from 'rxjs';
export interface OnboardingStatus{concluido:boolean;empresaConfigurada:boolean;filialConfigurada:boolean}
@Injectable({providedIn:'root'}) export class OnboardingService{constructor(private http:HttpClient){} status():Observable<OnboardingStatus>{return this.http.get<OnboardingStatus>('/api/v1/onboarding')} concluir(nomeFilial:string,cnpj:string):Observable<OnboardingStatus>{return this.http.post<OnboardingStatus>('/api/v1/onboarding/concluir',{nomeFilial,cnpj})}}
