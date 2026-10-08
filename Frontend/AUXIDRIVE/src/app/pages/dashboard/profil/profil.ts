import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { ProfilService } from '../../../services/profil-service';
import { Profil } from '../../../core/interfaces/profil';

const MOIS_FR = [
  'janvier', 'février', 'mars', 'avril', 'mai', 'juin',
  'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre',
];

@Component({
  selector: 'app-dashboard-profil',
  imports: [FormsModule],
  templateUrl: './profil.html',
  styleUrl: './profil.scss',
})
export class DashboardProfil implements OnInit {
  private profilService = inject(ProfilService);
  private authService = inject(AuthService);
  private router = inject(Router);

  chargement = true;
  enCours = false;
  messageSucces = '';
  messageErreur = '';

  profil: Profil | null = null;

  // informations personnelles
  nom = '';
  prenom = '';
  adresse = '';
  contact = '';
  mail = '';
  emailConnexion = '';
  private emailInitial = '';

  // mot de passe
  mdpActuel = '';
  mdpNouveau = '';
  mdpConfirmation = '';

  // suppression du compte
  confirmationAffichee = false;

  ngOnInit(): void {
    this.charger();
  }

  get dateSuppression(): string | null {
    return this.profil?.compte.dateSuppressionPlanifiee ?? null;
  }

  get suppressionPlanifiee(): boolean {
    return this.dateSuppression !== null;
  }

  get dateSuppressionFr(): string {
    return this.formaterDate(this.dateSuppression);
  }

  get heuresRestantes(): number {
    if (!this.dateSuppression) {
      return 0;
    }
    const resteMs = new Date(this.dateSuppression).getTime() - Date.now();
    return Math.max(0, Math.ceil(resteMs / 3_600_000));
  }

  get dateNaissanceFr(): string {
    const iso = this.profil?.assure.dateNaissance;
    return iso ? iso.split('-').reverse().join('/') : '—';
  }

  get informationsValides(): boolean {
    return (
      this.nom.trim() !== '' &&
      this.prenom.trim() !== '' &&
      this.emailValide(this.mail) &&
      this.emailValide(this.emailConnexion)
    );
  }

  get motDePasseValide(): boolean {
    return this.mdpActuel !== '' && this.mdpNouveau.length >= 8 && this.mdpNouveau === this.mdpConfirmation;
  }

  get emailModifiee(): boolean {
    return this.emailConnexion.trim() !== this.emailInitial;
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
        nomAssure: this.nom.trim(),
        prenomAssure: this.prenom.trim(),
        adresse: this.adresse.trim(),
        contact: this.contact.trim(),
        mail: this.mail.trim(),
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

  planifierSuppression(): void {
    if (this.enCours) {
      return;
    }
    this.viderMessages();
    this.enCours = true;

    this.profilService.planifierSuppression().subscribe({
      next: (reponse) => {
        if (this.profil) {
          this.profil.compte.dateSuppressionPlanifiee = reponse.dateSuppressionPlanifiee;
        }
        this.confirmationAffichee = false;
        this.messageSucces =
          'Compte à supprimer : la suppression définitive est programmée dans 72 heures. ' +
          'Vous pouvez encore l’annuler depuis cette page.';
        this.enCours = false;
      },
      error: (err: HttpErrorResponse) => {
        this.messageErreur = this.messageSuppression(err);
        this.enCours = false;
      },
    });
  }

  annulerSuppression(): void {
    if (this.enCours) {
      return;
    }
    this.viderMessages();
    this.enCours = true;

    this.profilService.annulerSuppression().subscribe({
      next: (reponse) => {
        if (this.profil) {
          this.profil.compte.dateSuppressionPlanifiee = reponse.dateSuppressionPlanifiee;
        }
        this.messageSucces = 'Suppression annulée : votre compte reste actif.';
        this.enCours = false;
      },
      error: (err: HttpErrorResponse) => {
        this.messageErreur = this.messageSuppression(err);
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

  private remplir(profil: Profil): void {
    this.profil = profil;
    const assure = profil.assure;
    this.nom = assure.nomAssure ?? '';
    this.prenom = assure.prenomAssure ?? '';
    this.adresse = assure.adresse ?? '';
    this.contact = assure.contact ?? '';
    this.mail = assure.mail ?? '';
    this.emailConnexion = profil.compte.email;
    this.emailInitial = profil.compte.email;
    this.chargement = false;
  }

  private viderMessages(): void {
    this.messageSucces = '';
    this.messageErreur = '';
  }

  private emailValide(valeur: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valeur.trim());
  }

  private formaterDate(iso: string | null): string {
    if (!iso) {
      return '';
    }
    const date = new Date(iso);
    const heures = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${date.getDate()} ${MOIS_FR[date.getMonth()]} ${date.getFullYear()} à ${heures}h${minutes}`;
  }

  private messageInformations(err: HttpErrorResponse): string {
    if (err.status === 409) {
      return 'Cet e-mail de connexion est déjà utilisé par un autre compte.';
    }
    if (err.status === 400) {
      return 'Vérifiez les champs : nom et prénom obligatoires, adresses e-mail valides, longueurs maximales respectées.';
    }
    if (err.status === 404 || err.status === 401) {
      return 'Session expirée ou compte introuvable. Veuillez vous reconnecter.';
    }
    return "Impossible d'enregistrer vos informations. Vérifiez votre connexion puis réessayez.";
  }

  private messageMotDePasse(err: HttpErrorResponse): string {
    if (err.status === 400) {
      return "Ancien mot de passe incorrect ou nouveau mot de passe trop court (8 caractères minimum).";
    }
    if (err.status === 404 || err.status === 401) {
      return 'Session expirée ou compte introuvable. Veuillez vous reconnecter.';
    }
    return 'Impossible de changer le mot de passe. Vérifiez votre connexion puis réessayez.';
  }

  private messageSuppression(err: HttpErrorResponse): string {
    if (err.status === 404 || err.status === 401) {
      return 'Session expirée ou compte introuvable. Veuillez vous reconnecter.';
    }
    return 'Opération impossible pour le moment. Vérifiez votre connexion puis réessayez.';
  }
}
