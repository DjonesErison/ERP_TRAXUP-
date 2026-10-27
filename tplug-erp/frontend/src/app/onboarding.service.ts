import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface OnboardingStatus { concluido: boolean; empresaConfigurada: boolean; filialConfigurada: boolean; }
export interface EmpresaInicial {
 id: string; codigoEmpresa: string; razaoSocial: string; nomeFantasia: string; cnpj: string; email: string;
 telefone: string; segmento: string; quantidadeLojas: number; responsavel: string;
 cep: string; endereco: string; numero: string; complemento: string; bairro: string; cidade: string; uf: string;
 empresaRevisada: boolean; equipeRevisada: boolean;
}
export interface UnidadeOnboarding {
 id?: string; codigo: string; nome: string; cnpj: string; matriz: boolean;
 cep: string; endereco: string; numero: string; complemento: string; bairro: string; cidade: string; uf: string;
 estoqueAtivo: boolean; pdvAtivo: boolean; fiscalAtivo: boolean; configuracaoConcluida?: boolean;
}
export interface ConfiguracaoInicial {
 nomeUsuario: string; empresa: EmpresaInicial; filial: { id: string; nome: string; cnpj: string } | null;
 unidades: UnidadeOnboarding[]; fiscalConfigurado: boolean; vendasConfiguradas: boolean;
}
export interface EnderecoCep { cep: string; logradouro: string; complemento: string; bairro: string; localidade: string; uf: string; erro?: boolean; }

@Injectable({ providedIn: 'root' })
export class OnboardingService {
 constructor(private http: HttpClient) {}
 status(): Observable<OnboardingStatus> { return this.http.get<OnboardingStatus>('/api/v1/onboarding'); }
 concluir(nomeFilial: string, cnpj: string): Observable<OnboardingStatus> { return this.http.post<OnboardingStatus>('/api/v1/onboarding/concluir', { nomeFilial, cnpj }); }
 dados(): Observable<ConfiguracaoInicial> { return this.http.get<ConfiguracaoInicial>('/api/v1/onboarding/configuracao'); }
 salvarEmpresa(dados: object): Observable<ConfiguracaoInicial> { return this.http.put<ConfiguracaoInicial>('/api/v1/onboarding/configuracao/empresa', dados); }
 salvarUnidades(unidades: UnidadeOnboarding[]): Observable<ConfiguracaoInicial> { return this.http.put<ConfiguracaoInicial>('/api/v1/onboarding/configuracao/unidades', { unidades }); }
 buscarCep(cep: string): Observable<EnderecoCep> { return this.http.get<EnderecoCep>(`/api/v1/cep/${cep.replace(/\D/g, '')}`); }
 adiar(): Observable<void> { return this.http.post<void>('/api/v1/onboarding/configuracao/adiar', {}); }
}
