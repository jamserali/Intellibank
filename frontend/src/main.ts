import { provideZoneChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { PortalRoot } from './app/portal.component';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { routes } from './app/portal.routes';
import { authInterceptor } from './app/auth.interceptor';

bootstrapApplication(PortalRoot, {
  providers: [
    provideZoneChangeDetection(),
    provideRouter(routes, withInMemoryScrolling({anchorScrolling:'enabled',scrollPositionRestoration:'enabled'})),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
}).catch((error: unknown) => console.error(error));
