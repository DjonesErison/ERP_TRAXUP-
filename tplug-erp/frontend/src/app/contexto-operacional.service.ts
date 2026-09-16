import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface FilialPermitida {
  id: string;
  empresaId: string;
  nome: string;
  cnpj?: string | null;
}

@Injectable({ providedIn: 'root' })
export class ContextoOperacionalService {
  static readonly FILIAL_KEY = 'traxup_filial_ativa';
  private readonly baseUrl = '/api/v1/me';

  constructor(private readonly http: HttpClient) {}

  listarFiliais(): Observable<FilialPermitida[]> {
    return this.http.get<FilialPermitida[]>(`${this.baseUrl}/filiais`);
  }

  selecionarFilial(filial: FilialPermitida): void {
    localStorage.setItem(ContextoOperacionalService.FILIAL_KEY, JSON.stringify(filial));
  }

  limpar(): void {
    localStorage.removeItem(ContextoOperacionalService.FILIAL_KEY);
  }

  get filialAtiva(): FilialPermitida | null {
    try {
      const raw = localStorage.getItem(ContextoOperacionalService.FILIAL_KEY);
      return raw ? JSON.parse(raw) as FilialPermitida : null;
    } catch {
      this.limpar();
      return null;
    }
  }
}
