import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Assure } from '../core/interfaces/assure';
import { AssureProfession } from '../core/interfaces/assure-profession';
import { AssureIdentifiantsLegaux } from '../core/interfaces/assure-identifiants-legaux';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class InscriptionService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/inscriptions';

  soumettre(
    assure: Assure,
    profession?: AssureProfession,
    identifiants?: AssureIdentifiantsLegaux,
    fichiers: { [key: string]: File | null } = {}
  ): Observable<any> {
    const formData = new FormData();

    // 1. Injection des parties JSON sous forme de Blobs typés application/json
    formData.append('assure', new Blob([JSON.stringify(assure)], { type: 'application/json' }));
    
    if (profession) {
      formData.append('profession', new Blob([JSON.stringify(profession)], { type: 'application/json' }));
    }
    
    if (identifiants) {
      formData.append('identifiants', new Blob([JSON.stringify(identifiants)], { type: 'application/json' }));
    }

    // 2. Injection des fichiers physiques s'ils ont été sélectionnés
    Object.keys(fichiers).forEach(key => {
      const fichier = fichiers[key];
      if (fichier) {
        formData.append(key, fichier);
      }
    });

    // 3. Envoi de la requête POST au backend
    return this.http.post<any>(this.apiUrl, formData);
  }
}
