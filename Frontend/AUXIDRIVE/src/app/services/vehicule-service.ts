import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CodeTarif, Vehicule } from '../core/interfaces/vehicule';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class VehiculeService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/vehicules`;

  mesVehicules(): Observable<Vehicule[]> {
    return this.http.get<Vehicule[]>(`${this.apiUrl}/compte`);
  }

  ajouter(vehicule: Partial<Vehicule>): Observable<Vehicule> {
    return this.http.post<Vehicule>(`${this.apiUrl}/compte`, vehicule);
  }

  codesTarifaires(): Observable<CodeTarif[]> {
    return this.http.get<CodeTarif[]>(`${environment.apiUrl}/api/referentiels/codes-tarifaires`);
  }
}
