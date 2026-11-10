import { Component, inject, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ContratService } from '../../../services/contrat-service';
import { FactureService } from '../../../services/facture-service';
import { OffreService } from '../../../services/offre';
import { VehiculeService } from '../../../services/vehicule-service';
import { Contrat } from '../../../core/interfaces/contrat';
import { Facture } from '../../../core/interfaces/facture';
import { GarantieType } from '../../../core/interfaces/garantie';
import { Offre } from '../../../core/interfaces/offre';
import { Vehicule } from '../../../core/interfaces/vehicule';
import { MontantPipe } from '../../../core/pipes/montant.pipe';
import { DateFrPipe } from '../../../core/pipes/date-fr.pipe';
import { EtatVide } from '../../../components/etat-vide/etat-vide';
import { Squelette } from '../../../components/squelette/squelette';

@Component({
  selector: 'app-dashboard-contrats',
  imports: [FormsModule, MontantPipe, DateFrPipe, EtatVide, Squelette],
  templateUrl: './contrats.html',
  styleUrl: './contrats.scss',
})
export class DashboardContrats implements OnInit {
  private contratService = inject(ContratService);
  private factureService = inject(FactureService);
  private offreService = inject(OffreService);
  private vehiculeService = inject(VehiculeService);

  contrats: Contrat[] = [];
  offres: Offre[] = [];
  vehicules: Vehicule[] = [];
  /** Garanties proposées par l'offre sélectionnée. */
  garanties: GarantieType[] = [];

  chargement = true;
  erreur = '';
  erreurChoix = '';
  messageSucces = '';
  choixEnCours: number | null = null;
  dureesRenouvellement: Record<number, number | null> = {};

  // formulaire de souscription
  formIdOffre: number | null = null;
  formVehicules: number[] = [];
  formGaranties: string[] = [];
  formDureeMois: number | null = null;
  enCours = false;

  // détail de la prime d'un contrat validé
  contratDetail: Contrat | null = null;
  factureDetail: Facture | null = null;
  chargementDetail = false;
  erreurDetail = '';

  ngOnInit(): void {
    forkJoin({
      contrats: this.contratService.mesContrats(),
      offres: this.offreService.getOffres(),
      vehicules: this.vehiculeService.mesVehicules(),
    }).subscribe({
      next: ({ contrats, offres, vehicules }) => {
        this.contrats = contrats;
        this.initialiserDurees(contrats);
        this.offres = offres;
        this.vehicules = vehicules;
        this.chargement = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger vos contrats.';
        this.chargement = false;
      },
    });
  }

  get offreSelectionnee(): Offre | undefined {
    return this.offres.find((o) => o.idOffre === this.formIdOffre);
  }

  get vehiculesValides(): boolean {
    const min = this.offreSelectionnee?.nbVehiculesMin ?? 1;
    return this.formVehicules.length >= min;
  }

  get dureeValide(): boolean {
    const min = this.offreSelectionnee?.dureeMinMois ?? 1;
    return this.formDureeMois !== null && this.formDureeMois >= min;
  }

  get formulairePret(): boolean {
    return this.formIdOffre !== null && this.vehiculesValides && this.dureeValide;
  }

  /**
   * Au changement d'offre, la durée repart du minimum imposé et les garanties proposées par
   * l'offre (celles de sa branche) sont rechargées.
   */
  changerOffre(): void {
    this.formDureeMois = this.offreSelectionnee?.dureeMinMois ?? null;
    this.formGaranties = [];
    this.garanties = [];

    const idOffre = this.formIdOffre;
    if (idOffre === null) {
      return;
    }
    this.offreService.getGaranties(idOffre).subscribe({
      next: (garanties) => (this.garanties = garanties),
      error: () => (this.erreur = 'Impossible de charger les garanties de cette offre.'),
    });
  }

  basculerGarantie(cdGarantie: string): void {
    const index = this.formGaranties.indexOf(cdGarantie);
    if (index === -1) {
      this.formGaranties.push(cdGarantie);
    } else {
      this.formGaranties.splice(index, 1);
    }
  }

  basculerVehicule(id: number): void {
    const index = this.formVehicules.indexOf(id);
    if (index === -1) {
      this.formVehicules.push(id);
    } else {
      this.formVehicules.splice(index, 1);
    }
  }

  private demande() {
    return {
      idOffre: this.formIdOffre!,
      idVehicules: [...this.formVehicules],
      cdGaranties: [...this.formGaranties],
      dureeMois: this.formDureeMois!,
    };
  }

