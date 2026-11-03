import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';

interface PieceCorrection { type: string; motif: string; fichier?: File }

@Component({
  selector: 'app-correction',
  imports: [RouterLink],
  templateUrl: './correction.html',
  styles: [`
    .correction-page { max-width: 680px; margin: 3rem auto; padding: 1.5rem; }
    .correction-piece { margin: 1rem 0; padding: 1.2rem; border: 1px solid #d5dcd9; border-radius: 12px; }
    .correction-piece label { display: block; font-weight: 700; margin-bottom: .6rem; }
    .correction-piece input { display: block; max-width: 100%; }
  `],
})
export class Correction implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private url = `${environment.apiUrl}/api/inscriptions/correction`;
  private jeton = this.route.snapshot.queryParamMap.get('jeton') ?? '';
  dossier: number | null = null;
  pieces: PieceCorrection[] = [];
  chargement = true;
  enCours = false;
  termine = false;
  erreur = '';

  ngOnInit(): void {
    if (!this.jeton) { this.erreur = 'Lien de correction manquant.'; this.chargement = false; return; }
    this.http.get<{ idInscription: number; documents: PieceCorrection[] }>(this.url, {
      params: { jeton: this.jeton },
    }).subscribe({
      next: reponse => { this.dossier = reponse.idInscription; this.pieces = reponse.documents; this.chargement = false; },
      error: () => { this.erreur = 'Ce lien de correction est invalide ou expiré. Contactez le service client.'; this.chargement = false; },
    });
  }

  choisir(event: Event, piece: PieceCorrection): void {
    piece.fichier = (event.target as HTMLInputElement).files?.[0];
    this.erreur = '';
  }

  envoyer(): void {
    if (this.enCours || this.termine) return;
    if (this.pieces.some(p => !p.fichier)) { this.erreur = 'Ajoutez toutes les pièces demandées.'; return; }
    if (this.pieces.some(p => !p.fichier || p.fichier.size > 5 * 1024 * 1024
        || !/\.(pdf|jpe?g|png)$/i.test(p.fichier.name))) {
      this.erreur = 'Chaque fichier doit être un PDF, JPEG ou PNG de 5 Mo maximum.';
      return;
    }
    const noms: Record<string, string> = { CIN: 'cin', CARTE_GRISE: 'carteGrise', CARTE_ROSE: 'carteRose', MODELE_N1: 'modeleN1' };
    const donnees = new FormData();
    this.pieces.forEach(p => donnees.append(noms[p.type], p.fichier!));
    this.enCours = true;
    this.erreur = '';
    this.http.post(this.url, donnees, { params: { jeton: this.jeton } }).subscribe({
      next: () => { this.enCours = false; this.termine = true; this.jeton = ''; },
      error: () => { this.enCours = false; this.erreur = 'Impossible de transmettre les pièces. Vérifiez vos fichiers ou réessayez.'; },
    });
  }
}
