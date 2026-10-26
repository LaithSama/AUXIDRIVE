import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';

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
  enCours = false;

  onSubmit(formulaire?: NgForm): void {
    if (this.enCours) return;
    this.errorMessage = '';
    if (formulaire?.invalid) {
      formulaire.control.markAllAsTouched();
      this.errorMessage = 'Renseignez une adresse e-mail valide et votre mot de passe.';
      return;
    }

    this.enCours = true;
    this.authService.login(this.email, this.motDePasse).subscribe({
      next: (res) => {
        this.enCours = false;
        if (res.role === 'ROLE_ADMIN' || res.role === 'ADMIN') {
          this.router.navigate(['/admin-dashboard']); // Espace d'administration
        } else if (res.role === 'ROLE_AGENT' || res.role === 'AGENT') {
          this.router.navigate(['/agent-dashboard']); // Espace de gestion
        } else {
          this.router.navigate(['/dashboard']); // Espace client standard
        }
      },
      error: (err) => {
        this.enCours = false;
        if (err.status === 401) {
          this.errorMessage = 'Identifiants invalides.';
        } else if (err.status === 403) {
          this.errorMessage = 'Compte non validé ou désactivé.';
        } else if (err.status === 0) {
          this.errorMessage = 'Connexion au serveur impossible. Vérifiez votre connexion et réessayez.';
        } else {
          this.errorMessage = 'Une erreur serveur est survenue. Veuillez réessayer.';
        }
      },
    });
  }
}
