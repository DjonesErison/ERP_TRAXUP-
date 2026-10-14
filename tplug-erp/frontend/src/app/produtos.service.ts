import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface ProdutoCadastro {
 codigo: string; descricao: string; grupo: string; ncm: string;
 vendaPrc: number; compraPrc: number; codigoBarra: string; unidade: string;
}
export interface Produto extends ProdutoCadastro {
 id: string; ativo: boolean; criadoEm: string; atualizadoEm: string;
}
@Injectable({providedIn:'root'})
export class ProdutosService {
 private readonly url='/api/v1/produtos';
 constructor(private readonly http: HttpClient) {}
 listar() { return this.http.get<Produto[]>(this.url); }
 criar(dados:ProdutoCadastro) { return this.http.post<Produto>(this.url,dados); }
 desativar(id:string) { return this.http.patch<Produto>(`${this.url}/${encodeURIComponent(id)}/desativar`,{}); }
}
