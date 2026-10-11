import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

/** L'espace d'administration est strictement réservé au rôle ADMIN. */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const role = authService.getRole();

  if (role === 'ROLE_ADMIN' || role === 'ADMIN') {
    return true;
  }

  // Un agent possède son propre espace
  if (role === 'ROLE_AGENT' || role === 'AGENT') {
    router.navigate(['/agent-dashboard']);
    return false;
  }

  // Aucune session : retour à la connexion
  router.navigate(['/connexion']);
  return false;
};
