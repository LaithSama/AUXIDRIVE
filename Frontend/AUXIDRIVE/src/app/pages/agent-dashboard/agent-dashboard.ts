import { Component, HostListener, inject, OnInit, signal, computed, WritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Contrat, StatutContrat } from '../../core/interfaces/contrat';
import { DetailSimulation, ParametresPrime } from '../../core/interfaces/demande-souscription';
import { Sinistre } from '../../core/interfaces/sinistre';
import { MontantPipe } from '../../core/pipes/montant.pipe';
import { DateFrPipe } from '../../core/pipes/date-fr.pipe';
import { AuthService } from '../../services/auth-service';
import { ProfilStaffPanel } from '../../components/profil-staff/profil-staff';
import { EtatVide } from '../../components/etat-vide/etat-vide';
import { Squelette } from '../../components/squelette/squelette';
import { environment } from '../../../environments/environment';

type VueAgent = 'tableau' | 'inscriptions' | 'contrats' | 'sinistres' | 'documents' | 'profil';
type OngletSinistre = 'DECLARE' | 'RAPPORT_EN_COURS' | 'CLOTURES';
interface DocumentInscription {
  idDocument: number;
  typeDocument: string;
  formatFichier: string;
  tailleKo?: number;
  dateTeleversement?: string;
  statutDocuments: 'EN_ATTENTE' | 'VALIDE' | 'REJETE';
  motifRejet?: string;
}
interface DocumentAVerifier {
  idDocument: number;
  typeDocument: string;
  formatFichier: string;
  tailleKo?: number;
  dateTeleversement?: string;
  statutDocuments: string;
  motifRejet?: string;
  idAssure?: number;
  nomAssure?: string;
  prenomAssure?: string;
  numerosAssure?: string;
  mailAssure?: string;
  origine: 'PROFIL' | 'INSCRIPTION';
}

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [FormsModule, MontantPipe, DateFrPipe, ProfilStaffPanel, EtatVide, Squelette],
  templateUrl: './agent-dashboard.html',
  styleUrl: './agent-dashboard.scss',
})
export class AgentDashboard implements OnInit {
  readonly taillePage = 10;
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);
  private sanitizer = inject(DomSanitizer);

  private apiUrl = `${environment.apiUrl}/api`;

  /** Vue d'ensemble affichée dès l'ouverture de l'espace agent. */
  vue = signal<VueAgent>('tableau');

  /** Sidebar dépliable (mobile). */
  sidebarOuverte = signal(false);

  /** E-mail de l'agent connecté, affiché dans le pied de la sidebar. */
  emailAgent = this.authService.getEmail() ?? 'Agent';

  /** Vrai si l'utilisateur connecté est administrateur (accès au retour vers l'espace admin). */
  estAdmin = (() => {
    const role = this.authService.getRole();
    return role === 'ROLE_ADMIN' || role === 'ADMIN';
  })();

  // ---------- Inscriptions ----------
  private apiUrlInscriptions = `${this.apiUrl}/inscriptions`;

  toutesLesInscriptions = signal<any[]>([]);
  ongletActif = signal<'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE'>('EN_ATTENTE');

  demandesEnAttente = computed(() =>
    this.toutesLesInscriptions().filter(d => d.statutInscription === 'EN_ATTENTE' || d.statutInscription === 'CORRECTION_DEMANDEE'));
  demandesAVerifier = computed(() => this.toutesLesInscriptions().filter(d => d.statutInscription === 'EN_ATTENTE'));
  demandesEnCorrection = computed(() => this.toutesLesInscriptions().filter(d => d.statutInscription === 'CORRECTION_DEMANDEE'));
  demandesValidees = computed(() =>
    this.toutesLesInscriptions().filter(d => d.statutInscription === 'VALIDEE'));
  demandesRefusees = computed(() =>
    this.toutesLesInscriptions().filter(d => d.statutInscription === 'REFUSEE'));

  // Filtres inscriptions
  rechercheInscription = signal('');
  filtreCin = signal<'tous' | 'avec' | 'sans'>('tous');
  dateDemandeDepuis = signal('');
  dateDemandeJusqua = signal('');
  triInscription = signal<'recent' | 'ancien' | 'nom'>('recent');

  inscriptionsAffichees = computed(() => {
    const liste = this.ongletActif() === 'EN_ATTENTE' ? this.demandesEnAttente()
      : this.ongletActif() === 'VALIDEE' ? this.demandesValidees()
      : this.demandesRefusees();

    const filtree = liste.filter(d => {
      const hay = [
        d.idInscription, d.assure?.nomAssure, d.assure?.prenomAssure,
        d.assure?.contact, d.assure?.mail, d.assure?.numerosAssure,
        d.identifiants?.cin, d.motifRefus, d.motif,
      ].map(v => this.normaliser(v)).join(' ');
      if (!this.correspond(hay, this.rechercheInscription())) return false;

      const aCin = !!d.identifiants?.cin;
      if (this.filtreCin() === 'avec' && !aCin) return false;
      if (this.filtreCin() === 'sans' && aCin) return false;

      const date = this.dateIso(d.dateDemande);
      if (!this.dansIntervalle(date, this.dateDemandeDepuis(), this.dateDemandeJusqua())) return false;
      return true;
    });

    return this.trierInscriptions(filtree);
  });
  pageInscriptions = signal(1);
  totalPagesInscriptions = computed(() => Math.max(1, Math.ceil(this.inscriptionsAffichees().length / this.taillePage)));
  pageCouranteInscriptions = computed(() => Math.min(this.pageInscriptions(), this.totalPagesInscriptions()));
  inscriptionsPage = computed(() => this.paginer(this.inscriptionsAffichees(), this.pageCouranteInscriptions()));

  demandeSelectionnee: any = null;
  documentsInscription: DocumentInscription[] = [];
  documentsChargement = false;
  documentsErreur = '';
  motifDocument: Record<number, string> = {};
  cinVerifie = '';
  documentEnCours = false;
  apercuDocument: SafeResourceUrl | null = null;
  apercuDocumentType = '';
  apercuDocumentLibelle = '';
  private apercuDocumentObjet: string | null = null;
  private requeteApercu = 0;
  numAssureSaisi = '';
  motifRefusSaisi = '';
  modeAction: 'VALIDER' | 'REFUSER' | null = null;

  // ---------- Documents à vérifier (ajoutés depuis le profil client) ----------
  documentsVerification = signal<DocumentAVerifier[]>([]);
  chargementVerification = signal(true);
  erreurVerification = signal(false);
  motifVerification: Record<number, string> = {};
  documentVerifEnCours = false;

  // ---------- Contrats ----------
  contrats = signal<Contrat[]>([]);
  ongletContrat = signal<StatutContrat>('EN_ATTENTE');

  contratsAttente = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'EN_ATTENTE'));
  contratsValides = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'VALIDE'));
  contratsRefuses = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'REFUSE'));
  contratsAnnules = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'ANNULE'));

  contratSelectionne: Contrat | null = null;
  contratAAnnuler: Contrat | null = null;
  annulationEnCours = false;
  erreurAnnulation = '';

  /** Champs de la modale de calcul de prime. */
  parametresPrime: ParametresPrime | null = null;
  detailPrime: DetailSimulation | null = null;
  // les champs numériques reçoivent un number (ou null si vidés) via le NumberValueAccessor d'Angular
  primeNetteSaisie: string | number | null = '';
  accessoiresSaisis: string | number | null = '';
  calculPrimeEnCours = false;

  // Filtres contrats
  rechercheContrat = signal('');
  primeMinContrat = signal<string | number | null>('');
  primeMaxContrat = signal<string | number | null>('');
  canalContrat = signal('');
  echeanceProche = signal(false);
  triContrat = signal<'recent' | 'ancien' | 'primeDesc' | 'primeAsc'>('recent');

  canauxDisponibles = computed(() => {
    const canaux = new Set<string>();
    this.contrats().forEach(c => { if (c.canalSouscription) canaux.add(c.canalSouscription); });
    return Array.from(canaux).sort();
  });

  contratsAffiches = computed(() => {
    const liste = this.ongletContrat() === 'EN_ATTENTE' ? this.contratsAttente()
      : this.ongletContrat() === 'VALIDE' ? this.contratsValides()
      : this.ongletContrat() === 'REFUSE' ? this.contratsRefuses() : this.contratsAnnules();

    const min = this.nombreOuNull(this.primeMinContrat());
    const max = this.nombreOuNull(this.primeMaxContrat());
    const canal = this.canalContrat();
    const limite = new Date();
    limite.setDate(limite.getDate() + 30);

    const filtree = liste.filter(c => {
      const hay = [c.idContrat, c.numPolice, c.nomAssure, c.prenomAssure, c.codeAgence]
        .map(v => this.normaliser(v)).join(' ');
      if (!this.correspond(hay, this.rechercheContrat())) return false;

      const prime = c.primeTtcCourante ?? 0;
      if (min !== null && prime < min) return false;
      if (max !== null && prime > max) return false;

      if (canal && c.canalSouscription !== canal) return false;

      if (this.echeanceProche()) {
        if (!c.dateExpiration) return false;
        if (new Date(c.dateExpiration) > limite) return false;
      }
      return true;
    });

    return this.trierContrats(filtree);
  });
  pageContrats = signal(1);
  totalPagesContrats = computed(() => Math.max(1, Math.ceil(this.contratsAffiches().length / this.taillePage)));
  pageCouranteContrats = computed(() => Math.min(this.pageContrats(), this.totalPagesContrats()));
  contratsPage = computed(() => this.paginer(this.contratsAffiches(), this.pageCouranteContrats()));

  // ---------- Sinistres ----------
  sinistres = signal<Sinistre[]>([]);
  ongletSinistre = signal<OngletSinistre>('DECLARE');

  sinistresDeclares = computed(() =>
    this.sinistres().filter(s => s.statutSinistre === 'DECLARE'));
  sinistresEnCours = computed(() =>
    this.sinistres().filter(s => s.statutSinistre === 'RAPPORT_EN_COURS'));
  sinistresATraiter = computed(() => this.sinistresDeclares().length + this.sinistresEnCours().length);
  sinistresClotures = computed(() =>
    this.sinistres().filter(s =>
      s.statutSinistre === 'INDEMNISE' || s.statutSinistre === 'REFUSE'));

  sinistreSelectionne: Sinistre | null = null;
  modeSinistre: 'RAPPORT' | 'CLOTURE' | 'DETAIL' | null = null;

  // Filtres sinistres
  rechercheSinistre = signal('');
  dateSinistreDepuis = signal('');
  dateSinistreJusqua = signal('');
  responsabiliteFiltre = signal<'toutes' | 'assure' | 'tiers' | 'indeterminee'>('toutes');
  montantMinSinistre = signal<string | number | null>('');
  montantMaxSinistre = signal<string | number | null>('');
  rapportRecu = signal<'tous' | 'oui' | 'non'>('tous');
  triSinistre = signal<'recent' | 'ancien' | 'montantDesc' | 'montantAsc'>('recent');

  sinistresAffiches = computed(() => {
    const liste = this.ongletSinistre() === 'DECLARE' ? this.sinistresDeclares()
      : this.ongletSinistre() === 'RAPPORT_EN_COURS' ? this.sinistresEnCours()
      : this.sinistresClotures();

    const min = this.nombreOuNull(this.montantMinSinistre());
    const max = this.nombreOuNull(this.montantMaxSinistre());
    const responsabilite = this.responsabiliteFiltre();
    const rapport = this.rapportRecu();

    const filtree = liste.filter(s => {
      const hay = [s.idSinistre, s.numPolice, s.numRapportPolice, s.nomAssure, s.prenomAssure, s.lieuSinistre]
        .map(v => this.normaliser(v)).join(' ');
      if (!this.correspond(hay, this.rechercheSinistre())) return false;

      const date = this.dateIso(s.dateSinistre);
      if (!this.dansIntervalle(date, this.dateSinistreDepuis(), this.dateSinistreJusqua())) return false;

      if (responsabilite === 'assure' && s.responsabiliteAssure !== true) return false;
      if (responsabilite === 'tiers' && s.responsabiliteAssure !== false) return false;
      if (responsabilite === 'indeterminee' && s.responsabiliteAssure !== null) return false;

      const montant = s.montantIndemnisation ?? 0;
      if (min !== null && montant < min) return false;
      if (max !== null && montant > max) return false;

      const aRapport = !!s.numRapportPolice;
      if (rapport === 'oui' && !aRapport) return false;
      if (rapport === 'non' && aRapport) return false;
      return true;
    });

    return this.trierSinistres(filtree);
  });
  pageSinistres = signal(1);
  totalPagesSinistres = computed(() => Math.max(1, Math.ceil(this.sinistresAffiches().length / this.taillePage)));
  pageCouranteSinistres = computed(() => Math.min(this.pageSinistres(), this.totalPagesSinistres()));
  sinistresPage = computed(() => this.paginer(this.sinistresAffiches(), this.pageCouranteSinistres()));

  // Formulaire de rapport policier
  numRapportSaisi = '';
  dateRapportSaisie = '';
  rapportSaisi = '';
  responsabiliteSaisie: boolean | null = null;

  // Formulaire de clôture
  montantSaisi: string | number | null = '';

  messageSucces = '';
  messageErreur = '';
  chargementInscriptions = signal(true);
  chargementContrats = signal(true);
  chargementSinistres = signal(true);
  erreurInscriptions = signal(false);
  erreurContrats = signal(false);
  erreurSinistres = signal(false);
  chargementTableau = computed(() => this.chargementInscriptions()
    || this.chargementContrats() || this.chargementSinistres());
  erreurTableau = signal(false);

  ngOnInit(): void {
    this.actualiserTableau();
  }

  actualiserTableau(): void {
    this.erreurTableau.set(false);
    this.chargerInitialisation();
    this.chargerContrats();
    this.chargerSinistres();
    this.chargerDocumentsVerification();
  }

  /** Récupère l'intégralité des dossiers auprès du backend */
  chargerInitialisation(): void {
    this.chargementInscriptions.set(true);
    this.erreurInscriptions.set(false);
    this.http.get<any[]>(this.apiUrlInscriptions).subscribe({
      next: (liste) => {
        this.toutesLesInscriptions.set(liste);
        this.pageInscriptions.set(this.pageCouranteInscriptions());
        this.chargementInscriptions.set(false);
      },
      error: (err) => { this.chargementInscriptions.set(false); this.erreurInscriptions.set(true); this.erreurChargement(err); },
    });
  }

  chargerContrats(): void {
    this.chargementContrats.set(true);
    this.erreurContrats.set(false);
    this.http.get<Contrat[]>(`${this.apiUrl}/contrats`).subscribe({
      next: (liste) => {
        this.contrats.set(liste);
        this.pageContrats.set(this.pageCouranteContrats());
        this.chargementContrats.set(false);
      },
      error: (err) => { this.chargementContrats.set(false); this.erreurContrats.set(true); this.erreurChargement(err); },
    });
  }

  chargerSinistres(): void {
    this.chargementSinistres.set(true);
    this.erreurSinistres.set(false);
    this.http.get<Sinistre[]>(`${this.apiUrl}/sinistres`).subscribe({
      next: (liste) => {
        this.sinistres.set(liste);
        this.pageSinistres.set(this.pageCouranteSinistres());
        this.chargementSinistres.set(false);
      },
      error: (err) => { this.chargementSinistres.set(false); this.erreurSinistres.set(true); this.erreurChargement(err); },
    });
  }

  /** Pièces ajoutées depuis le profil client, hors dossiers d'inscription. */
  chargerDocumentsVerification(): void {
    this.chargementVerification.set(true);
    this.erreurVerification.set(false);
    this.http.get<DocumentAVerifier[]>(`${this.apiUrl}/documents/a-verifier`).subscribe({
      next: (liste) => {
        this.documentsVerification.set(Array.isArray(liste) ? liste : []);
        this.chargementVerification.set(false);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des documents à vérifier :', err);
        this.chargementVerification.set(false);
        this.erreurVerification.set(true);
        if (err.status === 401 || err.status === 403) this.deconnexion();
      },
    });
  }

  libelleDocument(type: string): string {
    const libelles: Record<string, string> = {
      CIN: 'Carte d’identité',
      CARTE_GRISE: 'Carte grise',
      CARTE_ROSE: 'Carte rose',
      MODELE_N1: 'Modèle N°1',
    };
    return libelles[type] ?? type.replaceAll('_', ' ');
  }

  examinerDocumentVerification(doc: DocumentAVerifier, decision: 'valider' | 'rejeter'): void {
    const motif = (this.motifVerification[doc.idDocument] ?? '').trim();
    if (this.documentVerifEnCours || (decision === 'rejeter' && !motif)) return;
    this.documentVerifEnCours = true;
    this.messageSucces = '';
    this.messageErreur = '';
    this.http.put(`${this.apiUrl}/documents/${doc.idDocument}/${decision}`, null,
      { params: decision === 'rejeter' ? { motif } : {} }).subscribe({
      next: () => {
        this.documentVerifEnCours = false;
        this.fermerApercuDocument();
        this.chargerDocumentsVerification();
        this.messageSucces = decision === 'valider'
          ? `La pièce « ${this.libelleDocument(doc.typeDocument)} » a été acceptée.`
          : `La pièce « ${this.libelleDocument(doc.typeDocument)} » a été rejetée : le client verra le motif dans son profil.`;
      },
      error: () => {
        this.documentVerifEnCours = false;
        this.messageErreur = 'Impossible de traiter cette pièce.';
      },
    });
  }

  voirDocumentVerification(doc: DocumentAVerifier): void {
    this.fermerApercuDocument();
    const requete = this.requeteApercu;
    this.messageErreur = '';
    this.http.get(`${this.apiUrl}/documents/${doc.idDocument}/fichier`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        if (requete !== this.requeteApercu || this.vue() !== 'documents') return;
        this.apercuDocumentObjet = URL.createObjectURL(blob);
        this.apercuDocument = this.sanitizer.bypassSecurityTrustResourceUrl(this.apercuDocumentObjet);
        this.apercuDocumentType = (doc.formatFichier || 'pdf').toLowerCase();
        this.apercuDocumentLibelle = this.libelleDocument(doc.typeDocument);
      },
      error: () => {
        if (requete !== this.requeteApercu) return;
        this.messageErreur = 'Impossible d’ouvrir ce fichier.';
      },
    });
  }

  private erreurChargement(err: any): void {
    console.error('Erreur lors de la récupération :', err);
    if (err.status === 401 || err.status === 403) {
      this.deconnexion();
      return;
    }
    this.erreurTableau.set(true);
  }

  // ---------- Navigation ----------

  changerVue(vue: VueAgent): void {
    this.fermerApercuDocument();
    this.vue.set(vue);
    this.messageSucces = '';
    this.messageErreur = '';
    this.fermerSidebar();
  }

  /** Les raccourcis ouvrent l'onglet qui correspond au dossier à traiter. */
  ouvrirDossiers(vue: 'inscriptions' | 'contrats' | 'sinistres'): void {
    if (vue === 'inscriptions') this.changerOnglet('EN_ATTENTE');
    if (vue === 'contrats') this.changerOngletContrat('EN_ATTENTE');
    if (vue === 'sinistres') this.changerOngletSinistre(this.sinistresDeclares().length ? 'DECLARE' : 'RAPPORT_EN_COURS');
    this.changerVue(vue);
  }

  basculerSidebar(): void {
    this.sidebarOuverte.update(ouverte => !ouverte);
  }

  fermerSidebar(): void {
    this.sidebarOuverte.set(false);
  }

  /** Retour à l'espace d'administration (réservé aux administrateurs). */
  retourEspaceAdmin(): void {
    this.router.navigate(['/admin-dashboard']);
  }

  changerOnglet(statut: 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE'): void {
    this.ongletActif.set(statut);
    this.pageInscriptions.set(1);
    this.messageSucces = '';
    this.messageErreur = '';
  }

  changerOngletContrat(statut: StatutContrat): void {
    this.ongletContrat.set(statut);
    this.pageContrats.set(1);
    this.messageSucces = '';
    this.messageErreur = '';
  }

  changerOngletSinistre(onglet: OngletSinistre): void {
    this.ongletSinistre.set(onglet);
    this.pageSinistres.set(1);
    this.messageSucces = '';
    this.messageErreur = '';
  }

  // ---------- Modale inscriptions ----------

  ouvrirModale(demande: any, mode: 'VALIDER' | 'REFUSER'): void {
    this.fermerApercuDocument();
    this.demandeSelectionnee = demande;
    this.modeAction = mode;
    this.numAssureSaisi = '';
    this.motifRefusSaisi = '';
    this.messageSucces = '';
    this.messageErreur = '';
    this.motifDocument = {};
    this.cinVerifie = demande.identifiants?.cin ?? '';
    this.chargerDocumentsInscription(demande.idInscription);
  }

  private chargerDocumentsInscription(id: number): void {
    this.documentsChargement = true;
    this.documentsErreur = '';
    this.documentsInscription = [];
    this.http.get<{ documents: DocumentInscription[] }>(`${this.apiUrlInscriptions}/${id}`).subscribe({
      next: dossier => {
        if (this.demandeSelectionnee?.idInscription !== id) return;
        this.documentsInscription = Array.isArray(dossier?.documents) ? dossier.documents : [];
        this.documentsChargement = false;
      },
      error: () => {
        if (this.demandeSelectionnee?.idInscription !== id) return;
        this.documentsErreur = 'Impossible de charger les pièces du dossier.';
        this.documentsChargement = false;
      },
    });
  }

  get documentsPrets(): boolean {
    return !this.documentsChargement && !this.documentsErreur && !this.documentEnCours
      && this.demandeSelectionnee?.statutInscription === 'EN_ATTENTE'
      && this.documentsInscription.every(d => d.statutDocuments === 'VALIDE') && !!this.cinVerifie.trim();
  }

  examinerDocument(document: DocumentInscription, decision: 'valider' | 'rejeter'): void {
    const motif = this.motifDocument[document.idDocument]?.trim() ?? '';
    if (this.documentEnCours || document.statutDocuments !== 'EN_ATTENTE' || (decision === 'rejeter' && !motif)) return;
    this.documentEnCours = true;
    this.documentsErreur = '';
    this.http.put(`${this.apiUrl}/documents/${document.idDocument}/${decision}`, null,
      { params: decision === 'rejeter' ? { motif } : {} }).subscribe({
      next: () => {
        this.documentEnCours = false;
        this.fermerApercuDocument();
        this.chargerDocumentsInscription(this.demandeSelectionnee.idInscription);
        if (decision === 'rejeter') {
          this.demandeSelectionnee.statutInscription = 'CORRECTION_DEMANDEE';
          this.chargerInitialisation();
          this.messageSucces = 'Correction demandée. Vérifiez la remise de l’e-mail ; vous pouvez renvoyer le lien si nécessaire.';
        }
      },
      error: () => { this.documentEnCours = false; this.documentsErreur = 'Impossible de traiter cette pièce.'; },
    });
  }

  renvoyerCorrection(): void {
    if (!this.demandeSelectionnee || this.documentEnCours) return;
    this.documentEnCours = true;
    this.http.post(`${this.apiUrlInscriptions}/${this.demandeSelectionnee.idInscription}/renvoyer-correction`, null)
      .subscribe({
        next: () => { this.documentEnCours = false; this.messageSucces = 'Un nouveau lien de correction a été généré et son envoi a été demandé.'; },
        error: () => { this.documentEnCours = false; this.documentsErreur = 'Impossible de renvoyer la demande de correction.'; },
      });
  }

  voirDocument(document: DocumentInscription): void {
    this.fermerApercuDocument();
    const requete = this.requeteApercu;
    this.documentsErreur = '';
    const dossierId = this.demandeSelectionnee?.idInscription;
    this.http.get(`${this.apiUrl}/documents/${document.idDocument}/fichier`, { responseType: 'blob' }).subscribe({
      next: blob => {
        if (requete !== this.requeteApercu || this.demandeSelectionnee?.idInscription !== dossierId) return;
        this.apercuDocumentObjet = URL.createObjectURL(blob);
        this.apercuDocument = this.sanitizer.bypassSecurityTrustResourceUrl(this.apercuDocumentObjet);
        this.apercuDocumentType = document.formatFichier.toLowerCase();
        this.apercuDocumentLibelle = this.libelleDocument(document.typeDocument);
      },
      error: () => { if (requete === this.requeteApercu) this.documentsErreur = 'Impossible d’ouvrir ce fichier.'; },
    });
  }

  fermerApercuDocument(): void {
    this.requeteApercu++;
    if (this.apercuDocumentObjet) URL.revokeObjectURL(this.apercuDocumentObjet);
    this.apercuDocumentObjet = null;
    this.apercuDocument = null;
    this.apercuDocumentType = '';
    this.apercuDocumentLibelle = '';
  }

  @HostListener('document:keydown.escape')
  fermerApercuSurEchap(): void {
    if (this.apercuDocument) this.fermerApercuDocument();
  }

  fermerModale(): void {
    this.fermerApercuDocument();
    this.demandeSelectionnee = null;
    this.modeAction = null;
    this.contratSelectionne = null;
    if (!this.annulationEnCours) this.contratAAnnuler = null;
    this.parametresPrime = null;
    this.detailPrime = null;
    this.primeNetteSaisie = '';
    this.accessoiresSaisis = '';
    this.sinistreSelectionne = null;
    this.modeSinistre = null;
  }

  /** Vrai quand la saisie est un numéro d'assuré valide : 1 à 8 chiffres. */
  get numAssureValide(): boolean {
    return /^\d{1,8}$/.test(this.numAssureSaisi.trim());
  }

  /** Ne conserve que les chiffres saisis : le numéro d'assuré est numérique (8 chiffres max). */
  filtrerNumAssure(): void {
    this.numAssureSaisi = this.numAssureSaisi.replace(/\D/g, '').slice(0, 8);
  }

  /** POST /api/inscriptions/{id}/valider?numerosAssure=... */
  confirmerValidation(): void {
    if (!this.numAssureValide || !this.demandeSelectionnee || !this.documentsPrets) return;

    const id = this.demandeSelectionnee.idInscription;

    this.http.post(`${this.apiUrlInscriptions}/${id}/valider`, null, {
      params: { numerosAssure: this.numAssureSaisi.trim(), cin: this.cinVerifie.trim() }
    }).subscribe({
      next: () => {
        this.messageSucces = `Le dossier #${id} a été validé avec succès !`;
        this.chargerInitialisation();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = "Impossible de valider ce dossier. Vérifiez les contraintes d'ID.";
      }
    });
  }

  /** POST /api/inscriptions/{id}/refuser?motif=... */
  confirmerRefus(): void {
    if (!this.motifRefusSaisi.trim() || !this.demandeSelectionnee) return;

    const id = this.demandeSelectionnee.idInscription;

    this.http.post(`${this.apiUrlInscriptions}/${id}/refuser`, null, {
      params: { motif: this.motifRefusSaisi.trim() }
    }).subscribe({
      next: () => {
        this.messageSucces = `Le dossier #${id} a été refusé.`;
        this.chargerInitialisation();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = "Une erreur est survenue lors du rejet du dossier.";
      }
    });
  }

  // ---------- Contrats : calcul de prime et validation ----------

  /** Ouvre la modale de calcul de prime et charge les paramètres (code taxe, taux) du contrat. */
  ouvrirModaleContrat(contrat: Contrat): void {
    this.contratSelectionne = contrat;
    this.parametresPrime = null;
    this.detailPrime = null;
    this.primeNetteSaisie = '';
    this.accessoiresSaisis = '';
    this.calculPrimeEnCours = false;
    this.messageSucces = '';
    this.messageErreur = '';

    this.http.get<ParametresPrime>(`${this.apiUrl}/contrats/${contrat.idContrat}/parametres-prime`).subscribe({
      next: (parametres) => {
        this.parametresPrime = parametres;
        this.primeNetteSaisie = parametres.primeNetteCourante != null ? String(parametres.primeNetteCourante) : '';
        this.accessoiresSaisis = parametres.accessoiresDefaut != null ? String(parametres.accessoiresDefaut) : '0';
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = 'Impossible de charger les paramètres de calcul de ce contrat.';
      },
    });
  }

  /** POST /api/contrats/{id}/calculer-prime : TE, TACAVA, TVA, commission et prime TTC. */
  calculerPrimeTtc(): void {
    const contrat = this.contratSelectionne;
    if (!contrat) return;
    const primeNette = this.nombreOuNull(this.primeNetteSaisie);
    if (primeNette === null || primeNette < 0) {
      this.messageErreur = 'Saisissez une prime nette valide.';
      return;
    }

    this.messageErreur = '';
    this.calculPrimeEnCours = true;
    this.detailPrime = null;

    this.http.post<DetailSimulation>(`${this.apiUrl}/contrats/${contrat.idContrat}/calculer-prime`, {
      primeNette,
      accessoires: this.nombreOuNull(this.accessoiresSaisis),
    }).subscribe({
      next: (detail) => {
        this.detailPrime = detail;
        this.calculPrimeEnCours = false;
      },
      error: (err) => {
        console.error(err);
        this.calculPrimeEnCours = false;
        this.messageErreur = err.error?.message || 'Calcul impossible : vérifiez les valeurs saisies.';
      },
    });
  }

  /** POST /api/contrats/{id}/valider : la prime nette et les accessoires calculés sont persistés. */
  validerContrat(): void {
    const contrat = this.contratSelectionne;
    if (!contrat) return;
    if (!this.detailPrime) {
      this.messageErreur = "Calculez d'abord la prime TTC.";
      return;
    }

    this.messageErreur = '';
    this.http.post<Contrat>(`${this.apiUrl}/contrats/${contrat.idContrat}/valider`, {
      primeNette: this.nombreOuNull(this.primeNetteSaisie),
      accessoires: this.nombreOuNull(this.accessoiresSaisis),
    }).subscribe({
      next: () => {
        this.messageSucces = `Contrat #${contrat.idContrat} validé : numéro de police attribué, facture émise.`;
        this.chargerContrats();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = err.status === 409
          ? err.error?.detail || err.error?.message || 'Ce véhicule est déjà couvert ou la demande n’est plus en attente.'
          : 'Impossible de valider ce contrat.';
      },
    });
  }

  /** POST /api/contrats/{id}/refuser */
  refuserContrat(): void {
    const contrat = this.contratSelectionne;
    if (!contrat) return;

    this.messageErreur = '';
    this.http.post<Contrat>(`${this.apiUrl}/contrats/${contrat.idContrat}/refuser`, null).subscribe({
      next: () => {
        this.messageSucces = `Contrat #${contrat.idContrat} refusé.`;
        this.chargerContrats();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = err.status === 409
          ? 'Seule une demande encore en attente peut être traitée.'
          : 'Impossible de refuser ce contrat.';
      },
    });
  }

  ouvrirAnnulation(contrat: Contrat): void {
    if (contrat.statutContrat !== 'VALIDE') return;
    this.contratAAnnuler = contrat;
    this.erreurAnnulation = '';
  }

  fermerAnnulation(): void {
    if (!this.annulationEnCours) this.contratAAnnuler = null;
  }

  confirmerAnnulation(): void {
    const contrat = this.contratAAnnuler;
    if (!contrat || this.annulationEnCours) return;
    this.annulationEnCours = true;
    this.erreurAnnulation = '';
    this.http.post<Contrat>(`${this.apiUrl}/contrats/${contrat.idContrat}/annuler`, null).subscribe({
      next: () => {
        this.annulationEnCours = false;
        this.contratAAnnuler = null;
        this.changerOngletContrat('ANNULE');
        this.messageSucces = `Contrat #${contrat.idContrat} annulé pour client non présenté. Sa facture est annulée.`;
        this.chargerContrats();
      },
      error: (err) => {
        this.annulationEnCours = false;
        this.erreurAnnulation = err.error?.detail || err.error?.message
          || 'Annulation impossible : vérifiez que la facture est impayée et qu’aucun sinistre n’existe.';
      },
    });
  }

  // ---------- Sinistres : rapport et clôture ----------

  ouvrirRapport(sinistre: Sinistre): void {
    this.sinistreSelectionne = sinistre;
    this.modeSinistre = 'RAPPORT';
    this.numRapportSaisi = '';
    this.dateRapportSaisie = '';
    this.rapportSaisi = '';
    this.responsabiliteSaisie = null;
    this.messageSucces = '';
    this.messageErreur = '';
  }

  ouvrirCloture(sinistre: Sinistre): void {
    this.sinistreSelectionne = sinistre;
    this.modeSinistre = 'CLOTURE';
    this.montantSaisi = '';
    this.messageSucces = '';
    this.messageErreur = '';
  }

  ouvrirDetail(sinistre: Sinistre): void {
    this.sinistreSelectionne = sinistre;
    this.modeSinistre = 'DETAIL';
    this.messageSucces = '';
    this.messageErreur = '';
  }

  get rapportPret(): boolean {
    return !!this.numRapportSaisi.trim() && !!this.rapportSaisi.trim();
  }

  get indemnisationPrete(): boolean {
    return this.montantSaisi !== null
      && this.montantSaisi !== undefined
      && String(this.montantSaisi).trim() !== ''
      && Number(this.montantSaisi) >= 0;
  }

  /** Vrai tant que la prime nette n'a pas été saisie : le bouton de calcul reste alors désactivé. */
  get primeNetteSaisieVide(): boolean {
    return this.primeNetteSaisie === null
      || this.primeNetteSaisie === undefined
      || String(this.primeNetteSaisie).trim() === '';
  }

  responsabiliteLibelle(valeur: boolean | null): string {
    if (valeur === null) return 'Non déterminée';
    return valeur ? "À l'assuré" : 'À un tiers';
  }

  /** POST /api/sinistres/{id}/rapport : le sinistre passe en instruction. */
  envoyerRapport(): void {
    const sinistre = this.sinistreSelectionne;
    if (!sinistre || !this.rapportPret) return;

    this.http.post<Sinistre>(`${this.apiUrl}/sinistres/${sinistre.idSinistre}/rapport`, {
      numRapportPolice: this.numRapportSaisi.trim(),
      dateRapportPolice: this.dateRapportSaisie || null,
      rapportPolicier: this.rapportSaisi.trim(),
      responsabiliteAssure: this.responsabiliteSaisie,
    }).subscribe({
      next: () => {
        this.messageSucces = `Rapport policier enregistré pour le sinistre n°${sinistre.idSinistre}.`;
        this.chargerSinistres();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = err.status === 409
          ? 'Ce sinistre ne peut plus recevoir de rapport.'
          : "Impossible d'enregistrer le rapport policier.";
      },
    });
  }

  /** POST /api/sinistres/{id}/indemniser ou /refuser : le dossier est clôturé. */
  cloturer(indemniser: boolean): void {
    const sinistre = this.sinistreSelectionne;
    if (!sinistre) return;
    if (indemniser && !this.indemnisationPrete) return;

    const url = `${this.apiUrl}/sinistres/${sinistre.idSinistre}/${indemniser ? 'indemniser' : 'refuser'}`;
    const corps = indemniser ? { montant: Number(this.montantSaisi) } : null;

    this.http.post<Sinistre>(url, corps).subscribe({
      next: () => {
        this.messageSucces = indemniser
          ? `Sinistre n°${sinistre.idSinistre} validé : indemnisation enregistrée.`
          : `Sinistre n°${sinistre.idSinistre} refusé.`;
        this.chargerSinistres();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = err.status === 409
          ? 'Ce sinistre est déjà clôturé.'
          : 'Impossible de clôturer ce dossier.';
      },
    });
  }

  deconnexion(): void {
    sessionStorage.clear();
    this.router.navigate(['/connexion']);
  }

  // ---------- Filtres : utilitaires ----------

  changerFiltre<T>(filtre: WritableSignal<T>, valeur: T, page: WritableSignal<number>): void {
    filtre.set(valeur);
    page.set(1);
  }

  changerPage(page: WritableSignal<number>, destination: number, total: number): void {
    page.set(Math.max(1, Math.min(destination, total)));
  }

  private paginer<T>(liste: T[], page: number): T[] {
    const debut = (page - 1) * this.taillePage;
    return liste.slice(debut, debut + this.taillePage);
  }

  reinitialiserFiltres(): void {
    switch (this.vue()) {
      case 'contrats':
        this.rechercheContrat.set('');
        this.primeMinContrat.set('');
        this.primeMaxContrat.set('');
        this.canalContrat.set('');
        this.echeanceProche.set(false);
        this.triContrat.set('recent');
        this.pageContrats.set(1);
        break;
      case 'sinistres':
        this.rechercheSinistre.set('');
        this.dateSinistreDepuis.set('');
        this.dateSinistreJusqua.set('');
        this.responsabiliteFiltre.set('toutes');
        this.montantMinSinistre.set('');
        this.montantMaxSinistre.set('');
        this.rapportRecu.set('tous');
        this.triSinistre.set('recent');
        this.pageSinistres.set(1);
        break;
      default:
        this.rechercheInscription.set('');
        this.filtreCin.set('tous');
        this.dateDemandeDepuis.set('');
        this.dateDemandeJusqua.set('');
        this.triInscription.set('recent');
        this.pageInscriptions.set(1);
    }
  }

  private normaliser(valeur: unknown): string {
    return (valeur ?? '').toString().toLowerCase().trim();
  }

  private correspond(texte: string, requete: string): boolean {
    const q = this.normaliser(requete);
    return q === '' || texte.includes(q);
  }

  private nombreOuNull(valeur: string | number | null | undefined): number | null {
    if (valeur === null || valeur === undefined) return null;
    const brut = String(valeur).trim();
    if (brut === '') return null;
    const n = Number(brut);
    return Number.isNaN(n) ? null : n;
  }

  private dateIso(valeur: unknown): string {
    return (valeur ?? '').toString().slice(0, 10);
  }

  private dansIntervalle(date: string, depuis: string, jusqua: string): boolean {
    if (!date) return !depuis && !jusqua;
    if (depuis && date < depuis) return false;
    if (jusqua && date > jusqua) return false;
    return true;
  }

  private dateTri(valeur: unknown): string {
    if (valeur === null || valeur === undefined) {
      return '';
    }
    const brut = valeur.toString();
    if (brut.includes('T')) {
      return brut;
    }
    if (/^\d{4}-\d{2}-\d{2}$/.test(brut)) {
      return brut + 'T00:00:00';
    }
    return brut;
  }

  private trierInscriptions(liste: any[]): any[] {
    return [...liste].sort((a, b) => {
      switch (this.triInscription()) {
        case 'ancien':
          const dateAAncien = this.dateTri(a.dateDemande);
          const dateBAncien = this.dateTri(b.dateDemande);
          if (dateAAncien && dateBAncien && dateAAncien !== dateBAncien) {
            return dateAAncien.localeCompare(dateBAncien);
          }
          if (dateAAncien && !dateBAncien) return -1;
          if (!dateAAncien && dateBAncien) return 1;
          return (a.idInscription ?? 0) - (b.idInscription ?? 0);
        case 'nom':
          return this.nomAssure(a).localeCompare(this.nomAssure(b));
        default:
          const dateARecent = this.dateTri(a.dateDemande);
          const dateBRecent = this.dateTri(b.dateDemande);
          if (dateARecent && dateBRecent && dateARecent !== dateBRecent) {
            return dateBRecent.localeCompare(dateARecent);
          }
          if (dateARecent && !dateBRecent) return -1;
          if (!dateARecent && dateBRecent) return 1;
          return (b.idInscription ?? 0) - (a.idInscription ?? 0);
      }
    });
  }

  private trierContrats(liste: Contrat[]): Contrat[] {
    return [...liste].sort((a, b) => {
      switch (this.triContrat()) {
        case 'ancien':
          const dateAAncien = this.dateTri(a.dateSouscription);
          const dateBAncien = this.dateTri(b.dateSouscription);
          if (dateAAncien && dateBAncien && dateAAncien !== dateBAncien) {
            return dateAAncien.localeCompare(dateBAncien);
          }
          if (dateAAncien && !dateBAncien) return -1;
          if (!dateAAncien && dateBAncien) return 1;
          return (a.idContrat ?? 0) - (b.idContrat ?? 0);
        case 'primeDesc':
          return (b.primeTtcCourante ?? 0) - (a.primeTtcCourante ?? 0);
        case 'primeAsc':
          return (a.primeTtcCourante ?? 0) - (b.primeTtcCourante ?? 0);
        default:
          const dateARecent = this.dateTri(a.dateSouscription);
          const dateBRecent = this.dateTri(b.dateSouscription);
          if (dateARecent && dateBRecent && dateARecent !== dateBRecent) {
            return dateBRecent.localeCompare(dateARecent);
          }
          if (dateARecent && !dateBRecent) return -1;
          if (!dateARecent && dateBRecent) return 1;
          return (b.idContrat ?? 0) - (a.idContrat ?? 0);
      }
    });
  }

  private trierSinistres(liste: Sinistre[]): Sinistre[] {
    const useCloture = this.ongletSinistre() === 'CLOTURES';
    return [...liste].sort((a, b) => {
      switch (this.triSinistre()) {
        case 'ancien':
          const dateAAncien = useCloture ? this.dateTri((a as any).dateCloture) : this.dateTri(a.dateSinistre);
          const dateBAncien = useCloture ? this.dateTri((b as any).dateCloture) : this.dateTri(b.dateSinistre);
          if (dateAAncien && dateBAncien && dateAAncien !== dateBAncien) {
            return dateAAncien.localeCompare(dateBAncien);
          }
          if (dateAAncien && !dateBAncien) return -1;
          if (!dateAAncien && dateBAncien) return 1;
          return (a.idSinistre ?? 0) - (b.idSinistre ?? 0);
        case 'montantDesc':
          return (b.montantIndemnisation ?? 0) - (a.montantIndemnisation ?? 0);
        case 'montantAsc':
          return (a.montantIndemnisation ?? 0) - (b.montantIndemnisation ?? 0);
        default:
          const dateARecent = useCloture ? this.dateTri((a as any).dateCloture) : this.dateTri(a.dateSinistre);
          const dateBRecent = useCloture ? this.dateTri((b as any).dateCloture) : this.dateTri(b.dateSinistre);
          if (dateARecent && dateBRecent && dateARecent !== dateBRecent) {
            return dateBRecent.localeCompare(dateARecent);
          }
          if (dateARecent && !dateBRecent) return -1;
          if (!dateARecent && dateBRecent) return 1;
          return (b.idSinistre ?? 0) - (a.idSinistre ?? 0);
      }
    });
  }

  private nomAssure(d: any): string {
    return this.normaliser(`${d.assure?.nomAssure ?? d.nomAssure ?? ''} ${d.assure?.prenomAssure ?? d.prenomAssure ?? ''}`);
  }
}
