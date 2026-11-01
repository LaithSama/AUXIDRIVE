import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GarantieType } from '../core/interfaces/garantie';
import { Offre } from '../core/interfaces/offre';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class OffreService {
  private apiUrl = `${environment.apiUrl}/api/offres`;

  private http = inject(HttpClient);

  getOffres(): Observable<Offre[]> {
    return this.http.get<Offre[]>(this.apiUrl);
  }

  /** Garanties que le client peut choisir pour une offre (celles de sa branche). */
  getGaranties(idOffre: number): Observable<GarantieType[]> {
    return this.http.get<GarantieType[]>(`${this.apiUrl}/${idOffre}/garanties`);
  }
}
