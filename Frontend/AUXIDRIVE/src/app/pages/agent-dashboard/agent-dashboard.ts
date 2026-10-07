import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Contrat, StatutContrat } from '../../core/interfaces/contrat';
import { Sinistre } from '../../core/interfaces/sinistre';
import { MontantPipe } from '../../core/pipes/montant.pipe';
import { DateFrPipe } from '../../core/pipes/date-fr.pipe';

type VueAgent = 'inscriptions' | 'contrats' | 'sinistres';
type OngletSinistre = 'DECLARE' | 'RAPPORT_EN_COURS' | 'CLOTURES';

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [FormsModule, MontantPipe, DateFrPipe],
  templateUrl: './agent-dashboard.html',
  styleUrl: './agent-dashboard.scss',
})
export class AgentDashboard implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = 'http://localhost:8080/api';

  /** Espace affiché : inscriptions, contrats ou sinistres. */
  vue = signal<VueAgent>('inscriptions');

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
    localStorage.clear();
    this.router.navigate(['/connexion']);
  }
}
