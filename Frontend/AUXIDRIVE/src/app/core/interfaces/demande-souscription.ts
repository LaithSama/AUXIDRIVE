export interface DemandeSouscription {
  idOffre: number;
  idVehicules: number[];
  /** Codes des garanties choisies ; vide ou absent = contrat sans garantie. */
  cdGaranties?: string[];
  primeNette?: number;
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

export interface ResultatSouscription {
  idContrat: number;
  statut: string;
  primeTtc: number;
}
