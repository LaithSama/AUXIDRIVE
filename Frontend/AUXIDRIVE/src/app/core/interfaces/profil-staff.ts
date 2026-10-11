export interface ProfilStaff {
  email: string;
  nomUtil: string;
  role: string | null;
  statutUtilisateur: string | null;
  dateCreation: string | null;
  derniereConnexion: string | null;
}

export interface ProfilStaffAMaj {
  nomUtil: string;
  emailConnexion: string;
}
