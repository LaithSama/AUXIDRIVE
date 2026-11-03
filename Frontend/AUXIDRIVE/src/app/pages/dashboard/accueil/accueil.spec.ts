import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ContratService } from '../../../services/contrat-service';
import { FactureService } from '../../../services/facture-service';
import { SinistreService } from '../../../services/sinistre-service';
import { DashboardAccueil } from './accueil';

describe('DashboardAccueil', () => {
  const contrats = { mesContrats: vi.fn(() => of([] as { statutContrat: string }[])) };
  const sinistres = { mesSinistres: vi.fn(() => of([] as { statutSinistre: string }[])) };
  const factures = { mesFactures: vi.fn(() => of([] as { statutFacture: string }[])) };

  beforeEach(async () => {
    contrats.mesContrats.mockReturnValue(of([]));
    sinistres.mesSinistres.mockReturnValue(of([]));
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
    expect(fixture.nativeElement.querySelectorAll('a[href="/dashboard/contrats"]').length).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('a[routerlink="/dashboard/sinistres"]').length).toBe(1);
    expect(fixture.nativeElement.querySelector('.accueil-prochaine-etape').textContent).toContain('Découvrez les contrats disponibles');
  });

  it('priorise un contrat en attente avant les sinistres et les factures', () => {
    contrats.mesContrats.mockReturnValue(of([{ statutContrat: 'EN_ATTENTE' }]));
    sinistres.mesSinistres.mockReturnValue(of([{ statutSinistre: 'DECLARE' }]));
    factures.mesFactures.mockReturnValue(of([{ statutFacture: 'EMISE' }]));
    const fixture = TestBed.createComponent(DashboardAccueil);
    fixture.detectChanges();
    const etape = fixture.nativeElement.querySelector('.accueil-prochaine-etape') as HTMLElement;
    expect(etape.textContent).toContain('Votre contrat attend une validation');
    expect(etape.querySelector('a')?.getAttribute('href')).toBe('/dashboard/contrats');
  });

  it('propose de suivre un sinistre en cours si aucun contrat n’attend de validation', () => {
    sinistres.mesSinistres.mockReturnValue(of([{ statutSinistre: 'RAPPORT_EN_COURS' }]));
    factures.mesFactures.mockReturnValue(of([{ statutFacture: 'EMISE' }]));
    const fixture = TestBed.createComponent(DashboardAccueil);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.accueil-prochaine-etape').textContent).toContain('Un sinistre est en cours');
  });

  it('signale une facture émise sans la présenter comme impayée', () => {
    factures.mesFactures.mockReturnValue(of([{ statutFacture: 'EMISE' }]));
    const fixture = TestBed.createComponent(DashboardAccueil);
    fixture.detectChanges();
    const etape = fixture.nativeElement.querySelector('.accueil-prochaine-etape') as HTMLElement;
    expect(etape.textContent).toContain('Une facture est disponible');
    expect(etape.textContent).not.toContain('payer');
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
