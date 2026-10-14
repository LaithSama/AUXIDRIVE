import { Component, inject, OnInit } from '@angular/core';
import { FactureService } from '../../../services/facture-service';
import { Facture } from '../../../core/interfaces/facture';
import { MontantPipe } from '../../../core/pipes/montant.pipe';
import { DateFrPipe } from '../../../core/pipes/date-fr.pipe';

@Component({
  selector: 'app-dashboard-factures',
  imports: [MontantPipe, DateFrPipe],
  templateUrl: './factures.html',
  styleUrl: './factures.scss',
})
export class DashboardFactures implements OnInit {
  private factureService = inject(FactureService);

  factures: Facture[] = [];
  chargement = true;
  erreur = '';
  telechargementId: number | null = null;

  private static readonly LIBELLES_TYPES: Record<string, string> = {
    SOUSCRIPTION: 'Souscription',
    RENOUVELLEMENT: 'Renouvellement',
    MAJORATION: 'Majoration',
    RESILIATION: 'Résiliation',
  };

  libelleType(type: string | undefined): string {
    return type ? DashboardFactures.LIBELLES_TYPES[type] ?? type : '—';
  }

  ngOnInit(): void {
    this.factureService.mesFactures().subscribe({
      next: (factures) => {
        this.factures = factures;
        this.chargement = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger vos factures.';
        this.chargement = false;
      },
    });
  }

  telechargerPdf(facture: Facture): void {
    this.erreur = '';
    this.telechargementId = facture.idFacture;
    this.factureService.telechargerPdf(facture.idFacture).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const lien = document.createElement('a');
        lien.href = url;
        lien.download = `facture-${facture.numFacture || facture.idFacture}.pdf`;
        lien.click();
        URL.revokeObjectURL(url);
        this.telechargementId = null;
      },
      error: () => {
        this.erreur = `PDF indisponible pour la facture n°${facture.idFacture}.`;
        this.telechargementId = null;
      },
    });
  }
}
