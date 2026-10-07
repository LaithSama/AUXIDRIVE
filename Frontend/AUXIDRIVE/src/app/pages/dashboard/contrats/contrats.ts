import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ContratService } from '../../../services/contrat-service';
import { OffreService } from '../../../services/offre';
import { VehiculeService } from '../../../services/vehicule-service';
import { Contrat } from '../../../core/interfaces/contrat';
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

  chargement = true;
  erreur = '';
  messageSucces = '';

  // formulaire de souscription
  formIdOffre: number | null = null;
  formVehicules: number[] = [];
  formPrimeNette: number | null = null;
  formDateEffet = '';
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

  get vehiculesValides(): boolean {
    const min = this.offreSelectionnee?.nbVehiculesMin ?? 1;
    return this.formVehicules.length >= min;
  }

  get formulairePret(): boolean {
    return (
      this.formIdOffre !== null &&
      this.vehiculesValides &&
      this.formPrimeNette !== null &&
      this.formPrimeNette > 0
    );
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
      primeNette: this.formPrimeNette!,
      dateEffet: this.formDateEffet || undefined,
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
        this.formPrimeNette = null;
        this.formDateEffet = '';
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
