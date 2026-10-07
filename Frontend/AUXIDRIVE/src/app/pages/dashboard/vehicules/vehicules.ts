import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { VehiculeService } from '../../../services/vehicule-service';
import { CodeTarif, Vehicule } from '../../../core/interfaces/vehicule';

@Component({
  selector: 'app-dashboard-vehicules',
  imports: [FormsModule],
  templateUrl: './vehicules.html',
  styleUrl: './vehicules.scss',
})
export class DashboardVehicules implements OnInit {
  private vehiculeService = inject(VehiculeService);

  vehicules: Vehicule[] = [];
  chargement = true;
  erreur = '';
  messageSucces = '';
  enCours = false;

  // formulaire d'ajout
  formImmatriculation = '';
  formChassis = '';
  formMarque = '';
  formModele = '';
  formEnergie = '';
  formPlaces: number | null = null;
  formPuissance: number | null = null;
  formMoteur = '';
  formType = '';
  formUsage = '';
  formCodeTarif: number | null = null;

  codesTarifaires: CodeTarif[] = [];

  readonly energies = ['Essence', 'Diesel', 'Électrique', 'Hybride', 'GPL'];

  ngOnInit(): void {
    forkJoin({
      vehicules: this.vehiculeService.mesVehicules(),
      codes: this.vehiculeService.codesTarifaires(),
    }).subscribe({
      next: ({ vehicules, codes }) => {
        this.vehicules = vehicules;
        this.codesTarifaires = codes;
        this.chargement = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger vos véhicules.';
        this.chargement = false;
      },
    });
  }

  get formulairePret(): boolean {
    return (
      !!this.formImmatriculation.trim() &&
      !!this.formChassis.trim() &&
      !!this.formMarque.trim() &&
      !!this.formModele.trim() &&
      !!this.formEnergie &&
      this.formPlaces !== null &&
      this.formPlaces > 0 &&
      this.formPuissance !== null &&
      this.formPuissance > 0 &&
      this.formCodeTarif !== null
    );
  }

  ajouter(): void {
    if (!this.formulairePret) {
      return;
    }
    this.erreur = '';
    this.messageSucces = '';
    this.enCours = true;

    this.vehiculeService
      .ajouter({
        numImmatriculation: this.formImmatriculation.trim(),
        numChassis: this.formChassis.trim(),
        marque: this.formMarque.trim(),
        modele: this.formModele.trim(),
        energie: this.formEnergie,
        nombrePlaces: this.formPlaces!,
        puissanceFiscale: this.formPuissance!,
        numMoteur: this.formMoteur.trim() || null,
        typeVehicule: this.formType.trim() || null,
        usageVehicule: this.formUsage.trim() || null,
        codeTarif: { idCodeAuto: this.formCodeTarif! },
      })
      .subscribe({
        next: (vehicule) => {
          this.messageSucces = `Véhicule ${vehicule.marque} ${vehicule.modele} (${vehicule.numImmatriculation}) ajouté.`;
          this.reinitialiser();
          this.enCours = false;
          this.recharger();
        },
        error: (err: HttpErrorResponse) => {
          this.erreur = this.messageHttp(err);
          this.enCours = false;
        },
      });
  }

  private messageHttp(err: HttpErrorResponse): string {
    if (err.status === 409) {
      return "Ce numéro d'immatriculation ou de châssis est déjà enregistré.";
    }
    if (err.status === 400) {
      return 'Veuillez remplir correctement tous les champs obligatoires.';
    }
    if (err.status === 403) {
      return 'Action refusée : ce formulaire est réservé aux clients.';
    }
    return "Impossible d'ajouter le véhicule. Vérifiez votre connexion puis réessayez.";
  }

  private reinitialiser(): void {
    this.formImmatriculation = '';
    this.formChassis = '';
    this.formMarque = '';
    this.formModele = '';
    this.formEnergie = '';
    this.formPlaces = null;
    this.formPuissance = null;
    this.formMoteur = '';
    this.formType = '';
    this.formUsage = '';
    this.formCodeTarif = null;
  }

  private recharger(): void {
    this.vehiculeService.mesVehicules().subscribe({
      next: (vehicules) => {
        this.vehicules = vehicules;
        this.chargement = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger vos véhicules.';
        this.chargement = false;
      },
    });
  }
}
