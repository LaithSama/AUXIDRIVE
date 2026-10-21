/* ==========================================================================
   Séries de graphiques — types et fonctions pures, sans dépendance Angular.
   Le rendu est assuré par les composants SVG de src/app/components/graphiques ;
   ce fichier ne contient que les calculs, ce qui les rend testables à l'unité.
   ========================================================================== */

/** Une part prête à l'affichage : arc de donut ou barre. */
export interface PartSerie {
  libelle: string;
  valeur: number;
  /** Couleur CSS : variable du thème ou code hexadécimal. */
  couleur: string;
  /** Part relative, de 0 à 1. */
  part: number;
  /** Pourcentage arrondi ; la somme des pourcentages d'une série vaut 100. */
  pourcentage: number;
}

/** Point d'une série temporelle (courbe ou aire). */
export interface PointSerie {
  libelle: string;
  valeur: number;
}

/** Série de colonnes groupées : une série par couleur. */
export interface SerieColonnes {
  libelle: string;
  couleur: string;
  valeurs: number[];
}

/** Axe temporel partagé par l'aire et les colonnes groupées. */
export interface AxeTemporel {
  /** Libellés d'axe, abrégés (« janv. »). */
  categories: string[];
  /** Libellés longs (« janv. 26 »), pour les infobulles. */
  libellesComplets: string[];
  /** Borné haute de l'axe, arrondi à une valeur lisible. */
  maximum: number;
}

/** Entrée brute d'une part : libellé, valeur et couleur. */
export interface EntreeSerie {
  libelle: string;
  valeur: number;
  couleur: string;
}

/** Un mois d'une série temporelle. */
export interface MoisSerie {
  /** Clé technique au format AAAA-MM. */
  cle: string;
  /** Mois abrégé pour les axes (« janv. »). */
  libelleCourt: string;
  /** Mois et année à deux chiffres (« janv. 26 »). */
  libelleComplet: string;
  annee: number;
  /** Mois de 1 à 12. */
  mois: number;
}

const MOIS_COURTS = [
  'janv.', 'févr.', 'mars', 'avr.', 'mai', 'juin',
  'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.',
];

/**
 * Pourcentages entiers dont la somme vaut exactement 100.
 * Méthode du plus grand reste : on arrondit chaque part vers le bas, puis on
 * distribue les points restants sur les parts les plus fractionnaires.
 */
export function pourcentages(valeurs: number[]): number[] {
  const normalises = valeurs.map(v => Math.max(0, v || 0));
  const total = normalises.reduce((s, v) => s + v, 0);
  if (total <= 0) {
    return normalises.map(() => 0);
  }

  const exacts = normalises.map(v => (v / total) * 100);
  const arrondis = exacts.map(v => Math.floor(v));
  let reste = 100 - arrondis.reduce((s, v) => s + v, 0);

  const parFraction = exacts
    .map((valeur, index) => ({ index, fraction: valeur - arrondis[index] }))
    .sort((a, b) => b.fraction - a.fraction);

  for (let i = 0; reste > 0 && parFraction.length > 0; i += 1) {
    arrondis[parFraction[i % parFraction.length].index] += 1;
    reste -= 1;
  }
  return arrondis;
}

/** Parts d'un donut : chaque part est rapportée au total de la série. */
export function construireParts(entrees: EntreeSerie[]): PartSerie[] {
  const valeurs = entrees.map(e => Math.max(0, e.valeur || 0));
  const total = valeurs.reduce((s, v) => s + v, 0);
  const pcts = pourcentages(valeurs);

  return entrees.map((entree, i) => ({
    libelle: entree.libelle,
    valeur: valeurs[i],
    couleur: entree.couleur,
    part: total > 0 ? valeurs[i] / total : 0,
    pourcentage: pcts[i],
  }));
}

/** Barres : chaque part est rapportée à la plus grande valeur de la série. */
export function construireBarres(entrees: EntreeSerie[]): PartSerie[] {
  const valeurs = entrees.map(e => Math.max(0, e.valeur || 0));
  const maximum = valeurs.reduce((m, v) => (v > m ? v : m), 0);
  const pcts = pourcentages(valeurs);

  return entrees.map((entree, i) => ({
    libelle: entree.libelle,
    valeur: valeurs[i],
    couleur: entree.couleur,
    part: maximum > 0 ? valeurs[i] / maximum : 0,
    pourcentage: pcts[i],
  }));
}

