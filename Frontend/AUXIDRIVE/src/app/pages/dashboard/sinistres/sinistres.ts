import { Component, inject, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { SinistreService } from '../../../services/sinistre-service';
import { ContratService } from '../../../services/contrat-service';
import { VehiculeService } from '../../../services/vehicule-service';
import { Sinistre } from '../../../core/interfaces/sinistre';
import { Contrat } from '../../../core/interfaces/contrat';
import { Vehicule } from '../../../core/interfaces/vehicule';
import { MontantPipe } from '../../../core/pipes/montant.pipe';
import { DateFrPipe } from '../../../core/pipes/date-fr.pipe';

@Component({
  selector: 'app-dashboard-sinistres',
  imports: [FormsModule, MontantPipe, DateFrPipe],
  templateUrl: './sinistres.html',
  styleUrl: './sinistres.scss',
})
export class DashboardSinistres implements OnInit {
  private sinistreService = inject(SinistreService);
  private contratService = inject(ContratService);
  private vehiculeService = inject(VehiculeService);

  sinistres: Sinistre[] = [];
  contratsValides: Contrat[] = [];
  contratsEnAttente = 0;
  vehicules: Vehicule[] = [];

  chargement = true;
  erreur = '';
  messageSucces = '';
  enCours = false;

  // formulaire de déclaration
  formIdContrat: number | null = null;
  formIdVehicule: number | null = null;
  formDate = '';
  formLieu = '';
  formCirconstances = '';

  ngOnInit(): void {
    forkJoin({
      sinistres: this.sinistreService.mesSinistres(),
      contrats: this.contratService.mesContrats(),
      vehicules: this.vehiculeService.mesVehicules(),
    }).subscribe({
      next: ({ sinistres, contrats, vehicules }) => {
        this.sinistres = sinistres;
        this.contratsValides = contrats.filter((c) => c.statutContrat === 'VALIDE');
        this.contratsEnAttente = contrats.filter((c) => c.statutContrat === 'EN_ATTENTE').length;
        this.vehicules = vehicules;
        this.chargement = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger vos sinistres.';
        this.chargement = false;
      },
    });
  }

  get formulairePret(): boolean {
    return this.formIdContrat !== null && !!this.formDate;
  }

  declarer(): void {
    if (!this.formulairePret) {
      return;
    }
    this.erreur = '';
    this.messageSucces = '';
    this.enCours = true;

    this.sinistreService
      .declarer({
        idContrat: this.formIdContrat!,
        idVehicule: this.formIdVehicule,
        dateSinistre: this.formDate,
        lieuSinistre: this.formLieu || undefined,
        circonstances: this.formCirconstances || undefined,
      })
      .subscribe({
        next: (sinistre) => {
          this.messageSucces = `Sinistre n°${sinistre.idSinistre} déclaré avec succès.`;
          this.formIdContrat = null;
          this.formIdVehicule = null;
          this.formDate = '';
          this.formLieu = '';
          this.formCirconstances = '';
          this.enCours = false;
          this.recharger();
        },
        error: (err: HttpErrorResponse) => {
          this.erreur =
            err.error?.message ||
            'Déclaration impossible : vérifiez le contrat, la date et le véhicule sélectionnés.';
          this.enCours = false;
        },
      });
  }

  private recharger(): void {
    this.sinistreService.mesSinistres().subscribe({
      next: (sinistres) => (this.sinistres = sinistres),
    });
  }
}
