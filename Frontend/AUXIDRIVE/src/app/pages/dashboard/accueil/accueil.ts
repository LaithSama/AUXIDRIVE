import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ContratService } from '../../../services/contrat-service';
import { SinistreService } from '../../../services/sinistre-service';
import { FactureService } from '../../../services/facture-service';
import { AuthService } from '../../../services/auth-service';
import { Contrat } from '../../../core/interfaces/contrat';
import { Sinistre } from '../../../core/interfaces/sinistre';
import { Facture } from '../../../core/interfaces/facture';

@Component({
  selector: 'app-dashboard-accueil',
  imports: [RouterLink],
  templateUrl: './accueil.html',
  styleUrl: './accueil.scss',
})
export class DashboardAccueil implements OnInit {
  private contratService = inject(ContratService);
  private sinistreService = inject(SinistreService);
  private factureService = inject(FactureService);
  private authService = inject(AuthService);
  private router = inject(Router);

  chargement = true;
  erreur = '';

  contratsActifs = 0;
  contratsEnAttente = 0;
  sinistresEnCours = 0;
  facturesEmises = 0;
  totalFactures = 0;

  ngOnInit(): void {
    forkJoin({
      contrats: this.contratService.mesContrats(),
      sinistres: this.sinistreService.mesSinistres(),
      factures: this.factureService.mesFactures(),
    }).subscribe({
      next: ({ contrats, sinistres, factures }) => {
        this.contratsActifs = contrats.filter((c: Contrat) => c.statutContrat === 'VALIDE').length;
        this.contratsEnAttente = contrats.filter((c: Contrat) => c.statutContrat === 'EN_ATTENTE').length;
        this.sinistresEnCours = sinistres.filter(
          (s: Sinistre) => s.statutSinistre === 'DECLARE' || s.statutSinistre === 'RAPPORT_EN_COURS'
        ).length;
        this.facturesEmises = factures.filter((f: Facture) => f.statutFacture === 'EMISE').length;
        this.totalFactures = factures.length;
        this.chargement = false;
      },
      error: (err) => {
        if (err.status === 401 || err.status === 403) {
          this.authService.logout();
          this.router.navigate(['/connexion']);
          return;
        }
        this.erreur = "Impossible de charger votre tableau de bord. Vérifiez que vous êtes connecté en tant que client.";
        this.chargement = false;
      },
    });
  }
}
