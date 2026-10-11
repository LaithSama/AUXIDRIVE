export interface Role {
  idRole: number;
  libRole: string;
  description?: string;
}

export type StatutUtilisateur = 'ACTIF' | 'SUSPENDU' | 'INACTIF';

/** Compte d'un agent ou d'un administrateur (table Utilisateurs), géré par l'administrateur. */
export interface Utilisateur {
  idUtil: number;
  email: string;
  nomUtil: string;
  /** Envoyé uniquement à la création/modification, jamais renvoyé par l'API. */
  motDePasse?: string;
  role: Role;
  statutUtilisateur: StatutUtilisateur;
  dateCreation?: string;
  derniereConnexion?: string;
}
