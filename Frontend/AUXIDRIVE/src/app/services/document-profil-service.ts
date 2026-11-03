import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DocumentProfil, TypeDocumentProfil } from '../core/interfaces/document-profil';

@Injectable({
  providedIn: 'root',
})
export class DocumentProfilService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/profil/documents`;

  lister(): Observable<DocumentProfil[]> {
    return this.http.get<DocumentProfil[]>(this.apiUrl);
  }

  /** Ajout d'une pièce manquante : le fichier part en multipart avec son type. */
  ajouter(type: TypeDocumentProfil, fichier: File): Observable<DocumentProfil> {
    const donnees = new FormData();
    donnees.append('type', type);
    donnees.append('fichier', fichier);
    return this.http.post<DocumentProfil>(this.apiUrl, donnees);
  }

  /** Remplacement : la pièce repasse en attente de vérification. */
  remplacer(idDocument: number, fichier: File): Observable<DocumentProfil> {
    const donnees = new FormData();
    donnees.append('fichier', fichier);
    return this.http.put<DocumentProfil>(`${this.apiUrl}/${idDocument}`, donnees);
  }

  supprimer(idDocument: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${idDocument}`);
  }

  /** Aperçu : le backend ne sert le fichier qu'au propriétaire de la pièce. */
  fichier(idDocument: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${idDocument}/fichier`, { responseType: 'blob' });
  }
}
