import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Contrat } from '../core/interfaces/contrat';
import {
  DemandeSouscription,
  DetailSimulation,
  ResultatSouscription,
} from '../core/interfaces/demande-souscription';

@Injectable({
  providedIn: 'root',
})
export class ContratService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/contrats';

  mesContrats(): Observable<Contrat[]> {
    return this.http.get<Contrat[]>(`${this.apiUrl}/compte`);
  }

  simuler(demande: DemandeSouscription): Observable<DetailSimulation> {
    return this.http.post<DetailSimulation>(`${this.apiUrl}/simulation`, demande);
  }

  souscrire(demande: DemandeSouscription): Observable<ResultatSouscription> {
    return this.http.post<ResultatSouscription>(this.apiUrl, demande);
  }
}
