import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MotDePasseDemande, Profil, ReponseSuppression } from '../core/interfaces/profil';

export interface ProfilAMaj {
  nomAssure: string;
  prenomAssure: string;
  adresse: string;
  contact: string;
  mail: string;
  emailConnexion: string;
}

@Injectable({
  providedIn: 'root',
})
export class ProfilService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/profil';

  monProfil(): Observable<Profil> {
    return this.http.get<Profil>(this.apiUrl);
  }

  enregistrer(donnees: ProfilAMaj): Observable<Profil> {
    return this.http.put<Profil>(this.apiUrl, donnees);
  }

  changerMotDePasse(demande: MotDePasseDemande): Observable<{ modifie: boolean }> {
    return this.http.put<{ modifie: boolean }>(`${this.apiUrl}/mot-de-passe`, demande);
  }

  /** Planifie la suppression définitive du compte à +72 h. */
  planifierSuppression(): Observable<ReponseSuppression> {
    return this.http.post<ReponseSuppression>(`${this.apiUrl}/suppression`, {});
  }

  /** Annule la suppression programmée tant que le délai n'est pas écoulé. */
  annulerSuppression(): Observable<ReponseSuppression> {
    return this.http.post<ReponseSuppression>(`${this.apiUrl}/suppression/annuler`, {});
  }
}