/** Les `nb` derniers mois, du plus ancien au plus récent. */
export function moisRecents(nb: number, reference: Date = new Date()): MoisSerie[] {
  const liste: MoisSerie[] = [];
  for (let i = nb - 1; i >= 0; i -= 1) {
    const date = new Date(reference.getFullYear(), reference.getMonth() - i, 1);
    const annee = date.getFullYear();
    const mois = date.getMonth() + 1;
    const suffixe = String(annee).slice(2);
    liste.push({
      cle: `${annee}-${String(mois).padStart(2, '0')}`,
      libelleCourt: MOIS_COURTS[mois - 1],
      libelleComplet: `${MOIS_COURTS[mois - 1]} ${suffixe}`,
      annee,
      mois,
    });
  }
  return liste;
}

/** Extrait la clé AAAA-MM d'une date ISO ; null si la date est absente ou illisible. */
export function cleMois(valeur: string | null | undefined): string | null {
  if (!valeur) {
    return null;
  }
  const correspondance = /^(\d{4})-(\d{2})/.exec(valeur.trim());
  if (!correspondance) {
    return null;
  }
  const mois = Number(correspondance[2]);
  if (mois < 1 || mois > 12) {
    return null;
  }
  return `${correspondance[1]}-${correspondance[2]}`;
}

/** Compte les occurrences de chaque mois de l'axe. */
export function compterParMois(
  cles: (string | null | undefined)[],
  mois: MoisSerie[]
): number[] {
  const index = new Map(mois.map((m, i) => [m.cle, i]));
  const compte = new Array<number>(mois.length).fill(0);
  for (const cle of cles) {
    if (!cle) {
      continue;
    }
    const position = index.get(cle);
    if (position !== undefined) {
      compte[position] += 1;
    }
  }
  return compte;
}

/** Borné haute lisible : 1, 2, 2,5, 5 ou 10 fois une puissance de dix. */
export function maximumArrondi(valeurs: number[]): number {
  const maximum = valeurs.reduce(
    (m, v) => (Number.isFinite(v) && v > m ? v : m),
    0
  );
  if (maximum <= 0) {
    return 1;
  }
  const magnitude = 10 ** Math.floor(Math.log10(maximum));
  const normalise = maximum / magnitude;
  const pas =
    normalise <= 1 ? 1
      : normalise <= 2 ? 2
        : normalise <= 2.5 ? 2.5
          : normalise <= 5 ? 5
            : 10;
  return pas * magnitude;
}

/** Graduations d'un axe, de 0 au maximum compris. */
export function graduations(maximum: number, nb: number = 4): number[] {
  if (nb < 1) {
    return [0];
  }
  const pas = maximum / nb;
  return Array.from({ length: nb + 1 }, (_, i) => i * pas);
}

/** Nombre lisible : 1 234. */
export function formaterNombre(valeur: number): string {
  return Math.round(valeur || 0).toLocaleString('fr-FR');
}

/** Montant lisible : 1 234 Ar. */
export function formaterMontant(valeur: number): string {
  return `${Math.round(valeur || 0).toLocaleString('fr-FR')} Ar`;
}

/** Nombre abrégé pour les axes : 1,2 k ou 1,2 M. */
export function abregerNombre(valeur: number): string {
  const nombre = valeur || 0;
  const absolu = Math.abs(nombre);
  if (absolu >= 1_000_000) {
    return `${(nombre / 1_000_000).toLocaleString('fr-FR', { maximumFractionDigits: 1 })} M`;
  }
  if (absolu >= 1_000) {
    return `${(nombre / 1_000).toLocaleString('fr-FR', { maximumFractionDigits: 1 })} k`;
  }
  return Math.round(nombre).toLocaleString('fr-FR');
}

/** Montant abrégé pour les axes : 1,2 k ou 1,2 M (sans l'unité, précisée en sous-titre). */
export function abregerMontant(valeur: number): string {
  return abregerNombre(valeur);
}
