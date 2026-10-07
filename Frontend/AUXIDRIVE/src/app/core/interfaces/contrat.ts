export type StatutContrat = 'EN_ATTENTE' | 'VALIDE' | 'REFUSE';

export interface Contrat {
  idContrat: number;
  numPolice: string | null;
  codeAgence: string | null;
  dateSouscription: string | null;
  dateEffet: string | null;
  dateExpiration: string | null;
  statutContrat: StatutContrat;
  coefficientBonusMalus: number | null;
  primeNetteCourante: number | null;
  primeTtcCourante: number | null;
  tauxCommission: number | null;
  montantCommission: number | null;
  canalSouscription: string | null;
  idAssure?: number | null;
  nomAssure?: string | null;
  prenomAssure?: string | null;
}
