export type TypeDocumentProfil = 'CIN' | 'CARTE_GRISE' | 'CARTE_ROSE' | 'MODELE_N1';
export type StatutDocument = 'EN_ATTENTE' | 'VALIDE' | 'REJETE';

export interface DocumentProfil {
  idDocument: number;
  typeDocument: TypeDocumentProfil;
  formatFichier: string;
  tailleKo: number | null;
  dateTeleversement: string | null;
  statutDocuments: StatutDocument;
  motifRejet: string | null;
  dateValidation: string | null;
  /** Vrai quand la pièce appartient à une inscription en cours : le parcours de correction prime. */
  verrouille: boolean;
}

export interface TypeDocument {
  valeur: TypeDocumentProfil;
  libelle: string;
  description: string;
  icone: string;
}
