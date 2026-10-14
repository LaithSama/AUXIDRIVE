export interface DemandeSouscription {
  idOffre: number;
  idVehicules: number[];
  /** Codes des garanties choisies ; vide ou absent = contrat sans garantie. */
  cdGaranties?: string[];
  primeNette?: number;
  /** Accessoires saisis ; absent = ceux de la branche. */
  accessoires?: number;
  codeTaxe?: string;
  tauxCommission?: number;
  coefficientBonusMalus?: number;
  dureeMois?: number;
  codeAgence?: string;
}

export interface DetailSimulation {
  base: number;
  accessoires: number;
  te: number;
  tacava: number;
  tva: number;
  ttc: number;
  commission: number;
  tauxCommission: number;
  appliqueTe: boolean;
  appliqueTva: boolean;
  appliqueTacava: boolean;
}

/** Paramètres de préparation du calcul de prime d'un contrat, côté agent. */
export interface ParametresPrime {
  codeTaxe: string | null;
  libelleCodeTaxe: string | null;
  primeNetteCourante: number | null;
  accessoiresDefaut: number | null;
  tauxCommission: number | null;
  tauxTe: number | null;
  tauxTva: number | null;
  tauxTacava: number | null;
  appliqueTe: boolean;
  appliqueTva: boolean;
  appliqueTacava: boolean;
}

export interface ResultatSouscription {
  idContrat: number;
  statut: string;
  primeTtc: number;
}
