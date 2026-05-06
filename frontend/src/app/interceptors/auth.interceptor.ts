import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // Get JWT token from localStorage
  const token = localStorage.getItem('codesync_token');

  // Clone request and add Authorization header if token exists
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expired or invalid — clear storage and redirect to login
        localStorage.removeItem('codesync_token');
        localStorage.removeItem('codesync_user');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
