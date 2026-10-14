import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Cliente, ClienteContato, ClienteEndereco, ClienteVendaPagina, NovoCliente, NovoContato, NovoEndereco } from './clientes.models';

@Injectable({providedIn:'root'})
export class ClientesService {
  private readonly base='/api/v1/pessoas';
  constructor(private readonly http:HttpClient){}
  listar():Observable<Cliente[]>{ return this.http.get<Cliente[]>(this.base,{params:new HttpParams().set('papel','CLIENTE')}); }
  criar(dados:NovoCliente):Observable<Cliente>{ return this.http.post<Cliente>(this.base,dados); }
  desativar(id:string):Observable<Cliente>{ return this.http.patch<Cliente>(`${this.base}/${id}/desativar`,{}); }
  contatos(id:string):Observable<ClienteContato[]>{ return this.http.get<ClienteContato[]>(`${this.base}/${id}/contatos`); }
  criarContato(id:string,dados:NovoContato):Observable<ClienteContato>{ return this.http.post<ClienteContato>(`${this.base}/${id}/contatos`,dados); }
  enderecos(id:string):Observable<ClienteEndereco[]>{ return this.http.get<ClienteEndereco[]>(`${this.base}/${id}/enderecos`); }
  criarEndereco(id:string,dados:NovoEndereco):Observable<ClienteEndereco>{ return this.http.post<ClienteEndereco>(`${this.base}/${id}/enderecos`,dados); }
  vendas(clienteId:string,filialId?:string):Observable<ClienteVendaPagina>{
    let p=new HttpParams().set('pagina',0).set('tamanho',50).set('clienteId',clienteId);
    if(filialId) p=p.set('filialId',filialId);
    return this.http.get<ClienteVendaPagina>('/api/v1/vendas/pedidos/recentes',{params:p});
  }
}