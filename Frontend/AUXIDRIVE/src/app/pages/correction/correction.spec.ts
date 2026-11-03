import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Correction } from './correction';

describe('Correction des pièces', () => {
  const url = `${environment.apiUrl}/api/inscriptions/correction`;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Correction],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap({ jeton: 'secret' }) } } }],
    }).compileComponents();
  });

  it('remplace uniquement les pièces demandées et confirme le réexamen', () => {
    const fixture = TestBed.createComponent(Correction);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(req => req.url === url && req.params.get('jeton') === 'secret')
      .flush({ idInscription: 42, documents: [{ type: 'CARTE_GRISE', motif: 'Photo floue' }] });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Photo floue');

    const piece = fixture.componentInstance.pieces[0];
    piece.fichier = new File(['image'], 'grise.png', { type: 'image/png' });
    fixture.componentInstance.envoyer();
    const requete = http.expectOne(req => req.url === url && req.method === 'POST');
    expect((requete.request.body as FormData).get('carteGrise')).toBe(piece.fichier);
    requete.flush(null);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Un agent va réexaminer votre dossier');
    http.verify();
  });
});
