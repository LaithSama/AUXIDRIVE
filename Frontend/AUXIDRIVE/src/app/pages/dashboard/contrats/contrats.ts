import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ContratService } from '../../../services/contrat-service';
import { OffreService } from '../../../services/offre';
import { VehiculeService } from '../../../services/vehicule-service';
import { Contrat } from '../../../core/interfaces/contrat';
import { GarantieType } from '../../../core/interfaces/garantie';
import { Offre } from '../../../core/interfaces/offre';
import { Vehicule } from '../../../core/interfaces/vehicule';
import { DetailSimulation } from '../../../core/interfaces/demande-souscription';
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

  /** Prime nette fixe par mois : le montant total est ce tarif multiplié par la durée. */
  readonly primeMensuelleFixe = 20000;

  // formulaire de souscription
  formIdOffre: number | null = null;
  formVehicules: number[] = [];
  formGaranties: string[] = [];
  formDureeMois: number | null = null;
  simulation: DetailSimulation | null = null;
  enCours = false;

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

  /** Prime nette envoyée au serveur : tarif mensuel fixe x durée du contrat. */
  get primeNetteCalculee(): number {
    return this.primeMensuelleFixe * (this.formDureeMois ?? 0);
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
    this.simulation = null;
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
    this.simulation = null;
  }

  basculerVehicule(id: number): void {
    const index = this.formVehicules.indexOf(id);
    if (index === -1) {
      this.formVehicules.push(id);
    } else {
      this.formVehicules.splice(index, 1);
    }
    this.simulation = null;
  }

  private demande() {
    return {
      idOffre: this.formIdOffre!,
      idVehicules: [...this.formVehicules],
      cdGaranties: [...this.formGaranties],
      primeNette: this.primeNetteCalculee,
      dureeMois: this.formDureeMois!,
    };
  }

  simuler(): void {
    if (!this.formulairePret) {
      return;
    }
    this.erreur = '';
    this.enCours = true;
    this.contratService.simuler(this.demande()).subscribe({
      next: (detail) => {
        this.simulation = detail;
        this.enCours = false;
      },
      error: () => {
        this.erreur = 'Simulation impossible : vérifiez les informations saisies.';
        this.enCours = false;
      },
    });
  }

  souscrire(): void {
    if (!this.formulairePret || !this.simulation) {
      return;
    }
    this.erreur = '';
    this.enCours = true;
    this.contratService.souscrire(this.demande()).subscribe({
      next: (resultat) => {
        this.messageSucces = `Demande n°${resultat.idContrat} transmise (${resultat.statut}).`;
        this.simulation = null;
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

  private recharger(): void {
    this.contratService.mesContrats().subscribe({
      next: (contrats) => (this.contrats = contrats),
    });
  }
}
