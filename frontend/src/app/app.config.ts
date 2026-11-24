import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideApi } from './api/generated';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withFetch()),
    // Les chemins du contrat sont absolus (/api/v1/…) : l'API est servie par la même origine, via edge.
    provideApi(''),
  ],
};
