import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const rotaAuth = request.url.includes('/api/v1/auth/');
  const token = auth.accessToken;
  const autenticada = token && !rotaAuth
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(autenticada).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status !== 401 || rotaAuth || !auth.refreshToken) {
        return throwError(() => erro);
      }
      return auth.refreshSession().pipe(
        switchMap((novoToken) => next(request.clone({
          setHeaders: { Authorization: `Bearer ${novoToken}` }
        }))),
        catchError((refreshErro) => {
          auth.limparSessao();
          return throwError(() => refreshErro);
        })
      );
    })
  );
};
