export type StatutSinistre = 'DECLARE' | 'RAPPORT_EN_COURS' | 'INDEMNISE' | 'REFUSE';

export interface Sinistre {
  idSinistre: number;
  dateSinistre: string | null;
  dateDeclaration: string | null;
  lieuSinistre: string | null;
  circonstances: string | null;
  statutSinistre: StatutSinistre;
  numRapportPolice: string | null;
  dateRapportPolice: string | null;
  rapportPolicier: string | null;
  responsabiliteAssure: boolean | null;
  montantDommagesEstime: number | null;
  montantIndemnisation: number | null;
  dateCloture: string | null;
  idContrat?: number | null;
  idVehicule?: number | null;
  numPolice?: string | null;
  nomAssure?: string | null;
  prenomAssure?: string | null;
}

export interface DemandeSinistre {
  idContrat: number;
  idVehicule?: number | null;
  dateSinistre: string;
  lieuSinistre?: string;
  circonstances?: string;
}
