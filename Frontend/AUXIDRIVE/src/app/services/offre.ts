import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GarantieType } from '../core/interfaces/garantie';
import { Offre } from '../core/interfaces/offre';

@Injectable({
  providedIn: 'root',
})
export class OffreService {
  private apiUrl = 'http://localhost:8080/api/offres';

  private http = inject(HttpClient);

  getOffres(): Observable<Offre[]> {
    return this.http.get<Offre[]>(this.apiUrl);
  }

  /** Garanties que le client peut choisir pour une offre (celles de sa branche). */
  getGaranties(idOffre: number): Observable<GarantieType[]> {
    return this.http.get<GarantieType[]>(`${this.apiUrl}/${idOffre}/garanties`);
  }
}
