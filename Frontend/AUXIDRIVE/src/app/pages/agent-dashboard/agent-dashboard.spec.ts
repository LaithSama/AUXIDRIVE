import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../../services/auth-service';
import { AgentDashboard } from './agent-dashboard';

describe('AgentDashboard', () => {
  const http = { get: vi.fn() };

  beforeEach(async () => {
    http.get.mockImplementation((url: string) => {
      if (url.endsWith('/inscriptions')) return of([
        { statutInscription: 'EN_ATTENTE' }, { statutInscription: 'VALIDEE' },
      ]);
      if (url.endsWith('/contrats')) return of([
        { statutContrat: 'EN_ATTENTE' }, { statutContrat: 'VALIDE' },
      ]);
      return of([
        { statutSinistre: 'DECLARE' }, { statutSinistre: 'RAPPORT_EN_COURS' },
      ]);
    });
    await TestBed.configureTestingModule({
      imports: [AgentDashboard],
      providers: [
        provideRouter([]),
        { provide: HttpClient, useValue: http },
        { provide: AuthService, useValue: { getEmail: () => 'agent@mail.mg', getRole: () => 'AGENT' } },
      ],
    }).compileComponents();
  });

  it('ouvre le tableau de bord en premier et conserve l’accès aux inscriptions', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    fixture.detectChanges();
    expect(fixture.componentInstance.vue()).toBe('tableau');
    expect(fixture.nativeElement.querySelectorAll('.agent-apercu-carte').length).toBe(3);
    expect(fixture.nativeElement.querySelector('.agent-apercu').textContent)
      .toContain('Inscriptions à vérifier');

    const menu = Array.from(fixture.nativeElement.querySelectorAll('.agent-sidebar-lien'))
      .find((bouton: any) => bouton.textContent.includes('Inscriptions')) as HTMLButtonElement;
    menu.click();
    fixture.detectChanges();

    expect(fixture.componentInstance.vue()).toBe('inscriptions');
  });

  it('affiche les compteurs issus des listes et ouvre le bon onglet de traitement', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.changerVue('tableau');
    fixture.detectChanges();

    const compteurs = fixture.nativeElement.querySelectorAll('.agent-apercu-valeur');
    expect(Array.from(compteurs, (element: any) => element.textContent.trim())).toEqual(['1', '1', '2']);
    expect(fixture.nativeElement.querySelector('.agent-apercu-suite').textContent)
      .toContain('rapports de sinistre en cours');

    page.ongletContrat.set('VALIDE');
    (fixture.nativeElement.querySelectorAll('.agent-apercu-carte')[1] as HTMLButtonElement).click();
    expect(page.vue()).toBe('contrats');
    expect(page.ongletContrat()).toBe('EN_ATTENTE');
  });

  it('affiche les dossiers sous forme de cartes jusqu’à la largeur ordinateur', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    fixture.detectChanges();
    fixture.componentInstance.changerVue('inscriptions');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.agent-tableau-carte').classList.contains('d-lg-block')).toBe(true);
    expect(fixture.nativeElement.querySelector('.liste-mobile').classList.contains('d-lg-none')).toBe(true);
  });

  it('réserve le repère action requise aux dossiers à traiter sur bureau et mobile', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    for (const vue of ['inscriptions', 'contrats', 'sinistres'] as const) {
      page.changerVue(vue);
      fixture.detectChanges();
      const racine = fixture.nativeElement as HTMLElement;
      expect(racine.querySelector('.agent-tableau tbody .agent-ligne-action')).not.toBeNull();
      expect(racine.querySelector('.liste-mobile .agent-carte-action')).not.toBeNull();
      expect(racine.querySelector('.agent-pastille-action')).not.toBeNull();
    }

    page.changerVue('contrats');
    page.changerOngletContrat('VALIDE');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.agent-ligne-action')).toBeNull();
    expect(fixture.nativeElement.querySelector('.agent-carte-action')).toBeNull();
    expect(fixture.nativeElement.querySelector('.agent-pastille-action')).toBeNull();

    page.changerVue('sinistres');
    page.changerOngletSinistre('RAPPORT_EN_COURS');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.agent-pastille-action')?.textContent).toContain('À clôturer');
  });

  it('ouvre les sinistres à clôturer si aucun ne reste à instruire', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.sinistres.set(page.sinistresEnCours());
    page.ouvrirDossiers('sinistres');
    fixture.detectChanges();
    expect(page.ongletSinistre()).toBe('RAPPORT_EN_COURS');
    expect(fixture.nativeElement.querySelector('.agent-apercu-carte')).toBeNull();
    expect(fixture.nativeElement.querySelector('.agent-pastille-action')?.textContent).toContain('À clôturer');
  });

  it('distingue aucun dossier à traiter d’un échec de chargement', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.toutesLesInscriptions.set([]);
    page.contrats.set([]);
    page.sinistres.set([]);
    page.changerVue('tableau');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.agent-apercu').textContent)
      .toContain('Aucune demande en attente');
    expect(fixture.nativeElement.querySelectorAll('.agent-apercu-carte-active').length).toBe(0);

    vi.spyOn(console, 'error').mockImplementation(() => {});
    http.get.mockImplementation(() => throwError(() => ({ status: 500 })));
    page.actualiserTableau();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.agent-apercu')).toBeNull();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('Impossible de charger tous les dossiers');
    http.get.mockImplementation(() => of([]));
    (fixture.nativeElement.querySelector('.agent-contenu .agent-btn') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.agent-apercu')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.agent-apercu').textContent)
      .toContain('Aucune demande en attente');
    vi.restoreAllMocks();
  });
});
