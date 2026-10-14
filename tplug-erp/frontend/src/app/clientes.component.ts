import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Cliente, ClienteContato, ClienteEndereco, ClienteVenda, NovoCliente, NovoContato, NovoEndereco } from './clientes.models';
import { ClientesService } from './clientes.service';

@Component({selector:'app-clientes',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./clientes.component.html',styleUrl:'./clientes.component.css'})
export class ClientesComponent implements OnInit {
 @Input() filialId=''; @Output() sessaoExpirada=new EventEmitter<void>();
 clientes:Cliente[]=[]; selecionado:Cliente|null=null; contatos:ClienteContato[]=[]; enderecos:ClienteEndereco[]=[]; vendas:ClienteVenda[]=[];
 busca=''; situacao='TODOS'; aba:'geral'|'compras'|'contatos'|'enderecos'='geral'; carregando=false; detalhesLoading=false; erro=''; detalheErro=''; mensagem='';
 cadastroAberto=false; salvando=false;
 novo:NovoCliente={tipoPessoa:'FISICA',nomeRazaoSocial:'',nomeFantasia:'',cpfCnpj:'',email:'',telefone:'',cliente:true,fornecedor:false};
 contatoAberto=false; novoContato:NovoContato={nome:'',cargo:'',email:'',telefone:'',principal:false};
 enderecoAberto=false; novoEndereco:NovoEndereco={tipo:'COMERCIAL',logradouro:'',numero:'',complemento:'',bairro:'',cidade:'',uf:'',cep:'',principal:false};
 constructor(private readonly api:ClientesService){}
 ngOnInit(){this.carregar();}
 get filtrados(){const q=this.busca.trim().toLocaleLowerCase('pt-BR');return this.clientes.filter(c=>(this.situacao==='TODOS'||(this.situacao==='ATIVOS'&&c.ativo)||(this.situacao==='INATIVOS'&&!c.ativo))&&(!q||[c.nomeRazaoSocial,c.nomeFantasia,c.cpfCnpj,c.email,c.telefone].some(v=>v?.toLocaleLowerCase('pt-BR').includes(q))));}
 get ativos(){return this.clientes.filter(c=>c.ativo).length;} get inativos(){return this.clientes.length-this.ativos;}
 get totalExibido(){return this.vendas.reduce((s,v)=>s+Number(v.totalLiquido||0),0);}
 get ticketExibido(){return this.vendas.length?this.totalExibido/this.vendas.length:0;}
 get ultimaCompra(){return this.vendas[0]?.criadoEm||null;}
 carregar(){this.carregando=true;this.erro='';this.api.listar().subscribe({next:r=>{this.clientes=r;this.carregando=false;if(this.selecionado){this.selecionado=r.find(c=>c.id===this.selecionado?.id)||null;}},error:e=>this.falha(e,'Não foi possível carregar os clientes.')});}
 selecionar(c:Cliente){this.selecionado=c;this.aba='geral';this.carregarDetalhes();}
 fecharDetalhe(){this.selecionado=null;this.contatos=[];this.enderecos=[];this.vendas=[];}
 carregarDetalhes(){if(!this.selecionado)return;const id=this.selecionado.id;this.detalhesLoading=true;this.detalheErro='';let pendentes=3;const fim=()=>{pendentes--;if(!pendentes)this.detalhesLoading=false;};
   this.api.contatos(id).subscribe({next:r=>{this.contatos=r;fim();},error:e=>{this.tratarDetalhe(e);fim();}});
   this.api.enderecos(id).subscribe({next:r=>{this.enderecos=r;fim();},error:e=>{this.tratarDetalhe(e);fim();}});
   this.api.vendas(id,this.filialId).subscribe({next:r=>{this.vendas=r.conteudo;fim();},error:e=>{this.tratarDetalhe(e);fim();}});
 }
 abrirCadastro(){this.cadastroAberto=true;this.mensagem='';}
 cancelarCadastro(){if(!this.salvando)this.cadastroAberto=false;}
 salvarCliente(){if(this.salvando||!this.novo.nomeRazaoSocial.trim())return;this.salvando=true;this.erro='';
   const dados={...this.novo,nomeRazaoSocial:this.novo.nomeRazaoSocial.trim(),cpfCnpj:this.novo.cpfCnpj?.trim()||undefined,email:this.novo.email?.trim()||undefined,telefone:this.novo.telefone?.trim()||undefined,nomeFantasia:this.novo.nomeFantasia?.trim()||undefined};
   this.api.criar(dados).subscribe({next:c=>{this.salvando=false;this.cadastroAberto=false;this.novo={tipoPessoa:'FISICA',nomeRazaoSocial:'',nomeFantasia:'',cpfCnpj:'',email:'',telefone:'',cliente:true,fornecedor:false};this.mensagem='Cliente cadastrado com sucesso.';this.carregar();this.selecionar(c);},error:e=>{this.salvando=false;this.erro=this.mensagemErro(e,'Não foi possível cadastrar o cliente.');}});
 }
 salvarContato(){if(!this.selecionado||!this.novoContato.nome.trim())return;this.api.criarContato(this.selecionado.id,this.novoContato).subscribe({next:c=>{this.contatos=[...this.contatos,c];this.contatoAberto=false;this.novoContato={nome:'',cargo:'',email:'',telefone:'',principal:false};},error:e=>this.detalheErro=this.mensagemErro(e,'Não foi possível adicionar o contato.')});}
 salvarEndereco(){if(!this.selecionado||!this.novoEndereco.logradouro.trim()||!this.novoEndereco.cidade.trim()||this.novoEndereco.uf.trim().length!==2)return;this.novoEndereco.uf=this.novoEndereco.uf.toUpperCase();this.api.criarEndereco(this.selecionado.id,this.novoEndereco).subscribe({next:c=>{this.enderecos=[...this.enderecos,c];this.enderecoAberto=false;this.novoEndereco={tipo:'COMERCIAL',logradouro:'',numero:'',complemento:'',bairro:'',cidade:'',uf:'',cep:'',principal:false};},error:e=>this.detalheErro=this.mensagemErro(e,'Não foi possível adicionar o endereço.')});}
 desativar(){if(!this.selecionado||!this.selecionado.ativo||!confirm('Desativar este cliente?'))return;this.api.desativar(this.selecionado.id).subscribe({next:c=>{this.selecionado=c;this.mensagem='Cliente desativado.';this.carregar();},error:e=>this.detalheErro=this.mensagemErro(e,'Não foi possível desativar o cliente.')});}
 iniciais(c:Cliente){return c.nomeRazaoSocial.split(/\s+/).slice(0,2).map(p=>p[0]).join('').toUpperCase();}
 documento(v?:string|null){if(!v)return 'Não informado';const n=v.replace(/\D/g,'');return n.length===11?n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/,'$1.$2.$3-$4'):n.length===14?n.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/,'$1.$2.$3/$4-$5'):v;}
 private tratarDetalhe(e:any){if(e?.status===401){this.sessaoExpirada.emit();return;}if(e?.status!==403)this.detalheErro='Algumas informações da visão 360 não puderam ser carregadas.';}
 private falha(e:any,padrao:string){this.carregando=false;if(e?.status===401){this.sessaoExpirada.emit();return;}this.erro=this.mensagemErro(e,padrao);}
 private mensagemErro(e:any,padrao:string){return e?.status===403?'Seu perfil não possui permissão para esta ação.':e?.status===409?'Já existe um cliente com este CPF/CNPJ.':e?.error?.message||padrao;}
}