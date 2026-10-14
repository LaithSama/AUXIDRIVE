import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Facture } from '../core/interfaces/facture';

@Injectable({
  providedIn: 'root',
})
export class FactureService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/factures';

  mesFactures(): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/compte`);
  }

  /** Factures d'un contrat : accessible au client propriétaire du contrat. */
  facturesDuContrat(idContrat: number): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/contrat/${idContrat}`);
  }

  telechargerPdf(idFacture: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${idFacture}/pdf`, {
      responseType: 'blob',
    });
  }
}
