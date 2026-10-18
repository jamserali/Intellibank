import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
 const router=inject(Router);
 const token=sessionStorage.getItem('intellibank-access-token');
 const apiRequest=request.url.startsWith('/api/');
 const publicAuth=request.url.endsWith('/auth/login') || request.url.endsWith('/auth/register');
 const outgoing=token && apiRequest && !publicAuth ? request.clone({setHeaders:{Authorization:`Bearer ${token}`}}) : request;
 return next(outgoing).pipe(catchError(error=>{
 if(error.status===401 && apiRequest && !publicAuth){
 sessionStorage.removeItem('intellibank-access-token');sessionStorage.removeItem('intellibank-role');
 if(!request.url.endsWith('/auth/me')) router.navigateByUrl(router.url.startsWith('/admin')?'/admin/login':'/signin');
 }
 return throwError(()=>error);
 }));
};
