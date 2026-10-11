import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MotDePasseDemande } from '../core/interfaces/profil';
import { ProfilStaff, ProfilStaffAMaj } from '../core/interfaces/profil-staff';

@Injectable({
  providedIn: 'root',
})
export class ProfilStaffService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/profil-staff';

  monProfil(): Observable<ProfilStaff> {
    return this.http.get<ProfilStaff>(this.apiUrl);
  }

  enregistrer(donnees: ProfilStaffAMaj): Observable<ProfilStaff> {
    return this.http.put<ProfilStaff>(this.apiUrl, donnees);
  }

  changerMotDePasse(demande: MotDePasseDemande): Observable<{ modifie: boolean }> {
    return this.http.put<{ modifie: boolean }>(`${this.apiUrl}/mot-de-passe`, demande);
  }
}
