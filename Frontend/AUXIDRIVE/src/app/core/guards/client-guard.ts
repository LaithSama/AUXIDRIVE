import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

export const clientGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const role = authService.getRole();

  // L'espace /dashboard est réservé aux assurés
  if (role === 'ROLE_CLIENT' || role === 'CLIENT') {
    return true;
  }

  // Un agent ou un administrateur possède son propre espace
  if (role === 'ROLE_AGENT' || role === 'AGENT' || role === 'ROLE_ADMIN') {
    router.navigate(['/agent-dashboard']);
    return false;
  }

  // Aucune session : retour à la connexion
  router.navigate(['/connexion']);
  return false;
};
