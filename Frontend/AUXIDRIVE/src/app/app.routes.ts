import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.Home),
    title: 'AUXIASSUR — Assurance automobile | AUXIMAD',
  },
  {
    path: 'connexion',
    loadComponent: () => import('./pages/connexion/connexion').then((m) => m.Connexion),
    title: 'Espace client — AUXIASSUR',
  },
  { path: '**', redirectTo: '' },
];