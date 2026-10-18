import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterOutlet, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ApiService, LoginResponse } from './api.service';
@Component({selector:'ib-root',standalone:true,imports:[RouterOutlet],template:'<router-outlet />'})
export class PortalRoot {}
@Component({standalone:true,imports:[CommonModule,FormsModule,RouterLink],templateUrl:'./portal.component.html'})
export class PortalComponent {
 readonly router=inject(Router); readonly route=inject(ActivatedRoute); readonly http=inject(HttpClient); readonly api=inject(ApiService);
 get page(): string { return this.route.snapshot.data['page'] || 'home'; }
 email=localStorage.getItem('ib-remember-email') || ''; password=''; confirmPassword=''; fullName=''; phone=''; acceptTerms=false; remember=!!this.email; showPassword=false; busy=false; error=''; notice='';
 profile: any=null; customers: any[]=[]; tab='overview';
 constructor() { if (this.route.snapshot.data['profile']) this.profile=this.route.snapshot.data['profile']; }
 submit(): void {
 this.error=''; this.notice='';
 if (this.busy) return;
 if (this.page==='signup' && (this.password!==this.confirmPassword || !this.acceptTerms)) { this.error='Confirm your password and accept the terms.'; return; }
 this.busy=true;
 const request=this.page==='signup' ? this.http.post<LoginResponse>('/api/v1/auth/register',{fullName:this.fullName,email:this.email,phone:this.phone,password:this.password,confirmPassword:this.confirmPassword,acceptTerms:this.acceptTerms}) : this.api.login(this.email,this.password);
 request.subscribe({next: result => {
 const expected=this.page==='admin-login'?'ADMIN':'CUSTOMER';
 if (result.role!==expected) { this.busy=false; this.error='This account cannot access this portal. Use the appropriate sign-in page.'; return; }
 this.api.setAccessToken(result.accessToken);this.api.setRole(result.role);
 if(this.remember) localStorage.setItem('ib-remember-email',this.email);else localStorage.removeItem('ib-remember-email');
 this.password='';this.busy=false;this.router.navigateByUrl(result.role==='ADMIN'?'/admin':'/dashboard');
 },error:(e:HttpErrorResponse)=>{this.busy=false;this.error=e.status===0?'Cannot connect to the banking service. Please try again.':e.status===401?'Email or password is incorrect.':e.status===429?'Too many attempts. Please wait and try again.':Object.values(e.error?.fields || {}).join('. ') || e.error?.message || 'Unable to continue. Please try again.';}});
 }
 logout(): void { this.api.clearAccessToken();this.router.navigateByUrl('/'); }
}
