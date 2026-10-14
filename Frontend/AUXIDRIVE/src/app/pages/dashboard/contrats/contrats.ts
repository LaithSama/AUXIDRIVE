import { Component, inject, OnInit } from '@angular/core';
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

@Component({
  selector: 'app-dashboard-contrats',
  imports: [FormsModule, MontantPipe, DateFrPipe],
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
  messageSucces = '';

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
      error: () => {
        this.erreur = 'Souscription impossible : vérifiez les informations saisies.';
        this.enCours = false;
      },
    });
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
      next: (contrats) => (this.contrats = contrats),
    });
  }
}
