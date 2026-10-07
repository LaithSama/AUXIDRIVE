import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const role = authService.getRole();

  // Si l'utilisateur possède le rôle d'agent ou d'administrateur, l'accès est autorisé
  if (role === 'ROLE_AGENT' || role === 'AGENT' || role === 'ROLE_ADMIN') {
    return true;
  }

  // Sinon, on le redirige vers la page de connexion
  router.navigate(['/connexion']);
  return false;
};
