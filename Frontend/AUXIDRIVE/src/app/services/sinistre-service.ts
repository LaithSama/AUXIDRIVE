import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DemandeSinistre, Sinistre } from '../core/interfaces/sinistre';

@Injectable({
  providedIn: 'root',
})
export class SinistreService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/sinistres';

  mesSinistres(): Observable<Sinistre[]> {
    return this.http.get<Sinistre[]>(`${this.apiUrl}/compte`);
  }

  declarer(demande: DemandeSinistre): Observable<Sinistre> {
    return this.http.post<Sinistre>(`${this.apiUrl}/declarer`, demande);
  }
}
