export type StatutFacture = 'EMISE' | 'PAYEE' | 'ANNULEE';
export type TypeFacture = 'SOUSCRIPTION' | 'RENOUVELLEMENT' | 'MAJORATION' | 'RESILIATION';

export interface Facture {
  idFacture: number;
  numPolice: string | null;
  typeFacture: TypeFacture;
  primeNette: number | null;
  montantTaxes: number | null;
  montantTtc: number | null;
  accessoires: number | null;
  montantTe: number | null;
  montantTacava: number | null;
  montantTva: number | null;
  dateEmission: string | null;
  statutFacture: StatutFacture;
}
