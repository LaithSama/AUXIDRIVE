export interface CodeTarif {
  idCodeAuto: number;
  tarifCode?: string | null;
  tarifLibelle?: string | null;
  avecTacava?: boolean | null;
}

export interface Vehicule {
  idVehicule: number;
  numImmatriculation: string;
  numChassis: string;
  numMoteur?: string | null;
  marque: string;
  modele: string;
  typeVehicule?: string | null;
  usageVehicule?: string | null;
  puissanceFiscale: number;
  energie: string;
  nombrePlaces: number;
  dateAjout?: string | null;
  codeTarif?: CodeTarif | null;
}
