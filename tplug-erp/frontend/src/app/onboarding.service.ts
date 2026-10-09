import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
export interface OnboardingStatus { concluido: boolean; empresaConfigurada: boolean; filialConfigurada: boolean; }
export interface EmpresaInicial {
 id: string; razaoSocial: string; nomeFantasia: string; cnpj: string; email: string;
 telefone: string; segmento: string; quantidadeLojas: number; responsavel: string;
 cep: string; endereco: string; numero: string; complemento: string; bairro: string; cidade: string; uf: string;
 empresaRevisada: boolean; equipeRevisada: boolean;
}
export interface ConfiguracaoInicial {
 nomeUsuario: string; empresa: EmpresaInicial; filial: { id: string; nome: string; cnpj: string } | null;
 fiscalConfigurado: boolean; vendasConfiguradas: boolean;
}
export interface UsuarioInicial { id: string; nome: string; email: string; ativo: boolean; }
export interface PerfilInicial { id: string; nome: string; descricao: string; }
export interface TerminalInicial { id: string; codigo: string; nome: string; serie: number; }
export interface PerfilFiscal { regimeTributario: string; crt: number; ambiente: string; serieNfe: number; serieNfce: number; }
export interface ConfigPdv { exigirJustificativaCancelamento: boolean; exigirAutorizacaoCancelamento: boolean; tamanhoImpressao: string; imprimirCaixa: boolean; imprimirCozinha: boolean; }
@Injectable({ providedIn: 'root' })
export class OnboardingService {
 constructor(private http: HttpClient) {}
 status(): Observable<OnboardingStatus> { return this.http.get<OnboardingStatus>('/api/v1/onboarding'); }
 concluir(nomeFilial: string, cnpj: string): Observable<OnboardingStatus> { return this.http.post<OnboardingStatus>('/api/v1/onboarding/concluir', { nomeFilial, cnpj }); }
 dados(): Observable<ConfiguracaoInicial> { return this.http.get<ConfiguracaoInicial>('/api/v1/onboarding/configuracao'); }
 salvarEmpresa(dados: object): Observable<ConfiguracaoInicial> { return this.http.put<ConfiguracaoInicial>('/api/v1/onboarding/configuracao/empresa', dados); }
 revisarEquipe(): Observable<ConfiguracaoInicial> { return this.http.put<ConfiguracaoInicial>('/api/v1/onboarding/configuracao/equipe-revisada', {}); }
 adiar(): Observable<void> { return this.http.post<void>('/api/v1/onboarding/configuracao/adiar', {}); }
 fiscal(id: string): Observable<PerfilFiscal> { return this.http.get<PerfilFiscal>(`/api/v1/fiscal/perfis-filial/${id}`); }
 salvarFiscal(id: string, dados: PerfilFiscal): Observable<PerfilFiscal> { return this.http.put<PerfilFiscal>(`/api/v1/fiscal/perfis-filial/${id}`, dados); }
 usuarios(): Observable<UsuarioInicial[]> { return this.http.get<UsuarioInicial[]>('/api/v1/usuarios'); }
 perfis(): Observable<PerfilInicial[]> { return this.http.get<PerfilInicial[]>('/api/v1/rbac/perfis'); }
 atribuirPerfil(usuarioId: string, perfilId: string): Observable<void> { return this.http.post<void>(`/api/v1/rbac/usuarios/${usuarioId}/perfis/${perfilId}`, {}); }
 terminais(filialId: string): Observable<TerminalInicial[]> { return this.http.get<TerminalInicial[]>('/api/v1/pdv/terminais', { params: { filialId } }); }
 criarTerminal(filialId: string, dados: object): Observable<TerminalInicial> { return this.http.post<TerminalInicial>('/api/v1/pdv/terminais', { ...dados, filialId }); }
 pdv(id: string): Observable<ConfigPdv> { return this.http.get<ConfigPdv>(`/api/v1/pdv/terminais/${id}/configuracao`); }
 salvarPdv(id: string, dados: ConfigPdv): Observable<ConfigPdv> { return this.http.put<ConfigPdv>(`/api/v1/pdv/terminais/${id}/configuracao`, dados); }
}
