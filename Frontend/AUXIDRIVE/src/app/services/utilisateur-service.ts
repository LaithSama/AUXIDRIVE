import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Utilisateur } from '../core/interfaces/utilisateur';

/** Gestion des agents et administrateurs (routes réservées au rôle ADMIN). */
@Injectable({
  providedIn: 'root',
})
export class UtilisateurService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/utilisateurs';

  getUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(this.apiUrl);
  }

  creer(utilisateur: Partial<Utilisateur>): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(this.apiUrl, utilisateur);
  }

  modifier(id: number, utilisateur: Partial<Utilisateur>): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.apiUrl}/${id}`, utilisateur);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
