import { bootstrapApplication } from '@angular/platform-browser';
import * as bootstrap from 'bootstrap';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));

// Expose le bundle Bootstrap pour les composants qui pilotent l'UI
// via data-bs-* (menu mobile, carrousels, etc.).
(globalThis as unknown as { bootstrap?: typeof bootstrap }).bootstrap = bootstrap;
