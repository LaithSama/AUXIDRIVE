import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { ProfilStaffService } from '../../services/profil-staff-service';
import { ProfilStaff } from '../../core/interfaces/profil-staff';
import { DateFrPipe } from '../../core/pipes/date-fr.pipe';

/**
 * Profil de l'agent ou de l'administrateur connecté : nom, e-mail de connexion et mot de passe.
 * Composant partagé par les espaces agent et admin (le rôle et le statut restent en lecture seule).
 */
@Component({
  selector: 'app-profil-staff',
  standalone: true,
  imports: [FormsModule, DateFrPipe],
  templateUrl: './profil-staff.html',
  styleUrl: './profil-staff.scss',
})
export class ProfilStaffPanel implements OnInit {
  private profilService = inject(ProfilStaffService);
  private authService = inject(AuthService);
  private router = inject(Router);

  chargement = true;
  enCours = false;
  messageSucces = '';
  messageErreur = '';

  profil: ProfilStaff | null = null;

  // informations
  nom = '';
  emailConnexion = '';
  private emailInitial = '';

  // mot de passe
  mdpActuel = '';
  mdpNouveau = '';
  mdpConfirmation = '';

  ngOnInit(): void {
    this.charger();
  }

  get informationsValides(): boolean {
    return this.nom.trim() !== '' && this.emailValide(this.emailConnexion);
  }

  get motDePasseValide(): boolean {
    return this.mdpActuel !== '' && this.mdpNouveau.length >= 8 && this.mdpNouveau === this.mdpConfirmation;
  }

  get emailModifiee(): boolean {
    return this.emailConnexion.trim().toLowerCase() !== this.emailInitial;
  }

  get statutLibelle(): string {
    switch (this.profil?.statutUtilisateur) {
      case 'ACTIF': return 'Actif';
      case 'SUSPENDU': return 'Suspendu';
      case 'INACTIF': return 'Inactif';
      default: return '—';
    }
  }

  enregistrer(): void {
    if (!this.informationsValides || this.enCours) {
      return;
    }
    this.viderMessages();
    this.enCours = true;
    const emailChangee = this.emailModifiee;

    this.profilService
      .enregistrer({
        nomUtil: this.nom.trim(),
        emailConnexion: this.emailConnexion.trim(),
      })
      .subscribe({
        next: (profil) => {
          this.remplir(profil);
          this.enCours = false;
          if (emailChangee) {
            // le token en cours porte l'ancien e-mail : la session ne peut plus être utilisée
            this.messageSucces = 'E-mail de connexion modifié. Veuillez vous reconnecter.';
            setTimeout(() => {
              this.authService.logout();
              this.router.navigate(['/connexion']);
            }, 2500);
          } else {
            this.messageSucces = 'Vos informations ont été enregistrées.';
          }
        },
        error: (err: HttpErrorResponse) => {
          this.messageErreur = this.messageInformations(err);
          this.enCours = false;
        },
      });
  }

  changerMotDePasse(): void {
    if (!this.motDePasseValide || this.enCours) {
      return;
    }
    this.viderMessages();
    this.enCours = true;

    this.profilService
      .changerMotDePasse({ motDePasseActuel: this.mdpActuel, nouveauMotDePasse: this.mdpNouveau })
      .subscribe({
        next: () => {
          this.mdpActuel = '';
          this.mdpNouveau = '';
          this.mdpConfirmation = '';
          this.messageSucces = 'Votre mot de passe a été mis à jour.';
          this.enCours = false;
        },
        error: (err: HttpErrorResponse) => {
          this.messageErreur = this.messageMotDePasse(err);
          this.enCours = false;
        },
      });
  }

  private charger(): void {
    this.profilService.monProfil().subscribe({
      next: (profil) => this.remplir(profil),
      error: () => {
        this.messageErreur = 'Impossible de charger votre profil. Vérifiez votre connexion puis réessayez.';
        this.chargement = false;
      },
    });
  }

  private remplir(profil: ProfilStaff): void {
    this.profil = profil;
    this.nom = profil.nomUtil ?? '';
    this.emailConnexion = profil.email;
    this.emailInitial = profil.email.toLowerCase();
    this.chargement = false;
  }

  private viderMessages(): void {
    this.messageSucces = '';
    this.messageErreur = '';
  }

  private emailValide(valeur: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valeur.trim());
  }

  private messageInformations(err: HttpErrorResponse): string {
    if (err.status === 409) {
      return 'Cet e-mail de connexion est déjà utilisé par un autre compte.';
    }
    if (err.status === 400) {
      return 'Vérifiez les champs : nom obligatoire et adresse e-mail valide.';
    }
    if (err.status === 404 || err.status === 401) {
      return 'Session expirée ou compte introuvable. Veuillez vous reconnecter.';
    }
    return "Impossible d'enregistrer vos informations. Vérifiez votre connexion puis réessayez.";
  }

  private messageMotDePasse(err: HttpErrorResponse): string {
    if (err.status === 400) {
      return "Ancien mot de passe incorrect ou nouveau mot de passe invalide (8 caractères minimum).";
    }
    if (err.status === 404 || err.status === 401) {
      return 'Session expirée ou compte introuvable. Veuillez vous reconnecter.';
    }
    return 'Impossible de changer le mot de passe. Vérifiez votre connexion puis réessayez.';
  }
}
