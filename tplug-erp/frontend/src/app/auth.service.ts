import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';

export interface LoginPayload { tenantId: string; email: string; senha: string; }
export interface TokenResponse { accessToken: string; tokenType: string; expiresIn: number; refreshToken: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  static readonly ACCESS_TOKEN_KEY = 'tplug_access_token';
  static readonly REFRESH_TOKEN_KEY = 'tplug_refresh_token';
  static readonly TENANT_KEY = 'tplug_tenant_id';
  private readonly baseUrl = '/api/v1/auth';
  private refreshInFlight$?: Observable<string>;

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginPayload): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl}/login`, payload).pipe(tap(tokens => this.salvarTokens(tokens, payload.tenantId)));
  }

  solicitarRecuperacao(tenantId: string, email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/recuperacao-senha/solicitar`, { tenantId, email });
  }

  confirmarRecuperacao(token: string, novaSenha: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/recuperacao-senha/confirmar`, { token, novaSenha });
  }

  refreshSession(): Observable<string> {
    if (this.refreshInFlight$) return this.refreshInFlight$;
    const refreshToken = this.refreshToken;
    if (!refreshToken) return new Observable<string>(subscriber => subscriber.error(new Error('Refresh token ausente')));
    this.refreshInFlight$ = this.http.post<TokenResponse>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap(tokens => this.salvarTokens(tokens, this.tenantId)), map(tokens => tokens.accessToken),
      shareReplay({ bufferSize: 1, refCount: false }), finalize(() => { this.refreshInFlight$ = undefined; })
    );
    return this.refreshInFlight$;
  }

  logout(): Observable<void> {
    const refreshToken = this.refreshToken;
    if (!refreshToken) { this.limparSessao(); return of(void 0); }
    return this.http.post<void>(`${this.baseUrl}/logout`, { refreshToken }).pipe(catchError(() => of(void 0)), finalize(() => this.limparSessao()));
  }

  limparSessao(): void {
    localStorage.removeItem(AuthService.ACCESS_TOKEN_KEY);
    localStorage.removeItem(AuthService.REFRESH_TOKEN_KEY);
    localStorage.removeItem(AuthService.TENANT_KEY);
  }

  get accessToken(): string | null { return localStorage.getItem(AuthService.ACCESS_TOKEN_KEY); }
  get refreshToken(): string | null { return localStorage.getItem(AuthService.REFRESH_TOKEN_KEY); }
  get tenantId(): string | null { return localStorage.getItem(AuthService.TENANT_KEY); }
  get autenticado(): boolean { return Boolean(this.accessToken && this.refreshToken); }

  private salvarTokens(tokens: TokenResponse, tenantId: string | null): void {
    localStorage.setItem(AuthService.ACCESS_TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(AuthService.REFRESH_TOKEN_KEY, tokens.refreshToken);
    if (tenantId) localStorage.setItem(AuthService.TENANT_KEY, tenantId);
  }
}
