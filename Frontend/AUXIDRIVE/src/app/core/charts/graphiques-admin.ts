/* ==========================================================================
   Séries du tableau de bord d'administration.
   Agrégation 100 % côté client : le rôle ADMIN accède déjà aux contrats et aux
   inscriptions (SecurityConfig), aucun appel serveur supplémentaire n'est donc
   nécessaire au-delà des deux requêtes de listes.
   ========================================================================== */

import { Assure } from '../interfaces/assure';
import { CompteClientVue } from '../interfaces/compte-client';
import { Contrat } from '../interfaces/contrat';
import { Inscription } from '../interfaces/inscription';
import { Statistiques } from '../interfaces/statistiques';
import {
  AxeTemporel,
  EntreeSerie,
  PartSerie,
  SerieColonnes,
  cleMois,
  compterParMois,
  construireBarres,
  construireParts,
  maximumArrondi,
  moisRecents,
} from './chart-series';

/** Palette des graphiques : les mêmes teintes que le reste de l'application. */
export const COULEURS = {
  lagon: 'var(--lagon)',
  signal: 'var(--signal)',
  ambre: 'var(--ambre)',
  erreur: 'var(--erreur)',
  nuit: 'var(--nuit)',
  bois: 'var(--bois)',
  gris: 'var(--gris)',
};

/** Nombre de mois couverts par le graphique de chiffre d'affaires. */
export const MOIS_CHIFFRE_AFFAIRES = 12;

/** Nombre de mois couverts par le graphique des inscriptions. */
export const MOIS_INSCRIPTIONS = 6;

/** Données brutes nécessaires au calcul des six graphiques. */
export interface DonneesGraphiques {
  statistiques: Statistiques | null;
  comptes: CompteClientVue[];
  assures: Assure[];
  contrats: Contrat[];
  inscriptions: Inscription[];
}

/** Séries affichées par les six graphiques du tableau de bord. */
export interface GraphiquesAdmin {
  donutContrats: PartSerie[];
  /** Axe du graphique de chiffre d'affaires. */
  axeChiffreAffaires: AxeTemporel;
  valeursChiffreAffaires: number[];
  /** Axe du graphique des inscriptions. */
  axeInscriptions: AxeTemporel;
  seriesInscriptions: SerieColonnes[];
  barresComptes: PartSerie[];
  barresCanaux: PartSerie[];
  donutAssures: PartSerie[];
}

/**
 * Calcule les six séries du tableau de bord.
 * Fonction pure : elle ne dépend que des données reçues et de la date de référence.
 */
export function construireGraphiques(
  donnees: DonneesGraphiques,
  reference: Date = new Date()
): GraphiquesAdmin {
  const stats = donnees.statistiques;

  const donutContrats = construireParts([
    { libelle: 'Validés', valeur: stats?.contratsValides ?? 0, couleur: COULEURS.lagon },
    { libelle: 'En attente', valeur: stats?.contratsEnAttente ?? 0, couleur: COULEURS.ambre },
    { libelle: 'Refusés', valeur: stats?.contratsRefuses ?? 0, couleur: COULEURS.erreur },
  ]);

  const [axeChiffreAffaires, valeursChiffreAffaires] = construireChiffreAffaires(donnees.contrats, reference);
  const [axeInscriptions, seriesInscriptions] = construireInscriptions(donnees.inscriptions, reference);

  const barresComptes = construireBarres([
    { libelle: 'Actifs', valeur: compter(donnees.comptes, c => c.statutCompte === 'ACTIF'), couleur: COULEURS.lagon },
    { libelle: 'Suspendus', valeur: compter(donnees.comptes, c => c.statutCompte === 'SUSPENDU'), couleur: COULEURS.ambre },
    { libelle: 'Inactifs', valeur: compter(donnees.comptes, c => c.statutCompte === 'INACTIF'), couleur: COULEURS.erreur },
  ]);

  const barresCanaux = construireCanaux(donnees.contrats);

  const morales = donnees.assures.filter(a => !!a.personneMorale).length;
  const donutAssures = construireParts([
    { libelle: 'Personnes physiques', valeur: donnees.assures.length - morales, couleur: COULEURS.nuit },
    { libelle: 'Personnes morales', valeur: morales, couleur: COULEURS.bois },
  ]);

  return {
    donutContrats,
    axeChiffreAffaires,
    valeursChiffreAffaires,
    axeInscriptions,
    seriesInscriptions,
    barresComptes,
    barresCanaux,
    donutAssures,
  };
}

/**
 * Chiffre d'affaires mensuel : prime TTC des contrats validés, par mois de
 * souscription. Retourne l'axe et les valeurs correspondantes.
 */
function construireChiffreAffaires(contrats: Contrat[], reference: Date): [AxeTemporel, number[]] {
  const mois = moisRecents(MOIS_CHIFFRE_AFFAIRES, reference);
  const sommes = new Map<string, number>();

  for (const contrat of contrats) {
    if (contrat.statutContrat !== 'VALIDE') {
      continue;
    }
    const cle = cleMois(contrat.dateSouscription);
    if (!cle) {
      continue;
    }
    sommes.set(cle, (sommes.get(cle) ?? 0) + (contrat.primeTtcCourante ?? 0));
  }

  const valeurs = mois.map(m => sommes.get(m.cle) ?? 0);
  return [
    {
      categories: mois.map(m => m.libelleCourt),
      libellesComplets: mois.map(m => m.libelleComplet),
      maximum: maximumArrondi(valeurs),
    },
    valeurs,
  ];
}

/** Volume d'inscriptions reçues et validées, par mois. */
function construireInscriptions(inscriptions: Inscription[], reference: Date): [AxeTemporel, SerieColonnes[]] {
  const mois = moisRecents(MOIS_INSCRIPTIONS, reference);
  const recues = compterParMois(inscriptions.map(i => cleMois(i.dateDemande)), mois);
  const validees = compterParMois(
    inscriptions.filter(i => i.statutInscription === 'VALIDEE').map(i => cleMois(i.dateDemande)),
    mois
  );

  return [
    {
      categories: mois.map(m => m.libelleCourt),
      libellesComplets: mois.map(m => m.libelleComplet),
      maximum: maximumArrondi([...recues, ...validees]),
    },
    [
      { libelle: 'Reçues', couleur: COULEURS.nuit, valeurs: recues },
      { libelle: 'Validées', couleur: COULEURS.lagon, valeurs: validees },
    ],
  ];
}

/** Contrats souscrits par canal de souscription. */
function construireCanaux(contrats: Contrat[]): PartSerie[] {
  const compteurs = new Map<string, number>();
  for (const contrat of contrats) {
    const canal = (contrat.canalSouscription || 'NON RENSEIGNÉ').trim();
    compteurs.set(canal, (compteurs.get(canal) ?? 0) + 1);
  }

  const entrees: EntreeSerie[] = [...compteurs.entries()]
    .map(([canal, effectif]) => ({
      libelle: libelleCanal(canal),
      valeur: effectif,
      couleur: COULEURS.lagon,
    }))
    .sort((a, b) => b.valeur - a.valeur);

  return construireBarres(entrees);
}

/** Libellé lisible d'un canal technique. */
function libelleCanal(canal: string): string {
  switch (canal.toUpperCase()) {
    case 'AGENCE': return 'Agence';
    case 'PORTAL_CLIENT':
    case 'PORTAL': return 'Portail client';
    default: return canal.charAt(0) + canal.slice(1).toLowerCase();
  }
}

function compter<T>(liste: T[], predicat: (item: T) => boolean): number {
  return liste.filter(predicat).length;
}
