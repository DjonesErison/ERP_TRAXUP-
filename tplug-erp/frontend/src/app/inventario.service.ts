import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InventarioContagem, InventarioDivergencia, InventarioItemLeitura, InventarioSessao, InventarioTipoItem } from './inventario.models';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private readonly baseUrl = '/api/v1/inventarios';

  constructor(private readonly http: HttpClient) {}

  listar(filialId?: string, status?: string, limite = 100): Observable<InventarioSessao[]> {
    let params = new HttpParams().set('limite', limite);
    if (filialId) params = params.set('filialId', filialId);
    if (status) params = params.set('status', status);
    return this.http.get<InventarioSessao[]>(this.baseUrl, { params });
  }

  buscar(id: string): Observable<InventarioSessao> {
    return this.http.get<InventarioSessao>(`${this.baseUrl}/${id}`);
  }

  criar(filialId: string, descricao?: string, contagemCega = false): Observable<InventarioSessao> {
    return this.http.post<InventarioSessao>(this.baseUrl, {
      filialId,
      descricao: descricao?.trim() || null,
      contagemCega
    });
  }

  localizarPorCodigoBarras(codigo: string): Observable<InventarioItemLeitura> {
    const params = new HttpParams().set('codigo', codigo.trim());
    return this.http.get<InventarioItemLeitura>(`${this.baseUrl}/itens/por-codigo-barras`, { params });
  }

  registrarContagem(inventarioId: string, tipoItem: InventarioTipoItem, itemId: string, quantidadeContada: number): Observable<InventarioContagem> {
    return this.http.post<InventarioContagem>(`${this.baseUrl}/${inventarioId}/contagens`, {
      tipoItem,
      itemId,
      quantidadeContada
    });
  }

  listarContagens(inventarioId: string, limite = 100): Observable<InventarioContagem[]> {
    const params = new HttpParams().set('limite', limite);
    return this.http.get<InventarioContagem[]>(`${this.baseUrl}/${inventarioId}/contagens`, { params });
  }

  listarDivergencias(inventarioId: string, limite = 100): Observable<InventarioDivergencia[]> {
    const params = new HttpParams().set('limite', limite);
    return this.http.get<InventarioDivergencia[]>(`${this.baseUrl}/${inventarioId}/divergencias`, { params });
  }

  concluir(inventarioId: string): Observable<InventarioSessao> {
    return this.http.post<InventarioSessao>(`${this.baseUrl}/${inventarioId}/concluir`, {});
  }

  cancelar(inventarioId: string): Observable<InventarioSessao> {
    return this.http.post<InventarioSessao>(`${this.baseUrl}/${inventarioId}/cancelar`, {});
  }

  ajustarEstoque(inventarioId: string): Observable<InventarioSessao> {
    return this.http.post<InventarioSessao>(`${this.baseUrl}/${inventarioId}/ajustar-estoque`, {});
  }
}
