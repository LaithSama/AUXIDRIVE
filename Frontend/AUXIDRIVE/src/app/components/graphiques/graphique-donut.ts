import { Component, computed, input } from '@angular/core';
import { PartSerie, formaterMontant, formaterNombre } from '../../core/charts/chart-series';
import { GraphiqueLegende, ItemLegende } from './graphique-legende';

/** Arc calculé du donut : longueur et décalage sur la circonférence. */
interface SegmentDonut {
  libelle: string;
  couleur: string;
  pourcentage: number;
  /** Longueur de l'arc, en unités du viewBox. */
  longueur: number;
  /** Décalage cumulé sur la circonférence (négatif : avance sur le tracé). */
  decalage: number;
}

/**
 * Donut en SVG pur : chaque part est un arc stroked sur une même circonférence.
 * L'effectif total est affiché au centre, la légende sous le graphique.
 */
@Component({
  selector: 'app-graphique-donut',
  imports: [GraphiqueLegende],
  templateUrl: './graphique-donut.html',
  styleUrl: './graphique-donut.scss',
})
export class GraphiqueDonut {
  readonly parts = input<PartSerie[]>([]);
  /** Format des valeurs affichées : effectif ou montant. */
  readonly format = input<'nombre' | 'montaire'>('nombre');
  /** Valeur affichée au centre (effectif ou montant). */
  readonly valeurCentre = input('');
  /** Libellé affiché sous la valeur centrale. */
  readonly libelleCentre = input('');
  /** Résumé chiffré lu par les lecteurs d'écran. */
  readonly description = input('');
  readonly messageVide = input('Aucune donnée à afficher.');

  protected readonly rayon = 76;
  protected readonly epaisseur = 26;
  protected readonly circonference = 2 * Math.PI * this.rayon;

  /** Écart entre deux arcs, en unités du viewBox. */
  private readonly ecart = 3;

  protected readonly segments = computed<SegmentDonut[]>(() => {
    const parts = this.parts();
    let cumule = 0;
    return parts.map(p => {
      const brute = p.part * this.circonference;
      const longueur = brute <= 0
        ? 0
        : parts.length > 1 ? Math.max(1, brute - this.ecart) : brute;
      const segment: SegmentDonut = {
        libelle: p.libelle,
        couleur: p.couleur,
        pourcentage: p.pourcentage,
        longueur,
        decalage: -cumule,
      };
      cumule += brute;
      return segment;
    });
  });

  protected readonly items = computed<ItemLegende[]>(() =>
    this.parts().map(p => ({
      libelle: p.libelle,
      couleur: p.couleur,
      valeur: this.format() === 'montaire' ? formaterMontant(p.valeur) : formaterNombre(p.valeur),
      pourcentage: p.pourcentage,
    }))
  );

  protected readonly aDesDonnees = computed(() => this.parts().some(p => p.valeur > 0));
}
