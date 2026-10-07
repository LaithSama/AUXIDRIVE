import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CodeTarif, Vehicule } from '../core/interfaces/vehicule';

@Injectable({
  providedIn: 'root',
})
export class VehiculeService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/vehicules';

  mesVehicules(): Observable<Vehicule[]> {
    return this.http.get<Vehicule[]>(`${this.apiUrl}/compte`);
  }

  ajouter(vehicule: Partial<Vehicule>): Observable<Vehicule> {
    return this.http.post<Vehicule>(`${this.apiUrl}/compte`, vehicule);
  }

  codesTarifaires(): Observable<CodeTarif[]> {
    return this.http.get<CodeTarif[]>('http://localhost:8080/api/referentiels/codes-tarifaires');
  }
}
