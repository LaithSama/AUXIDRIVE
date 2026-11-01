import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { Facture } from '../../../core/interfaces/facture';
import { environment } from '../../../../environments/environment';
import { DashboardFactures } from './factures';

const FACTURE: Facture = {
  idFacture: 12,
  numPolice: 'POL-001',
  numFacture: 'FAC-001',
  typeFacture: 'SOUSCRIPTION',
  primeNette: 1000,
  montantTaxes: 100,
  montantTtc: 1100,
  accessoires: 0,
  montantTe: 0,
  montantTacava: 0,
  montantTva: 100,
  dateEmission: '2026-10-01',
  statutFacture: 'EMISE',
};

describe('DashboardFactures', () => {
  let fixture: ComponentFixture<DashboardFactures>;
  let httpMock: HttpTestingController;
  const createObjectURL = vi.fn(() => 'blob:http://localhost/facture');
  const revokeObjectURL = vi.fn();

  beforeEach(async () => {
    vi.clearAllMocks();
    vi.stubGlobal('URL', class extends URL {
      static override createObjectURL = createObjectURL;
      static override revokeObjectURL = revokeObjectURL;
    });
    await TestBed.configureTestingModule({
      imports: [DashboardFactures],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardFactures);
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/api/factures/compte`).flush([FACTURE]);
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    httpMock.verify();
    vi.unstubAllGlobals();
  });

  it('affiche le PDF dans la page et conserve le bouton de téléchargement', () => {
    const boutons = fixture.nativeElement.querySelectorAll('.fac-actions button');
    expect(boutons).toHaveLength(2);
    expect(boutons[1].textContent).toContain('Télécharger');

    boutons[0].click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="status"]').textContent).toContain('Chargement');

    const requete = httpMock.expectOne(`${environment.apiUrl}/api/factures/12/pdf`);
    expect(requete.request.responseType).toBe('blob');
    requete.flush(new Blob(['%PDF-1.4'], { type: 'application/pdf' }));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.fac-apercu-pdf').getAttribute('src')).toBe('blob:http://localhost/facture');
    fixture.nativeElement.querySelector('[aria-label="Fermer l\'aperçu"]').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.fac-apercu')).toBeNull();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:http://localhost/facture');
  });

  it('signale un PDF indisponible dans l’aperçu', () => {
    fixture.componentInstance.voirPdf(FACTURE);
    httpMock.expectOne(`${environment.apiUrl}/api/factures/12/pdf`)
      .flush(new Blob(['Introuvable']), { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.fac-apercu [role="alert"]').textContent).toContain('PDF indisponible');
    expect(fixture.nativeElement.querySelector('.fac-apercu-pdf')).toBeNull();
  });

  it('annule la requête si l’aperçu est fermé avant son chargement', () => {
    fixture.componentInstance.voirPdf(FACTURE);
    const requete = httpMock.expectOne(`${environment.apiUrl}/api/factures/12/pdf`);
    fixture.componentInstance.fermerApercu();

    expect(requete.cancelled).toBe(true);
    expect(fixture.componentInstance.factureApercu).toBeNull();
    expect(createObjectURL).not.toHaveBeenCalled();
  });
});
