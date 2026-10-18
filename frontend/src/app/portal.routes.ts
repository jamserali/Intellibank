import { inject } from '@angular/core';
import { Routes, Router, CanActivateFn, ResolveFn } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { catchError, map, of } from 'rxjs';
import { ApiService } from './api.service';
import { PortalComponent } from './portal.component';
import { AppComponent } from './app.component';
const guard: CanActivateFn=(route)=>{
 const api=inject(ApiService), router=inject(Router), http=inject(HttpClient);
 return http.get<{role:string}>('/api/v1/auth/me').pipe(map(user=>user.role===route.data['role']?true:router.parseUrl('/unauthorized')),catchError(()=>{api.clearAccessToken();return of(router.parseUrl(route.data['role']==='ADMIN'?'/admin/login':'/signin'));}));
};
const profile: ResolveFn<unknown>=()=>inject(HttpClient).get('/api/v1/auth/me');
export const routes: Routes=[
 {path:'',component:PortalComponent,data:{page:'home'}},
 {path:'signin',component:PortalComponent,data:{page:'signin'}},
 {path:'signup',component:PortalComponent,data:{page:'signup'}},
 {path:'admin/login',component:PortalComponent,data:{page:'admin-login'}},
 {path:'dashboard',component:PortalComponent,canActivate:[guard],resolve:{profile},data:{page:'dashboard',role:'CUSTOMER'}},
 {path:'admin',component:AppComponent,canActivate:[guard],data:{role:'ADMIN'}},
 {path:'privacy',component:PortalComponent,data:{page:'privacy'}},
 {path:'terms',component:PortalComponent,data:{page:'terms'}},
 {path:'forgot-password',component:PortalComponent,data:{page:'forgot-password'}},
 {path:'unauthorized',component:PortalComponent,data:{page:'unauthorized'}},
 {path:'**',component:PortalComponent,data:{page:'not-found'}}
];