  souscrire(): void {
    if (!this.formulairePret) {
      return;
    }
    this.erreur = '';
    this.enCours = true;
    this.contratService.souscrire(this.demande()).subscribe({
      next: (resultat) => {
        this.messageSucces = `Demande n°${resultat.idContrat} transmise (${resultat.statut}).`;
        this.formIdOffre = null;
        this.formVehicules = [];
        this.formGaranties = [];
        this.garanties = [];
        this.formDureeMois = null;
        this.enCours = false;
        this.recharger();
      },
      error: (err: HttpErrorResponse) => {
        this.erreur = err.status === 409
          ? err.error?.detail || err.error?.message || 'Ce véhicule est déjà couvert par un contrat valide sur cette période.'
          : 'Souscription impossible : vérifiez les informations saisies.';
        this.enCours = false;
      },
    });
  }

  peutChoisirRenouvellement(contrat: Contrat): boolean {
    const maintenant = new Date();
    const aujourdHui = `${maintenant.getFullYear()}-${String(maintenant.getMonth() + 1).padStart(2, '0')}-${String(maintenant.getDate()).padStart(2, '0')}`;
    return contrat.statutContrat === 'VALIDE'
      && !!contrat.dateExpiration
      && contrat.dateExpiration > aujourdHui;
  }

  choisirRenouvellement(contrat: Contrat, caseACocher: HTMLInputElement): void {
    if (!this.peutChoisirRenouvellement(contrat) || this.choixEnCours !== null) return;
    const actif = caseACocher.checked;
    this.enregistrerRenouvellement(contrat, actif, null, caseACocher);
  }

  enregistrerDureeRenouvellement(contrat: Contrat): void {
    const duree = this.dureesRenouvellement[contrat.idContrat];
    if (!contrat.renouvellementAutomatique || !this.peutChoisirRenouvellement(contrat)
        || this.choixEnCours !== null || !Number.isInteger(duree) || duree == null || duree < 1) {
      this.erreurChoix = 'Saisissez une durée valide en mois.';
      return;
    }
    this.enregistrerRenouvellement(contrat, true, duree);
  }

  private enregistrerRenouvellement(
    contrat: Contrat, actif: boolean, dureeMois: number | null, caseACocher?: HTMLInputElement,
  ): void {
    this.choixEnCours = contrat.idContrat;
    this.erreurChoix = '';
    this.messageSucces = '';
    this.contratService.choisirRenouvellementAutomatique(contrat.idContrat, actif, dureeMois).subscribe({
      next: (misAJour) => {
        this.contrats = this.contrats.map((c) => c.idContrat === misAJour.idContrat ? misAJour : c);
        this.dureesRenouvellement[contrat.idContrat] = misAJour.dureeProchainRenouvellementMois ?? misAJour.dureeMois;
        this.messageSucces = dureeMois !== null
          ? `Durée de ${dureeMois} mois enregistrée pour le prochain renouvellement.`
          : actif ? 'Renouvellement automatique activé pour la prochaine échéance.'
          : 'Renouvellement automatique désactivé pour la prochaine échéance.';
        this.choixEnCours = null;
      },
      error: (erreur) => {
        if (caseACocher) caseACocher.checked = contrat.renouvellementAutomatique;
        this.erreurChoix = erreur?.error?.message || 'Impossible de modifier le renouvellement de ce contrat.';
        this.choixEnCours = null;
      },
    });
  }

  private initialiserDurees(contrats: Contrat[]): void {
    for (const contrat of contrats) {
      this.dureesRenouvellement[contrat.idContrat] = contrat.dureeProchainRenouvellementMois ?? contrat.dureeMois;
    }
  }

  // ---------- détail de la prime ----------

  /** Prime nette du contrat : base taxable de la facture moins les accessoires. */
  get primeNetteDetail(): number | null {
    if (!this.factureDetail || this.factureDetail.primeNette === null) {
      return null;
    }
    return this.factureDetail.primeNette - (this.factureDetail.accessoires ?? 0);
  }

  /**
   * Ouvre la modale de détail de la prime. Réservée aux contrats validés : un contrat en attente
   * ou refusé n'a pas de prime arrêtée. Le détail est repris de la facture émise à la validation,
   * donc sans recalcul.
   */
  ouvrirDetailPrime(contrat: Contrat): void {
    if (contrat.statutContrat !== 'VALIDE') {
      return;
    }
    this.contratDetail = contrat;
    this.factureDetail = null;
    this.erreurDetail = '';
    this.chargementDetail = true;

    this.factureService.facturesDuContrat(contrat.idContrat).subscribe({
      next: (factures) => {
        this.factureDetail = factures.length > 0 ? factures[factures.length - 1] : null;
        this.chargementDetail = false;
      },
      error: () => {
        this.erreurDetail = 'Impossible de charger le détail de la prime.';
        this.chargementDetail = false;
      },
    });
  }

  fermerDetailPrime(): void {
    this.contratDetail = null;
    this.factureDetail = null;
    this.erreurDetail = '';
    this.chargementDetail = false;
  }

  private recharger(): void {
    this.contratService.mesContrats().subscribe({
      next: (contrats) => {
        this.contrats = contrats;
        this.initialiserDurees(contrats);
      },
    });
  }
}
