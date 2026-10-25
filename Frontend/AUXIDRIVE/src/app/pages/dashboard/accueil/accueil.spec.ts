import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ContratService } from '../../../services/contrat-service';
import { FactureService } from '../../../services/facture-service';
import { SinistreService } from '../../../services/sinistre-service';
import { DashboardAccueil } from './accueil';

describe('DashboardAccueil', () => {
  const contrats = { mesContrats: () => of([]) };
  const sinistres = { mesSinistres: () => of([]) };
  const factures = { mesFactures: vi.fn(() => of([] as { statutFacture: string }[])) };

  beforeEach(async () => {
    factures.mesFactures.mockReturnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [DashboardAccueil],
      providers: [
        provideRouter([]),
        { provide: ContratService, useValue: contrats },
        { provide: SinistreService, useValue: sinistres },
        { provide: FactureService, useValue: factures },
      ],
    }).compileComponents();
  });

  it('propose des actions utiles quand le client ne possède aucun dossier', () => {
    const fixture = TestBed.createComponent(DashboardAccueil);
    fixture.detectChanges();
    const texte = fixture.nativeElement.textContent as string;

    expect(texte).toContain('Aucun contrat actif pour le moment');
    expect(texte).toContain('Aucun sinistre en cours');
    expect(texte).toContain('Aucune facture disponible');
    expect(fixture.nativeElement.querySelectorAll('a[routerlink="/dashboard/contrats"]').length).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('a[routerlink="/dashboard/sinistres"]').length).toBe(2);
  });

  it('ne présente pas une facture payée comme à régler', () => {
    factures.mesFactures.mockReturnValue(of([{ statutFacture: 'PAYEE' }]));
    const fixture = TestBed.createComponent(DashboardAccueil);
    fixture.detectChanges();
    const texte = fixture.nativeElement.textContent as string;

    expect(texte).toContain('0 factures émises');
    expect(texte).not.toContain('À régler');
    expect(texte).not.toContain('Aucune facture disponible');
  });
});
