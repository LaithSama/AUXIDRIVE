import { Component, computed, input } from '@angular/core';
import { PartSerie, formaterMontant, formaterNombre } from '../../core/charts/chart-series';

/** Barre horizontale positionnée dans le viewBox. */
interface BarreCalculee {
  libelle: string;
  libelleAxe: string;
  valeur: string;
  couleur: string;
  pourcentage: number;
  /** Position verticale du haut de la barre. */
  y: number;
  hauteur: number;
  /** Longueur de la barre, proportionnelle à la plus grande valeur. */
  largeur: number;
}

/**
 * Barres horizontales en SVG : une ligne par mesure, la plus longue servant
 * de référence. La valeur est écrite en bout de barre pour rester lisible
 * sans survol.
 */
@Component({
  selector: 'app-graphique-barres',
  templateUrl: './graphique-barres.html',
  styleUrl: './graphique-barres.scss',
})
export class GraphiqueBarres {
  readonly parts = input<PartSerie[]>([]);
  /** Format des valeurs affichées : effectif ou montant. */
  readonly format = input<'nombre' | 'montaire'>('nombre');
  /** Titre lu par les lecteurs d'écran. */
  readonly titre = input('');
  /** Résumé chiffré lu par les lecteurs d'écran. */
  readonly description = input('');
  readonly messageVide = input('Aucune donnée à afficher.');

  protected readonly margeGauche = 88;
  protected readonly margeDroite = 44;
  protected readonly margeHaute = 8;
  protected readonly margeBasse = 8;
  protected readonly largeurTotale = 320;
  protected readonly hauteurTotale = 200;
  protected readonly largeurUtile = this.largeurTotale - this.margeGauche - this.margeDroite;
  protected readonly hauteurUtile = this.hauteurTotale - this.margeHaute - this.margeBasse;

  protected readonly barres = computed<BarreCalculee[]>(() => {
    const parts = this.parts();
    const hauteurLigne = this.hauteurUtile / Math.max(parts.length, 1);
    const hauteurBarre = Math.min(24, hauteurLigne * 0.52);

    return parts.map((p, index) => {
      const largeur = p.part * this.largeurUtile;
      const y = this.margeHaute + index * hauteurLigne + (hauteurLigne - hauteurBarre) / 2;
      return {
        libelle: p.libelle,
        libelleAxe: this.abreger(p.libelle),
        valeur: this.format() === 'montaire' ? formaterMontant(p.valeur) : formaterNombre(p.valeur),
        couleur: p.couleur,
        pourcentage: p.pourcentage,
        y,
        hauteur: hauteurBarre,
        largeur,
      };
    });
  });

  protected readonly aDesDonnees = computed(() => this.parts().some(p => p.valeur > 0));

  /** Un libellé trop long est tronqué pour ne pas déborder sur la barre. */
  private abreger(libelle: string): string {
    return libelle.length > 16 ? `${libelle.slice(0, 15)}…` : libelle;
  }
}
