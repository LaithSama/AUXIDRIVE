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

/** Réponse de création : le compte, le mot de passe temporaire à communiquer et le résultat de l'e-mail. */
export interface CompteClientCree {
  compte: CompteClientVue;
  motDePasseTemporaire: string;
  /** false si l'e-mail n'est pas parti : le mot de passe doit alors être transmis manuellement. */
  emailEnvoye: boolean;
}
