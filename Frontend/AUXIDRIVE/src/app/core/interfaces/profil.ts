export interface ProfilCompte {
  email: string;
  dateCreation: string | null;
  dateSuppressionPlanifiee: string | null;
}

export interface ProfilAssure {
  numerosAssure: string | null;
  civilite: string | null;
  dateNaissance: string | null;
  nomAssure: string;
  prenomAssure: string;
  adresse: string | null;
  contact: string | null;
  mail: string | null;
}

export interface Profil {
  compte: ProfilCompte;
  assure: ProfilAssure;
}

export interface MotDePasseDemande {
  motDePasseActuel: string;
  nouveauMotDePasse: string;
}

export interface ReponseSuppression {
  dateSuppressionPlanifiee: string | null;
}
