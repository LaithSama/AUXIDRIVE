import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Contrat, StatutContrat } from '../../core/interfaces/contrat';
import { Sinistre } from '../../core/interfaces/sinistre';
import { MontantPipe } from '../../core/pipes/montant.pipe';
import { DateFrPipe } from '../../core/pipes/date-fr.pipe';
import { AuthService } from '../../services/auth-service';
import { ProfilStaffPanel } from '../../components/profil-staff/profil-staff';

type VueAgent = 'inscriptions' | 'contrats' | 'sinistres' | 'profil';
type OngletSinistre = 'DECLARE' | 'RAPPORT_EN_COURS' | 'CLOTURES';

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [FormsModule, MontantPipe, DateFrPipe, ProfilStaffPanel],
  templateUrl: './agent-dashboard.html',
  styleUrl: './agent-dashboard.scss',
})
export class AgentDashboard implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  private apiUrl = 'http://localhost:8080/api';

  /** Espace affiché : inscriptions, contrats ou sinistres. */
  vue = signal<VueAgent>('inscriptions');

  /** Sidebar dépliable (mobile). */
  sidebarOuverte = signal(false);

  /** E-mail de l'agent connecté, affiché dans le pied de la sidebar. */
  emailAgent = this.authService.getEmail() ?? 'Agent';

  // ---------- Inscriptions ----------
  private apiUrlInscriptions = `${this.apiUrl}/inscriptions`;

  toutesLesInscriptions = signal<any[]>([]);
  ongletActif = signal<'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE'>('EN_ATTENTE');

  demandesEnAttente = computed(() =>
    this.toutesLesInscriptions().filter(d => d.statutInscription === 'EN_ATTENTE'));
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

  demandeSelectionnee: any = null;
  numAssureSaisi = '';
  motifRefusSaisi = '';
  modeAction: 'VALIDER' | 'REFUSER' | null = null;

  // ---------- Contrats ----------
  contrats = signal<Contrat[]>([]);
  ongletContrat = signal<StatutContrat>('EN_ATTENTE');

  contratsAttente = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'EN_ATTENTE'));
  contratsValides = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'VALIDE'));
  contratsRefuses = computed(() =>
    this.contrats().filter(c => c.statutContrat === 'REFUSE'));

  contratSelectionne: Contrat | null = null;
  modeContrat: 'VALIDER' | 'REFUSER' | null = null;

  // Filtres contrats
  rechercheContrat = signal('');
  primeMinContrat = signal('');
  primeMaxContrat = signal('');
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
      : this.contratsRefuses();

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

  // ---------- Sinistres ----------
  sinistres = signal<Sinistre[]>([]);
  ongletSinistre = signal<OngletSinistre>('DECLARE');

  sinistresDeclares = computed(() =>
    this.sinistres().filter(s => s.statutSinistre === 'DECLARE'));
  sinistresEnCours = computed(() =>
    this.sinistres().filter(s => s.statutSinistre === 'RAPPORT_EN_COURS'));
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
  montantMinSinistre = signal('');
  montantMaxSinistre = signal('');
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

  // Formulaire de rapport policier
  numRapportSaisi = '';
  dateRapportSaisie = '';
  rapportSaisi = '';
  responsabiliteSaisie: boolean | null = null;

  // Formulaire de clôture
  montantSaisi = '';

  messageSucces = '';
  messageErreur = '';

  ngOnInit(): void {
    this.chargerInitialisation();
    this.chargerContrats();
    this.chargerSinistres();
  }

  /** Récupère l'intégralité des dossiers auprès du backend */
  chargerInitialisation(): void {
    this.http.get<any[]>(this.apiUrlInscriptions).subscribe({
      next: (liste) => this.toutesLesInscriptions.set(liste),
      error: (err) => this.erreurChargement(err),
    });
  }

  chargerContrats(): void {
    this.http.get<Contrat[]>(`${this.apiUrl}/contrats`).subscribe({
      next: (liste) => this.contrats.set(liste),
      error: (err) => this.erreurChargement(err),
    });
  }

  chargerSinistres(): void {
    this.http.get<Sinistre[]>(`${this.apiUrl}/sinistres`).subscribe({
      next: (liste) => this.sinistres.set(liste),
      error: (err) => this.erreurChargement(err),
    });
  }

  private erreurChargement(err: any): void {
    console.error('Erreur lors de la récupération :', err);
    if (err.status === 401 || err.status === 403) {
      this.deconnexion();
    }
  }

  // ---------- Navigation ----------

  changerVue(vue: VueAgent): void {
    this.vue.set(vue);
    this.messageSucces = '';
    this.messageErreur = '';
    this.fermerSidebar();
  }

  basculerSidebar(): void {
    this.sidebarOuverte.update(ouverte => !ouverte);
  }

  fermerSidebar(): void {
    this.sidebarOuverte.set(false);
  }

  changerOnglet(statut: 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE'): void {
    this.ongletActif.set(statut);
    this.messageSucces = '';
    this.messageErreur = '';
  }

  changerOngletContrat(statut: StatutContrat): void {
    this.ongletContrat.set(statut);
    this.messageSucces = '';
    this.messageErreur = '';
  }

  changerOngletSinistre(onglet: OngletSinistre): void {
    this.ongletSinistre.set(onglet);
    this.messageSucces = '';
    this.messageErreur = '';
  }

  // ---------- Modale inscriptions ----------

  ouvrirModale(demande: any, mode: 'VALIDER' | 'REFUSER'): void {
    this.demandeSelectionnee = demande;
    this.modeAction = mode;
    this.numAssureSaisi = '';
    this.motifRefusSaisi = '';
    this.messageSucces = '';
    this.messageErreur = '';
  }

  fermerModale(): void {
    this.demandeSelectionnee = null;
    this.modeAction = null;
    this.contratSelectionne = null;
    this.modeContrat = null;
    this.sinistreSelectionne = null;
    this.modeSinistre = null;
  }

  /** POST /api/inscriptions/{id}/valider?numerosAssure=... */
  confirmerValidation(): void {
    if (!this.numAssureSaisi.trim() || !this.demandeSelectionnee) return;

    const id = this.demandeSelectionnee.idInscription;

    this.http.post(`${this.apiUrlInscriptions}/${id}/valider`, null, {
      params: { numerosAssure: this.numAssureSaisi.trim() }
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

  // ---------- Contrats : validation ----------

  ouvrirModaleContrat(contrat: Contrat, mode: 'VALIDER' | 'REFUSER'): void {
    this.contratSelectionne = contrat;
    this.modeContrat = mode;
    this.messageSucces = '';
    this.messageErreur = '';
  }

  /** POST /api/contrats/{id}/valider ou /refuser */
  confirmerContrat(): void {
    const contrat = this.contratSelectionne;
    if (!contrat || !this.modeContrat) return;

    const action = this.modeContrat === 'VALIDER' ? 'valider' : 'refuser';

    this.http.post(`${this.apiUrl}/contrats/${contrat.idContrat}/${action}`, null).subscribe({
      next: () => {
        this.messageSucces = this.modeContrat === 'VALIDER'
          ? `Contrat #${contrat.idContrat} validé : numéro de police attribué, facture émise.`
          : `Contrat #${contrat.idContrat} refusé.`;
        this.chargerContrats();
        setTimeout(() => this.fermerModale(), 1500);
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = err.status === 409
          ? 'Seule une demande encore en attente peut être traitée.'
          : 'Impossible de traiter ce contrat.';
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
    return this.montantSaisi !== '' && Number(this.montantSaisi) >= 0;
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

  reinitialiserFiltres(): void {
    switch (this.vue()) {
      case 'contrats':
        this.rechercheContrat.set('');
        this.primeMinContrat.set('');
        this.primeMaxContrat.set('');
        this.canalContrat.set('');
        this.echeanceProche.set(false);
        this.triContrat.set('recent');
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
        break;
      default:
        this.rechercheInscription.set('');
        this.filtreCin.set('tous');
        this.dateDemandeDepuis.set('');
        this.dateDemandeJusqua.set('');
        this.triInscription.set('recent');
    }
  }

  private normaliser(valeur: unknown): string {
    return (valeur ?? '').toString().toLowerCase().trim();
  }

  private correspond(texte: string, requete: string): boolean {
    const q = this.normaliser(requete);
    return q === '' || texte.includes(q);
  }

  private nombreOuNull(valeur: string): number | null {
    const brut = valeur.trim();
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

  private trierInscriptions(liste: any[]): any[] {
    return [...liste].sort((a, b) => {
      switch (this.triInscription()) {
        case 'ancien':
          return this.dateIso(a.dateDemande).localeCompare(this.dateIso(b.dateDemande));
        case 'nom':
          return this.nomAssure(a).localeCompare(this.nomAssure(b));
        default:
          return this.dateIso(b.dateDemande).localeCompare(this.dateIso(a.dateDemande));
      }
    });
  }

  private trierContrats(liste: Contrat[]): Contrat[] {
    return [...liste].sort((a, b) => {
      switch (this.triContrat()) {
        case 'ancien':
          return this.dateIso(a.dateSouscription).localeCompare(this.dateIso(b.dateSouscription));
        case 'primeDesc':
          return (b.primeTtcCourante ?? 0) - (a.primeTtcCourante ?? 0);
        case 'primeAsc':
          return (a.primeTtcCourante ?? 0) - (b.primeTtcCourante ?? 0);
        default:
          return this.dateIso(b.dateSouscription).localeCompare(this.dateIso(a.dateSouscription));
      }
    });
  }

  private trierSinistres(liste: Sinistre[]): Sinistre[] {
    return [...liste].sort((a, b) => {
      switch (this.triSinistre()) {
        case 'ancien':
          return this.dateIso(a.dateSinistre).localeCompare(this.dateIso(b.dateSinistre));
        case 'montantDesc':
          return (b.montantIndemnisation ?? 0) - (a.montantIndemnisation ?? 0);
        case 'montantAsc':
          return (a.montantIndemnisation ?? 0) - (b.montantIndemnisation ?? 0);
        default:
          return this.dateIso(b.dateSinistre).localeCompare(this.dateIso(a.dateSinistre));
      }
    });
  }

  private nomAssure(d: any): string {
    return this.normaliser(`${d.assure?.nomAssure ?? d.nomAssure ?? ''} ${d.assure?.prenomAssure ?? d.prenomAssure ?? ''}`);
  }
}
