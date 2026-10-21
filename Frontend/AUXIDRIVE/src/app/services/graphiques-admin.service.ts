import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Contrat } from '../core/interfaces/contrat';
import { Inscription } from '../core/interfaces/inscription';

/**
 * Sourcing des données brutes des graphiques du tableau de bord administrateur.
 *
 * Deux listes suffisent : le rôle ADMIN est déjà autorisé à consulter
 * `/api/contrats` et `/api/inscriptions` (voir SecurityConfig côté back-end).
 * L'agrégation des six graphiques est ensuite réalisée dans le navigateur,
 * dans `core/charts/graphiques-admin.ts` : aucun point d'agrégation serveur
 * n'est nécessaire.
 */
@Injectable({
  providedIn: 'root',
})
export class GraphiquesAdminService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api';

  /** Tous les contrats souscrits, tous canaux confondus. */
  getContrats(): Observable<Contrat[]> {
    return this.http.get<Contrat[]>(`${this.apiUrl}/contrats`);
  }

  /** Toutes les demandes d'inscription, quel que soit leur statut. */
  getInscriptions(): Observable<Inscription[]> {
    return this.http.get<Inscription[]>(`${this.apiUrl}/inscriptions`);
  }
}
