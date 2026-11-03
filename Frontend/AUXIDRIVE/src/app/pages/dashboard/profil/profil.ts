import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { ProfilService } from '../../../services/profil-service';
import { DocumentProfilService } from '../../../services/document-profil-service';
import { Profil } from '../../../core/interfaces/profil';
import { DocumentProfil, TypeDocument, TypeDocumentProfil } from '../../../core/interfaces/document-profil';
import { Squelette } from '../../../components/squelette/squelette';
import { EtatVide } from '../../../components/etat-vide/etat-vide';

const MOIS_FR = [
  'janvier', 'février', 'mars', 'avril', 'mai', 'juin',
  'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre',
];

const TAILLE_MAX = 5 * 1024 * 1024;
const EXTENSIONS_ACCEPTEES = ['pdf', 'jpg', 'jpeg', 'png'];

@Component({
  selector: 'app-dashboard-profil',
  imports: [FormsModule, Squelette, EtatVide],
  templateUrl: './profil.html',
  styleUrl: './profil.scss',
})
export class DashboardProfil implements OnInit {
  private profilService = inject(ProfilService);
  private documentService = inject(DocumentProfilService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private sanitizer = inject(DomSanitizer);

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

  // pièces justificatives
  readonly typesDocuments: TypeDocument[] = [
    {
      valeur: 'CIN',
      libelle: 'Carte d’identité nationale',
      description: 'Recto lisible, en cours de validité.',
      icone: 'bi-person-vcard',
    },
    {
      valeur: 'CARTE_GRISE',
      libelle: 'Carte grise',
      description: 'Certificat d’immatriculation du véhicule assuré.',
      icone: 'bi-car-front',
    },
    {
      valeur: 'CARTE_ROSE',
      libelle: 'Carte rose',
      description: 'Permis de conduire du conducteur principal.',
      icone: 'bi-credit-card-2-front',
    },
    {
      valeur: 'MODELE_N1',
      libelle: 'Modèle N°1',
      description: 'Formulaire de déclaration signé.',
      icone: 'bi-file-earmark-text',
    },
  ];

  documents: DocumentProfil[] = [];
  documentsChargement = true;
  documentsErreur = '';
  messageDocuments = '';
  erreurDocument = '';
  documentEnCours = false;
  typeOuvert: TypeDocumentProfil | null = null;
  documentASupprimer: DocumentProfil | null = null;
  apercuDocument: SafeResourceUrl | null = null;
  apercuDocumentType = '';
  private apercuObjet: string | null = null;

  ngOnInit(): void {
    this.charger();
    this.chargerDocuments();
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

  get piecesParType(): { type: TypeDocument; document: DocumentProfil | null }[] {
    return this.typesDocuments.map(type => ({
      type,
      document: this.documentDuType(type.valeur) ?? null,
    }));
  }

  get nombrePieces(): number {
    return this.documents.length;
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

  // ---------- Pièces justificatives ----------

  documentDuType(type: TypeDocumentProfil): DocumentProfil | undefined {
    return this.documents.find(d => d.typeDocument === type);
  }

  libelleType(type: string): string {
    return this.typesDocuments.find(t => t.valeur === type)?.libelle ?? type.replaceAll('_', ' ');
  }

  iconeType(type: string): string {
    return this.typesDocuments.find(t => t.valeur === type)?.icone ?? 'bi-file-earmark';
  }

  statutLibelle(doc: DocumentProfil): string {
    if (doc.statutDocuments === 'VALIDE') return 'Vérifiée';
    if (doc.statutDocuments === 'REJETE') return 'À remplacer';
    return 'En cours de vérification';
  }

  statutClasse(doc: DocumentProfil): string {
    if (doc.statutDocuments === 'VALIDE') return 'statut-valide';
    if (doc.statutDocuments === 'REJETE') return 'statut-rejete';
    return 'statut-attente';
  }

  dateDocument(doc: DocumentProfil): string {
    if (!doc.dateTeleversement) return 'Date inconnue';
    const date = new Date(doc.dateTeleversement);
    return `${date.getDate()} ${MOIS_FR[date.getMonth()]} ${date.getFullYear()}`;
  }

  tailleDocument(doc: DocumentProfil): string {
    return doc.tailleKo ? `${doc.tailleKo} Ko` : '';
  }

  ouvrirSelecteur(type: TypeDocumentProfil): void {
    if (this.documentEnCours) return;
    this.erreurDocument = '';
    this.messageDocuments = '';
    this.typeOuvert = this.typeOuvert === type ? null : type;
  }

  /** Sélection d'un fichier : validation locale avant l'envoi, puis ajout ou remplacement. */
  choisirFichier(type: TypeDocumentProfil, evenement: Event): void {
    const input = evenement.target as HTMLInputElement;
    const fichier = input.files?.[0];
    input.value = '';
    if (!fichier) return;

    const extension = fichier.name.split('.').pop()?.toLowerCase() ?? '';
    if (!EXTENSIONS_ACCEPTEES.includes(extension)) {
      this.erreurDocument = 'Format refusé : choisissez un fichier PDF, JPEG ou PNG.';
      return;
    }
    if (fichier.size > TAILLE_MAX) {
      this.erreurDocument = 'Fichier trop volumineux : 5 Mo maximum.';
      return;
    }

    this.envoyerDocument(type, fichier);
  }

  private envoyerDocument(type: TypeDocumentProfil, fichier: File): void {
    this.erreurDocument = '';
    this.messageDocuments = '';
    this.documentEnCours = true;
    const existant = this.documentDuType(type);

    const requete = existant
      ? this.documentService.remplacer(existant.idDocument, fichier)
      : this.documentService.ajouter(type, fichier);

    requete.subscribe({
      next: (piece) => {
        this.remplacerEnMemoire(piece);
        this.documentEnCours = false;
        this.typeOuvert = null;
        this.messageDocuments = existant
          ? `La pièce « ${this.libelleType(type)} » a été remplacée. Elle sera vérifiée par un agent.`
          : `La pièce « ${this.libelleType(type)} » a été ajoutée. Elle sera vérifiée par un agent.`;
      },
      error: (err: HttpErrorResponse) => {
        this.documentEnCours = false;
        this.erreurDocument = this.messageDocument(err);
      },
    });
  }

  private remplacerEnMemoire(piece: DocumentProfil): void {
    const index = this.documents.findIndex(d => d.typeDocument === piece.typeDocument);
    if (index >= 0) {
      this.documents = this.documents.map(d => (d.typeDocument === piece.typeDocument ? piece : d));
    } else {
      this.documents = [...this.documents, piece];
    }
    this.documents.sort(
      (a, b) =>
        this.typesDocuments.findIndex(t => t.valeur === a.typeDocument) -
        this.typesDocuments.findIndex(t => t.valeur === b.typeDocument),
    );
  }

  demanderSuppression(doc: DocumentProfil): void {
    if (this.documentEnCours) return;
    this.erreurDocument = '';
    this.messageDocuments = '';
    this.documentASupprimer = doc;
  }

  annulerSuppressionDocument(): void {
    this.documentASupprimer = null;
  }

  confirmerSuppressionDocument(): void {
    const doc = this.documentASupprimer;
    if (!doc || this.documentEnCours) return;
    this.documentEnCours = true;
    this.erreurDocument = '';

    this.documentService.supprimer(doc.idDocument).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.idDocument !== doc.idDocument);
        this.documentASupprimer = null;
        this.documentEnCours = false;
        this.messageDocuments = `La pièce « ${this.libelleType(doc.typeDocument)} » a été supprimée.`;
      },
      error: (err: HttpErrorResponse) => {
        this.documentASupprimer = null;
        this.documentEnCours = false;
        this.erreurDocument = this.messageDocument(err);
      },
    });
  }

  voirDocument(doc: DocumentProfil): void {
    this.fermerApercuDocument();
    this.erreurDocument = '';
    this.documentService.fichier(doc.idDocument).subscribe({
      next: (blob) => {
        this.apercuObjet = URL.createObjectURL(blob);
        this.apercuDocument = this.sanitizer.bypassSecurityTrustResourceUrl(this.apercuObjet);
        this.apercuDocumentType = (doc.formatFichier || 'pdf').toLowerCase();
      },
      error: () => {
        this.erreurDocument = 'Impossible d’ouvrir cette pièce pour le moment.';
      },
    });
  }

  fermerApercuDocument(): void {
    if (this.apercuObjet) URL.revokeObjectURL(this.apercuObjet);
    this.apercuObjet = null;
    this.apercuDocument = null;
  }

  rechargerDocuments(): void {
    this.chargerDocuments();
  }

  private chargerDocuments(): void {
    this.documentsChargement = true;
    this.documentsErreur = '';
    this.documentService.lister().subscribe({
      next: (liste) => {
        this.documents = Array.isArray(liste) ? liste : [];
        this.documentsChargement = false;
      },
      error: () => {
        this.documentsErreur = 'Impossible de charger vos pièces justificatives. Vérifiez votre connexion puis réessayez.';
        this.documentsChargement = false;
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

  private messageDocument(err: HttpErrorResponse): string {
    if (err.status === 409) {
      return 'Cette pièce ne peut pas être modifiée pour le moment : un dossier d’inscription est en cours d’instruction. Suivez le lien de correction reçu par e-mail.';
    }
    if (err.status === 400 || err.status === 413) {
      return 'Le fichier est refusé : choisissez un PDF, JPEG ou PNG de 5 Mo maximum.';
    }
    if (err.status === 404) {
      return 'Cette pièce est introuvable : rechargez la page puis réessayez.';
    }
    return 'Opération impossible pour le moment. Vérifiez votre connexion puis réessayez.';
  }
}
