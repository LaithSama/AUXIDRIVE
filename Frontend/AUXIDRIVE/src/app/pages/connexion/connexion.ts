import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-connexion',
  imports: [FormsModule, RouterLink],
  templateUrl: './connexion.html',
  styleUrl: './connexion.scss',
})
export class Connexion {
  private authService = inject(AuthService);
  private router = inject(Router);

  email: string = '';
  motDePasse: string = '';
  errorMessage: string = '';

  onSubmit(): void {
    this.errorMessage = '';

    this.authService.login(this.email, this.motDePasse).subscribe({
      next: (res) => {
        console.log('Connexion réussie ! Rôle détecté :', res.role);
        
        // 👈 C'est cette vérification cruciale qu'il faut ajouter ou valider
        if (res.role === 'ROLE_ADMIN' || res.role === 'ADMIN') {
          this.router.navigate(['/admin-dashboard']); // Espace d'administration
        } else if (res.role === 'ROLE_AGENT' || res.role === 'AGENT') {
          this.router.navigate(['/agent-dashboard']); // Espace de gestion
        } else {
          this.router.navigate(['/dashboard']); // Espace client standard
        }
      },
      error: (err) => {
        if (err.status === 401) {
          this.errorMessage = 'Identifiants invalides.';
        } else if (err.status === 403) {
          this.errorMessage = 'Compte non validé ou désactivé.';
        } else {
          this.errorMessage = 'Une erreur serveur est survenue. Veuillez réessayer.';
        }
      },
    });
  }
}
