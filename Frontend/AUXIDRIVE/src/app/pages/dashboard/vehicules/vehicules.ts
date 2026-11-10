import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, forkJoin, of } from 'rxjs';
import { VehiculeService } from '../../../services/vehicule-service';
import { CodeTarif, Vehicule } from '../../../core/interfaces/vehicule';
import { EtatVide } from '../../../components/etat-vide/etat-vide';
import { Squelette } from '../../../components/squelette/squelette';

@Component({
  selector: 'app-dashboard-vehicules',
  imports: [FormsModule, EtatVide, Squelette],
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
  selection = new Set<number>();
  confirmationSuppression = false;
  suppressionEnCours = false;
  messageSuppression = '';
  erreursSuppression: string[] = [];

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

  basculerSelection(id: number, coche: boolean): void {
    if (coche) {
      this.selection.add(id);
    } else {
      this.selection.delete(id);
    }
    this.confirmationSuppression = false;
  }

  supprimerSelection(): void {
    if (!this.confirmationSuppression || this.suppressionEnCours || !this.selection.size) return;

    const cibles = this.vehicules.filter((v) => this.selection.has(v.idVehicule));
    if (!cibles.length) {
      this.selection.clear();
      this.confirmationSuppression = false;
      return;
    }
    this.suppressionEnCours = true;
    this.confirmationSuppression = false;
    this.messageSuppression = '';
    this.erreursSuppression = [];

    forkJoin(
      cibles.map((v) =>
        this.vehiculeService.supprimer(v.idVehicule).pipe(
          catchError((erreur: HttpErrorResponse) => of({ erreur, idVehicule: v.idVehicule })),
        ),
      ),
    ).subscribe({
      next: (resultats) => {
        const supprimes = new Set<number>();
        resultats.forEach((resultat, index) => {
          if ('erreur' in resultat) {
            const vehicule = cibles[index];
            const raison = resultat.erreur.status === 409
              ? 'lié à un contrat, une facture ou une autre donnée existante'
              : resultat.erreur.status === 404
                ? 'introuvable ou ne vous appartient pas'
                : 'suppression impossible, veuillez réessayer';
            this.erreursSuppression.push(`${vehicule.numImmatriculation} : ${raison}.`);
          } else {
            supprimes.add(resultat.idVehicule);
            this.selection.delete(resultat.idVehicule);
          }
        });
        this.vehicules = this.vehicules.filter((v) => !supprimes.has(v.idVehicule));
        if (supprimes.size) {
          this.messageSuppression = `${supprimes.size} véhicule${supprimes.size > 1 ? 's' : ''} supprimé${supprimes.size > 1 ? 's' : ''}.`;
        }
        this.suppressionEnCours = false;
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
        this.selection = new Set([...this.selection].filter((id) => vehicules.some((v) => v.idVehicule === id)));
        this.chargement = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger vos véhicules.';
        this.chargement = false;
      },
    });
  }
}
