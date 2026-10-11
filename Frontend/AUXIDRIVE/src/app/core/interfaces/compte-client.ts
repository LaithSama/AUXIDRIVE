export type StatutCompte = 'ACTIF' | 'SUSPENDU' | 'INACTIF';

/** Vue enrichie d'un compte client : l'identité de l'assuré est jointe à la ligne. */
export interface CompteClientVue {
  idCompteClient: number;
  email: string;
  statutCompte: StatutCompte;
  dateCreation?: string;
  dateDerniereConnexion?: string;
  idAssure?: number | null;
  numerosAssure?: string | null;
  nomAssure?: string | null;
  prenomAssure?: string | null;
}

/** Réponse de création : le compte plus le mot de passe temporaire à communiquer. */
export interface CompteClientCree {
  compte: CompteClientVue;
  motDePasseTemporaire: string;
}
