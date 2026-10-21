import { Component, computed, input } from '@angular/core';
import {
  abregerMontant,
  abregerNombre,
  formaterMontant,
  formaterNombre,
} from '../../core/charts/chart-series';

/** Point de la courbe, positionné dans le viewBox. */
interface PointCalcule {
  x: number;
  y: number;
  valeur: number;
}

/** Étiquette de l'axe horizontal. */
interface EtiquetteX {
  libelle: string;
  x: number;
  y: number;
  visible: boolean;
}

/** Graduation de l'axe vertical. */
interface GraduationCalculee {
  y: number;
  libelle: string;
}

/**
 * Aire en SVG : la courbe est remplie d'un dégradé natif et le dernier point
 * est mis en avant. La valeur la plus récente est rappelée sous le graphique.
 */
@Component({
  selector: 'app-graphique-aire',
  templateUrl: './graphique-aire.html',
  styleUrl: './graphique-aire.scss',
})
export class GraphiqueAire {
  readonly categories = input<string[]>([]);
  readonly libellesComplets = input<string[]>([]);
  readonly valeurs = input<number[]>([]);
  /** Borné haute de l'axe vertical. */
  readonly maximum = input(1);
  /** Couleur de la courbe (variable CSS du thème). */
  readonly couleur = input('var(--lagon)');
  /** Format des valeurs affichées : effectif ou montant. */
  readonly format = input<'nombre' | 'montaire'>('nombre');
  /** Titre lu par les lecteurs d'écran. */
  readonly titre = input('');
  /** Résumé chiffré lu par les lecteurs d'écran. */
  readonly description = input('');
  readonly messageVide = input('Aucune donnée à afficher.');

  protected readonly margeGauche = 44;
  protected readonly margeDroite = 8;
  protected readonly margeHaute = 18;
  protected readonly margeBasse = 28;
  protected readonly largeurTotale = 320;
  protected readonly hauteurTotale = 210;
  protected readonly largeurUtile = this.largeurTotale - this.margeGauche - this.margeDroite;
  protected readonly hauteurUtile = this.hauteurTotale - this.margeHaute - this.margeBasse;

  private static compteur = 0;
  /** Identifiant unique du dégradé, pour ne pas confondre plusieurs aires. */
  protected readonly idDegrade = `gr-degrade-${GraphiqueAire.compteur++}`;

  protected readonly points = computed<PointCalcule[]>(() => {
    const valeurs = this.valeurs();
    const maximum = Math.max(this.maximum(), 1);
    const pas = valeurs.length > 1 ? this.largeurUtile / (valeurs.length - 1) : 0;

    return valeurs.map((valeur, index) => ({
      x: this.margeGauche + index * pas,
      y: this.margeHaute
        + this.hauteurUtile
        - Math.max(0, Math.min(1, valeur / maximum)) * this.hauteurUtile,
      valeur,
    }));
  });

  /** Tracé de la courbe. */
  protected readonly courbe = computed(() =>
    this.points().map(p => `${p.x},${p.y}`).join(' L ')
  );

  /** Tracé de l'aire, fermée sur la ligne de base. */
  protected readonly aire = computed(() => {
    const points = this.points();
    if (!points.length) {
      return '';
    }
    const base = this.margeHaute + this.hauteurUtile;
    const trace = points.map(p => `${p.x},${p.y}`).join(' L ');
    return `M ${points[0].x},${base} L ${trace} L ${points[points.length - 1].x},${base} Z`;
  });

  /** Graduations de l'axe vertical : 0, moitié, maximum. */
  protected readonly axeY = computed<GraduationCalculee[]>(() => {
    const maximum = Math.max(this.maximum(), 1);
    return [0, maximum / 2, maximum].map(valeur => ({
      y: this.margeHaute + this.hauteurUtile - (valeur / maximum) * this.hauteurUtile,
      libelle: this.format() === 'montaire' ? abregerMontant(valeur) : abregerNombre(valeur),
    }));
  });

  /** Étiquettes de l'axe horizontal : une sur deux, la dernière toujours. */
  protected readonly axeX = computed<EtiquetteX[]>(() => {
    const categories = this.categories();
    const points = this.points();
    const dernier = categories.length - 1;
    return categories.map((libelle, index) => ({
      libelle,
      x: points[index]?.x ?? this.margeGauche,
      y: this.hauteurTotale - 9,
      visible: index % 2 === 0 || index === dernier,
    }));
  });

  /** Dernier point mis en avant. */
  protected readonly dernierPoint = computed(() => {
    const points = this.points();
    return points.length ? points[points.length - 1] : null;
  });

  /** Libellé et valeur du dernier point, rappelés sous le graphique. */
  protected readonly dernier = computed(() => {
    const point = this.dernierPoint();
    if (!point) {
      return null;
    }
    const complet = this.libellesComplets();
    return {
      libelle: complet.length ? complet[complet.length - 1] : '',
      valeur: this.format() === 'montaire' ? formaterMontant(point.valeur) : formaterNombre(point.valeur),
    };
  });

  protected readonly aDesDonnees = computed(() => this.valeurs().some(v => v > 0));
}
