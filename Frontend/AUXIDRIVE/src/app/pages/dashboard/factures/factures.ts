import { Component, HostListener, inject, OnDestroy, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
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
export class DashboardFactures implements OnInit, OnDestroy {
  private factureService = inject(FactureService);
  private sanitizer = inject(DomSanitizer);

  factures: Facture[] = [];
  chargement = true;
  erreur = '';
  telechargementId: number | null = null;
  factureApercu: Facture | null = null;
  chargementApercu = false;
  erreurApercu = '';
  apercuUrl: SafeResourceUrl | null = null;
  private apercuObjectUrl: string | null = null;
  private apercuSubscription: Subscription | null = null;

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

  voirPdf(facture: Facture): void {
    this.fermerApercu();
    this.factureApercu = facture;
    this.chargementApercu = true;
    this.apercuSubscription = this.factureService.telechargerPdf(facture.idFacture).subscribe({
      next: (blob) => {
        // Le PDF est récupéré avec le jeton d'authentification, puis affiché localement.
        this.apercuObjectUrl = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
        this.apercuUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.apercuObjectUrl);
        this.chargementApercu = false;
      },
      error: () => {
        this.erreurApercu = `PDF indisponible pour la facture n°${facture.idFacture}.`;
        this.chargementApercu = false;
      },
    });
  }

  fermerApercu(): void {
    this.apercuSubscription?.unsubscribe();
    this.apercuSubscription = null;
    this.factureApercu = null;
    this.apercuUrl = null;
    this.chargementApercu = false;
    this.erreurApercu = '';
    if (this.apercuObjectUrl) {
      URL.revokeObjectURL(this.apercuObjectUrl);
      this.apercuObjectUrl = null;
    }
  }

  @HostListener('document:keydown.escape')
  fermerApercuAvecEchap(): void {
    if (this.factureApercu) this.fermerApercu();
  }

  ngOnDestroy(): void {
    this.fermerApercu();
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
