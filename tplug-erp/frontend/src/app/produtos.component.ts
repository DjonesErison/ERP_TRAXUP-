import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostListener, OnDestroy, OnInit, Output } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { Produto, ProdutoCadastro, ProdutosService } from './produtos.service';

@Component({selector:'app-produtos',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./produtos.component.html',styleUrl:'./produtos.component.css'})
export class ProdutosComponent implements OnInit,OnDestroy {
 @Output() sessaoExpirada=new EventEmitter<void>();
 produtos:Produto[]=[]; ativo?:Produto; query=''; grupo=''; situacao=''; ordem='descricao';
 selecionados=new Set<string>(); loading=false; salvando=false; alterando=false; modal=false;
 erro=''; erroCadastro=''; mensagem=''; aba='Geral'; confirmar?:Produto;
 draft:ProdutoCadastro=this.novoDraft(); private requests=new Subscription();
 constructor(private readonly service:ProdutosService,private readonly auth:AuthService) {}
 ngOnInit(){this.carregar();} ngOnDestroy(){this.requests.unsubscribe();}
 private novoDraft():ProdutoCadastro{return {codigo:'',descricao:'',grupo:'',ncm:'',vendaPrc:0,compraPrc:0,codigoBarra:'',unidade:'UN'};}
 get grupos(){return [...new Set(this.produtos.map(p=>p.grupo).filter(Boolean))].sort();}
 get filtrados(){const q=this.query.trim().toLocaleLowerCase('pt-BR');return this.produtos.filter(p=>(!q||[p.codigo,p.descricao,p.codigoBarra,p.grupo].some(v=>(v||'').toLocaleLowerCase('pt-BR').includes(q)))&&(!this.grupo||p.grupo===this.grupo)&&(!this.situacao||(this.situacao==='ativo'?p.ativo:!p.ativo))).sort((a,b)=>this.ordem==='recentes'?new Date(b.criadoEm).getTime()-new Date(a.criadoEm).getTime():a.descricao.localeCompare(b.descricao,'pt-BR'));}
 get quantidadeAtivos(){return this.produtos.filter(p=>p.ativo).length;}
 get todosSelecionados(){return !!this.filtrados.length&&this.filtrados.every(p=>this.selecionados.has(p.id));}
 carregar(){if(this.loading||this.alterando)return;this.loading=true;this.erro='';this.produtos=[];this.ativo=undefined;this.selecionados.clear();this.requests.add(this.service.listar().subscribe({next:ps=>{this.produtos=ps;this.loading=false;},error:e=>{this.loading=false;this.erro=this.falha(e,'consultar os produtos');}}));}
 private falha(e:any,acao:string){if(!this.auth.autenticado){this.sessaoExpirada.emit();return 'Sua sessão expirou.';}return e.status===403?'Seu perfil não tem permissão para '+acao+'.':'Não foi possível '+acao+'. Tente novamente.';}
 limpar(){this.query='';this.grupo='';this.situacao='';this.selecionados.clear();}
 marcar(p:Produto){this.selecionados.has(p.id)?this.selecionados.delete(p.id):this.selecionados.add(p.id);}
 marcarTodos(){const marcar=!this.todosSelecionados;for(const p of this.filtrados)marcar?this.selecionados.add(p.id):this.selecionados.delete(p.id);}
 selecionar(p:Produto){this.ativo=p;this.aba='Geral';}
 margem(p:Produto){return Number(p.compraPrc)>0?(Number(p.vendaPrc)-Number(p.compraPrc))/Number(p.compraPrc)*100:null;}
 novo(){this.draft=this.novoDraft();this.erroCadastro='';this.mensagem='';this.modal=true;}
 fechar(){if(!this.salvando)this.modal=false;}
 @HostListener('document:keydown.escape') escape(){this.fechar();if(!this.alterando)this.confirmar=undefined;}
 salvar(form:NgForm){if(this.salvando)return;this.erroCadastro='';const d=this.draft;
 if(form.invalid||!d.codigo.trim()||!d.descricao.trim()||!d.unidade.trim()||d.compraPrc==null||d.vendaPrc==null||!Number.isFinite(Number(d.compraPrc))||!Number.isFinite(Number(d.vendaPrc))||Number(d.compraPrc)<0||Number(d.vendaPrc)<0){form.control.markAllAsTouched();this.erroCadastro='Preencha código, descrição, unidade e preços válidos.';return;}
 if(d.ncm&& !/^\d{8}$/.test(d.ncm)){this.erroCadastro='Informe um NCM com 8 dígitos ou deixe o campo vazio.';return;}
 this.salvando=true;const payload={...d,codigo:d.codigo.trim(),descricao:d.descricao.trim(),grupo:d.grupo.trim(),unidade:d.unidade.trim().toUpperCase(),codigoBarra:d.codigoBarra.trim(),ncm:d.ncm.trim(),compraPrc:Number(d.compraPrc),vendaPrc:Number(d.vendaPrc)};
 this.requests.add(this.service.criar(payload).subscribe({next:p=>{this.produtos=[...this.produtos,p];this.ativo=p;this.aba='Geral';this.salvando=false;this.modal=false;this.limpar();this.mensagem='Produto cadastrado com sucesso.';},error:e=>{this.salvando=false;this.erroCadastro=e.status===409?'Já existe um produto com esse código.':e.status===400?'Dados recusados. Revise o cadastro.':this.falha(e,'cadastrar produtos');}}));
 }
 desativar(){const p=this.confirmar;if(!p||this.alterando)return;this.alterando=true;this.erro='';this.requests.add(this.service.desativar(p.id).subscribe({next:r=>{this.produtos=this.produtos.map(x=>x.id===r.id?r:x);if(this.ativo?.id===r.id)this.ativo=r;this.confirmar=undefined;this.alterando=false;this.mensagem='Produto desativado. O histórico foi preservado.';},error:e=>{this.alterando=false;this.confirmar=undefined;this.erro=this.falha(e,'desativar produtos');}}));}
 exportar(){const ps=this.filtrados.filter(p=>!this.selecionados.size||this.selecionados.has(p.id));
 const cell=(v:unknown)=>{let s=String(v??'');if(/^[\s]*[=+\-@]/.test(s))s="'"+s;return '"'+s.replace(/"/g,'""')+'"';};
 const rows=[['Código','Descrição','Código de barras','Grupo','Unidade','Custo','Venda','Situação'],...ps.map(p=>[p.codigo,p.descricao,p.codigoBarra,p.grupo,p.unidade,p.compraPrc,p.vendaPrc,p.ativo?'Ativo':'Inativo'])];
 const url=URL.createObjectURL(new Blob(['\uFEFF'+rows.map(r=>r.map(cell).join(';')).join('\r\n')],{type:'text/csv;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download='produtos-traxup.csv';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
 }
}
