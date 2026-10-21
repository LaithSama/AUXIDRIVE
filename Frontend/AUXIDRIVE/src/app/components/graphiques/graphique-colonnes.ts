import { Component, computed, input } from '@angular/core';
import {
  SerieColonnes,
  abregerMontant,
  abregerNombre,
  formaterMontant,
  formaterNombre,
} from '../../core/charts/chart-series';

/** Colonne positionnée dans le viewBox. */
interface ColonneCalculee {
  x: number;
  y: number;
  largeur: number;
  hauteur: number;
  valeur: string;
  couleur: string;
}

/** Graduation de l'axe vertical. */
interface GraduationCalculee {
  y: number;
  libelle: string;
}

/**
 * Colonnes groupées en SVG : une catégorie par période, une colonne par
 * série. L'effectif est écrit au-dessus de chaque colonne pour que le
 * graphique se lise sans survol.
 */
@Component({
  selector: 'app-graphique-colonnes',
  templateUrl: './graphique-colonnes.html',
  styleUrl: './graphique-colonnes.scss',
})
export class GraphiqueColonnes {
  readonly categories = input<string[]>([]);
  readonly series = input<SerieColonnes[]>([]);
  /** Borné haute de l'axe vertical. */
  readonly maximum = input(1);
  /** Format des valeurs affichées : effectif ou montant. */
  readonly format = input<'nombre' | 'montaire'>('nombre');
  /** Titre lu par les lecteurs d'écran. */
  readonly titre = input('');
  /** Résumé chiffré lu par les lecteurs d'écran. */
  readonly description = input('');
  readonly messageVide = input('Aucune donnée à afficher.');

  protected readonly margeGauche = 34;
  protected readonly margeDroite = 6;
  protected readonly margeHaute = 20;
  protected readonly margeBasse = 30;
  protected readonly largeurTotale = 320;
  protected readonly hauteurTotale = 210;
  protected readonly largeurUtile = this.largeurTotale - this.margeGauche - this.margeDroite;
  protected readonly hauteurUtile = this.hauteurTotale - this.margeHaute - this.margeBasse;

  /** Colonnes regroupées par catégorie. */
  protected readonly groupes = computed<ColonneCalculee[][]>(() => {
    const categories = this.categories();
    const series = this.series();
    if (!categories.length || !series.length) {
      return [];
    }

    const maximum = Math.max(this.maximum(), 1);
    const largeurGroupe = this.largeurUtile / categories.length;
    const nombreSeries = series.length;
    const largeurBarre = Math.min(15, (largeurGroupe - 8) / nombreSeries - 2);
    const ecart = 2;
    const total = nombreSeries * largeurBarre + (nombreSeries - 1) * ecart;

    return categories.map((_, indexCategorie) => {
      const depart = this.margeGauche
        + indexCategorie * largeurGroupe
        + (largeurGroupe - total) / 2;

      return series.map((serie, indexSerie) => {
        const valeur = serie.valeurs[indexCategorie] ?? 0;
        const hauteur = Math.max(0, Math.min(1, valeur / maximum)) * this.hauteurUtile;
        return {
          x: depart + indexSerie * (largeurBarre + ecart),
          y: this.margeHaute + this.hauteurUtile - hauteur,
          largeur: largeurBarre,
          hauteur,
          valeur: this.formater(valeur),
          couleur: serie.couleur,
        };
      });
    });
  });

  /** Graduations de l'axe vertical : 0, moitié, maximum. */
  protected readonly axeY = computed<GraduationCalculee[]>(() => {
    const maximum = Math.max(this.maximum(), 1);
    return [0, maximum / 2, maximum].map(valeur => ({
      y: this.margeHaute + this.hauteurUtile - (valeur / maximum) * this.hauteurUtile,
      libelle: this.format() === 'montaire' ? abregerMontant(valeur) : abregerNombre(valeur),
    }));
  });

  /** Position des libellés de l'axe horizontal. */
  protected readonly axeX = computed(() => {
    const categories = this.categories();
    const largeurGroupe = this.largeurUtile / Math.max(categories.length, 1);
    return categories.map((categorie, index) => ({
      libelle: categorie,
      x: this.margeGauche + index * largeurGroupe + largeurGroupe / 2,
      y: this.hauteurTotale - 10,
    }));
  });

  protected readonly aDesDonnees = computed(() =>
    this.series().some(s => s.valeurs.some(v => v > 0))
  );

  private formater(valeur: number): string {
    return this.format() === 'montaire' ? formaterMontant(valeur) : formaterNombre(valeur);
  }
}
