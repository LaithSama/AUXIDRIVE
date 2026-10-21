import {
  abregerNombre,
  compterParMois,
  construireBarres,
  construireParts,
  formaterMontant,
  formaterNombre,
  maximumArrondi,
  moisRecents,
  pourcentages,
} from './chart-series';

describe('chart-series', () => {
  it('calcule des pourcentages dont la somme vaut 100', () => {
    const pcts = pourcentages([10, 10, 10]);
    expect(pcts.reduce((s, v) => s + v, 0)).toBe(100);
    expect(pcts).toEqual([34, 33, 33]);
  });

  it('répartit le reste sur les plus grandes fractions', () => {
    const pcts = pourcentages([1, 1, 1]);
    expect(pcts.reduce((s, v) => s + v, 0)).toBe(100);
  });

  it('retourne 0 si le total est nul', () => {
    expect(pourcentages([0, 0])).toEqual([0, 0]);
  });

  it('construit des parts de donut relatives au total', () => {
    const parts = construireParts([
      { libelle: 'A', valeur: 3, couleur: 'var(--a)' },
      { libelle: 'B', valeur: 1, couleur: 'var(--b)' },
    ]);
    expect(parts[0].part).toBeCloseTo(0.75);
    expect(parts[1].part).toBeCloseTo(0.25);
    expect(parts.reduce((s, p) => s + p.pourcentage, 0)).toBe(100);
  });

  it('construit des barres relatives au maximum', () => {
    const barres = construireBarres([
      { libelle: 'A', valeur: 10, couleur: 'var(--a)' },
      { libelle: 'B', valeur: 5, couleur: 'var(--b)' },
    ]);
    expect(barres[0].part).toBe(1);
    expect(barres[1].part).toBe(0.5);
  });

  it('génère les n derniers mois dans l\'ordre chronologique', () => {
    const mois = moisRecents(3, new Date('2026-10-15T00:00:00Z'));
    expect(mois.length).toBe(3);
    expect(mois[0].cle).toBe('2026-08');
    expect(mois[2].cle).toBe('2026-10');
    expect(mois[0].libelleCourt).toBe('août');
  });

  it('compte les occurrences par mois', () => {
    const mois = moisRecents(3, new Date('2026-10-15T00:00:00Z'));
    const cles = ['2026-10', '2026-09', null, '2026-10'];
    expect(compterParMois(cles, mois)).toEqual([0, 2, 2]);
  });

  it('arrondit le maximum à une valeur lisible', () => {
    expect(maximumArrondi([1234])).toBe(2000);
    expect(maximumArrondi([0])).toBe(1);
    expect(maximumArrondi([750])).toBe(1000);
  });

  it('formate les nombres et montants en français', () => {
    expect(formaterNombre(1234)).toBe('1\u00a0234');
    expect(formaterMontant(1234)).toBe('1\u00a0234 Ar');
    expect(abregerNombre(1500)).toBe('1,5 k');
    expect(abregerNombre(1_250_000)).toBe('1,25 M');
  });
});
