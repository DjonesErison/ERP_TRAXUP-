import {CommonModule} from '@angular/common';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ContextoOperacionalService, FilialPermitida} from './contexto-operacional.service';
import {ConfiguracaoInicial, OnboardingService, UnidadeOnboarding} from './onboarding.service';

type Secao = 'empresa'|'filiais'|'usuarios'|'fiscal'|'pdv'|'integracoes';
interface Item { id:string; nome:string; descricao?:string; email?:string; chave?:string; ativo?:boolean }
interface Terminal {id:string; nome:string; codigo:string; ativo:boolean}
interface Pdv {exigirJustificativaCancelamento:boolean; exigirAutorizacaoCancelamento:boolean; tamanhoImpressao:string; imprimirCaixa:boolean; imprimirCozinha:boolean}
interface Fiscal {regimeTributario:string; crt:number; ambiente:string; serieNfe:number; serieNfce:number}
@Component({selector:'app-configuracoes',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./configuracoes.component.html',styleUrl:'./configuracoes.component.css'})
export class ConfiguracoesComponent implements OnInit {
 @Input() filial:FilialPermitida|null=null;
 @Output() filialAtualizada=new EventEmitter<FilialPermitida>();
 readonly secoes:{id:Secao;nome:string;descricao:string;icone:string}[]=[
  {id:'empresa',nome:'Dados da empresa',descricao:'Cadastro, endereço e contato',icone:'▣'},
  {id:'filiais',nome:'Matriz e filiais',descricao:'Unidades e acesso',icone:'⌂'},
  {id:'usuarios',nome:'Usuários e permissões',descricao:'Equipe, perfis e acessos',icone:'♙'},
  {id:'fiscal',nome:'Configurações fiscais',descricao:'Regime, séries e ambiente',icone:'▤'},
  {id:'pdv',nome:'PDV e impressão',descricao:'Terminais e comprovantes',icone:'▧'},
  {id:'integracoes',nome:'Integrações',descricao:'Conexões e serviços',icone:'⇄'}];
 secao:Secao='empresa'; dados?:ConfiguracaoInicial; loading=false; salvando=false; erro=''; mensagem='';
 usuarios:Item[]=[]; perfis:Item[]=[]; permissoes:Item[]=[]; usuarioId=''; perfilId=''; permissaoId=''; filiaisEquipe:Item[]=[]; filiaisUsuario:string[]=[]; acessoCarregado=false; novoPerfil={nome:'',descricao:''}; novoUsuario={nome:'',email:'',senha:''};
 terminais:Terminal[]=[]; terminalId=''; novoTerminal={codigo:'',nome:'',serie:1}; pdv:Pdv|null=null; fiscal:Fiscal|null=null; filialLista:FilialPermitida[]=[];unidades:UnidadeOnboarding[]=[];unidadeEditada:UnidadeOnboarding|null=null;
 readonly ufs=['AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'];
 constructor(private http:HttpClient,private onboarding:OnboardingService,private contexto:ContextoOperacionalService){}
 ngOnInit(){this.abrir('empresa');}
 abrir(s:Secao){this.secao=s;this.erro='';this.mensagem='';this.loading=false;
  if(s==='empresa'){this.loading=true;this.onboarding.dados().subscribe({next:d=>{this.dados=d;this.loading=false},error:e=>this.falha(e)});}
  if(s==='filiais'){this.loading=true;this.contexto.listarFiliais().subscribe({next:d=>{this.filialLista=d;this.onboarding.dados().subscribe({next:x=>{this.unidades=x.unidades;this.unidadeEditada=null;this.loading=false},error:e=>this.falha(e)})},error:e=>this.falha(e)});}
  if(s==='usuarios')this.carregarEquipe();
  if(s==='fiscal')this.carregarFiscal();
  if(s==='pdv')this.carregarTerminais();
 }
 private falha(e:HttpErrorResponse){this.loading=false;this.salvando=false;this.erro=e.status===403?'Seu usuário não tem permissão para esta operação.':e.status===404?'Dados não cadastrados para esta filial.':e.error?.message||'Não foi possível carregar ou salvar. Tente novamente.';}
 salvarEmpresa(){if(!this.dados||this.salvando)return;this.salvando=true;this.erro='';const matriz=this.dados.unidades.find(u=>u.matriz)||this.dados.filial;this.http.put<ConfiguracaoInicial>('/api/v1/onboarding/configuracao/empresa/dados',{...this.dados.empresa,nomeFilial:matriz?.nome||'Matriz'}).subscribe({next:d=>{this.dados=d;this.salvando=false;this.mensagem='Dados da empresa salvos.'},error:e=>this.falha(e)});}
 editarFilial(f:FilialPermitida){const u=this.unidades.find(x=>x.id===f.id);if(!u){this.erro='Não foi possível carregar os dados desta filial.';return;}this.unidadeEditada={...u};this.erro='';this.mensagem='';}
 salvarFilial(){const u=this.unidadeEditada;if(!u?.id||this.salvando)return;this.salvando=true;this.erro='';this.http.put<ConfiguracaoInicial>(`/api/v1/onboarding/configuracao/filiais/${u.id}/dados`,u).subscribe({next:d=>{this.unidades=d.unidades;this.filialLista=this.filialLista.map(f=>f.id===u.id?{...f,nome:u.nome,cidade:u.cidade,uf:u.uf}:f);const atual=this.filial;if(atual&&atual.id===u.id){const atualizada={...atual,nome:u.nome,cidade:u.cidade,uf:u.uf};this.filial=atualizada;this.contexto.selecionarFilial(atualizada);this.filialAtualizada.emit(atualizada);}this.unidadeEditada=null;this.salvando=false;this.mensagem='Dados da filial salvos.'},error:e=>this.falha(e)});}
 carregarEquipe(){this.loading=true;this.http.get<Item[]>('/api/v1/usuarios').subscribe({next:u=>{this.usuarios=u;this.http.get<Item[]>('/api/v1/rbac/perfis').subscribe({next:p=>{this.perfis=p;this.http.get<Item[]>('/api/v1/rbac/permissoes').subscribe({next:x=>{this.permissoes=x;this.loading=false},error:e=>this.falha(e)})},error:e=>this.falha(e)})},error:e=>this.falha(e)});}
 criarUsuario(){if(this.salvando)return;this.salvando=true;this.erro='';this.http.post<Item>('/api/v1/usuarios',this.novoUsuario).subscribe({next:u=>{this.usuarios=[...this.usuarios,u];this.novoUsuario={nome:'',email:'',senha:''};this.salvando=false;this.mensagem='Usuário criado. Atribua um perfil e acesso às filiais para habilitar a operação.'},error:e=>{this.novoUsuario.senha='';this.falha(e)}});}
 criarPerfil(){if(this.salvando)return;this.salvando=true;this.erro='';this.http.post<Item>('/api/v1/rbac/perfis',this.novoPerfil).subscribe({next:p=>{this.perfis=[...this.perfis,p];this.novoPerfil={nome:'',descricao:''};this.salvando=false;this.mensagem='Perfil criado.'},error:e=>this.falha(e)});}
 atribuirPerfil(){if(!this.usuarioId||!this.perfilId||this.salvando)return;this.salvando=true;this.erro='';this.http.post(`/api/v1/rbac/usuarios/${this.usuarioId}/perfis/${this.perfilId}`,{}).subscribe({next:()=>{this.salvando=false;this.mensagem='Perfil atribuído ao usuário.'},error:e=>this.falha(e)});}
 atribuirPermissao(){if(!this.perfilId||!this.permissaoId||this.salvando)return;this.salvando=true;this.erro='';this.http.post(`/api/v1/rbac/perfis/${this.perfilId}/permissoes/${this.permissaoId}`,{}).subscribe({next:()=>{this.salvando=false;this.mensagem='Permissão atribuída ao perfil.'},error:e=>this.falha(e)});}
 carregarAcesso(){this.acessoCarregado=false;this.filiaisUsuario=[];if(!this.usuarioId){this.loading=false;return;}const id=this.usuarioId;this.loading=true;this.http.get<Item[]>('/api/v1/filiais').subscribe({next:fs=>{if(id!==this.usuarioId)return;this.filiaisEquipe=fs;this.http.get<{filialIds:string[]}>(`/api/v1/rbac/usuarios/${id}/filiais`).subscribe({next:a=>{if(id!==this.usuarioId)return;this.filiaisUsuario=a.filialIds;this.acessoCarregado=true;this.loading=false},error:e=>{if(id===this.usuarioId)this.falha(e)}})},error:e=>{if(id===this.usuarioId)this.falha(e)}});}
 alternarFilial(id:string){this.filiaisUsuario=this.filiaisUsuario.includes(id)?this.filiaisUsuario.filter(x=>x!==id):[...this.filiaisUsuario,id];}
 salvarAcesso(){if(!this.usuarioId||!this.acessoCarregado||this.salvando)return;this.salvando=true;this.erro='';this.http.put(`/api/v1/rbac/usuarios/${this.usuarioId}/filiais`,{filialIds:this.filiaisUsuario}).subscribe({next:()=>{this.salvando=false;this.mensagem='Acesso às filiais atualizado.'},error:e=>this.falha(e)});}
 carregarFiscal(){if(!this.filial?.id){this.erro='Selecione uma filial para configurar o fiscal.';return}this.loading=true;this.http.get<Fiscal>(`/api/v1/fiscal/perfis-filial/${this.filial.id}`).subscribe({next:f=>{this.fiscal=f;this.loading=false},error:e=>{if(e.status===404){this.fiscal=null;this.loading=false;this.mensagem='Ainda não existe configuração fiscal para esta filial.'}else this.falha(e)}});}
 novoFiscal(){this.fiscal={regimeTributario:'SIMPLES_NACIONAL',crt:1,ambiente:'HOMOLOGACAO',serieNfe:1,serieNfce:1};this.mensagem='Revise os dados fiscais antes de salvar.';}
 salvarFiscal(){if(!this.filial?.id||!this.fiscal||this.salvando)return;this.salvando=true;this.erro='';this.http.put<Fiscal>(`/api/v1/fiscal/perfis-filial/${this.filial.id}`,this.fiscal).subscribe({next:f=>{this.fiscal=f;this.salvando=false;this.mensagem='Configuração fiscal salva.'},error:e=>this.falha(e)});}
 carregarTerminais(){if(!this.filial?.id){this.erro='Selecione uma filial para configurar o PDV.';return}this.loading=true;this.http.get<Terminal[]>(`/api/v1/pdv/terminais?filialId=${encodeURIComponent(this.filial.id)}`).subscribe({next:t=>{this.terminais=t;this.loading=false;this.terminalId='';this.pdv=null},error:e=>this.falha(e)});}
 criarTerminal(){if(!this.filial?.id||this.salvando)return;this.salvando=true;this.erro='';this.http.post<Terminal>('/api/v1/pdv/terminais',{...this.novoTerminal,filialId:this.filial.id}).subscribe({next:t=>{this.terminais=[...this.terminais,t];this.novoTerminal={codigo:'',nome:'',serie:1};this.salvando=false;this.mensagem='Terminal cadastrado.'},error:e=>this.falha(e)});}
 selecionarTerminal(){this.pdv=null;if(!this.terminalId)return;this.loading=true;this.http.get<Pdv>(`/api/v1/pdv/terminais/${this.terminalId}/configuracao`).subscribe({next:p=>{this.pdv=p;this.loading=false},error:e=>{if(e.status===404){this.pdv={exigirJustificativaCancelamento:true,exigirAutorizacaoCancelamento:false,tamanhoImpressao:'MEDIA',imprimirCaixa:true,imprimirCozinha:false};this.loading=false;this.mensagem='Revise as opções para este terminal antes de salvar.'}else this.falha(e)}});}
 salvarPdv(){if(!this.terminalId||!this.pdv||this.salvando)return;this.salvando=true;this.erro='';this.http.put<Pdv>(`/api/v1/pdv/terminais/${this.terminalId}/configuracao`,this.pdv).subscribe({next:p=>{this.pdv=p;this.salvando=false;this.mensagem='Configuração de PDV e impressão salva.'},error:e=>this.falha(e)});}
}
