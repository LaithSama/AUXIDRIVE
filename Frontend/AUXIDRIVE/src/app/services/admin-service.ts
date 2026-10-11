import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Assure } from '../core/interfaces/assure';
import { CompteClientCree, CompteClientVue } from '../core/interfaces/compte-client';
import { Role } from '../core/interfaces/utilisateur';
import { Statistiques } from '../core/interfaces/statistiques';

/** Espace d'administration (routes réservées au rôle ADMIN). */
@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/admin';

  // ---------- tableau de bord ----------

  getStatistiques(): Observable<Statistiques> {
    return this.http.get<Statistiques>(`${this.apiUrl}/statistiques`);
  }

  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/roles`);
  }

  // ---------- assurés ----------

  getAssures(): Observable<Assure[]> {
    return this.http.get<Assure[]>(`${this.apiUrl}/assures`);
  }

  creerAssure(assure: Partial<Assure>): Observable<Assure> {
    return this.http.post<Assure>(`${this.apiUrl}/assures`, assure);
  }

  modifierAssure(id: number, assure: Partial<Assure>): Observable<Assure> {
    return this.http.put<Assure>(`${this.apiUrl}/assures/${id}`, assure);
  }

  supprimerAssure(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/assures/${id}`);
  }

  // ---------- comptes clients ----------

  getComptesClients(): Observable<CompteClientVue[]> {
    return this.http.get<CompteClientVue[]>(`${this.apiUrl}/comptes-clients`);
  }

  creerCompteClient(idAssure: number, email: string): Observable<CompteClientCree> {
    return this.http.post<CompteClientCree>(`${this.apiUrl}/comptes-clients`, { idAssure, email });
  }

  modifierCompteClient(
    id: number,
    compte: { email: string; statutCompte: string }
  ): Observable<CompteClientVue> {
    return this.http.put<CompteClientVue>(`${this.apiUrl}/comptes-clients/${id}`, compte);
  }

  supprimerCompteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/comptes-clients/${id}`);
  }
}
