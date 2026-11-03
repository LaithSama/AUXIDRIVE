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

  it('regroupe les filtres et hiérarchise les cartes sans masquer les actions principales', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    for (const vue of ['inscriptions', 'contrats', 'sinistres'] as const) {
      page.changerVue(vue);
      fixture.detectChanges();
      const racine = fixture.nativeElement as HTMLElement;
      expect(racine.querySelector('.agent-filtres-recherche input')).not.toBeNull();
      expect(racine.querySelector('.agent-filtres-options .saisie')).not.toBeNull();
      expect(racine.querySelector('.agent-filtres-pied .agent-btn-annuler')).not.toBeNull();
      expect(racine.querySelector('.agent-filtres-pied .agent-filtres-compteur')).not.toBeNull();
      expect(racine.querySelector('.liste-mobile .agent-fiche .liste-mobile-titre')).not.toBeNull();
      expect(racine.querySelector('.liste-mobile .agent-fiche .pastille')).not.toBeNull();
      expect(racine.querySelector('.liste-mobile .agent-fiche .liste-mobile-details')).not.toBeNull();
    }

    page.changerVue('inscriptions');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.agent-tableau .agent-btn-valider')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.agent-tableau .agent-btn-refuser')).not.toBeNull();
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

  for (const vue of ['inscriptions', 'contrats', 'sinistres'] as const) {
    it(`pagine les ${vue} après filtrage sur bureau et mobile`, () => {
      const fixture = TestBed.createComponent(AgentDashboard);
      const page = fixture.componentInstance;
      fixture.detectChanges();
      page.changerVue(vue);
      const ids = Array.from({ length: 11 }, (_, i) => i + 1);
      if (vue === 'inscriptions') {
        page.toutesLesInscriptions.set(ids.map(id => ({
          idInscription: id, statutInscription: 'EN_ATTENTE', assure: { nomAssure: `Dossier ${String(id).padStart(2, '0')}` },
        })));
      } else if (vue === 'contrats') {
        page.contrats.set(ids.map(id => ({
          ...page.contrats()[0], idContrat: id, statutContrat: 'EN_ATTENTE' as const,
          nomAssure: `Dossier ${String(id).padStart(2, '0')}`,
        })));
      } else {
        page.sinistres.set(ids.map(id => ({
          ...page.sinistres()[0], idSinistre: id, statutSinistre: 'DECLARE' as const,
          nomAssure: `Dossier ${String(id).padStart(2, '0')}`,
        })));
      }
      fixture.detectChanges();

      const racine = fixture.nativeElement as HTMLElement;
      const pagination = racine.querySelector('.agent-pagination') as HTMLElement;
      expect(pagination).not.toBeNull();
      expect(pagination.compareDocumentPosition(racine.querySelector('.liste-mobile')!) & Node.DOCUMENT_POSITION_PRECEDING).toBeTruthy();
      expect(pagination.textContent).toContain('Page 1 sur 2');
      expect(racine.querySelector('.agent-filtres-compteur')?.textContent).toContain('11 résultat(s)');
      expect(racine.querySelectorAll('.agent-tableau tbody tr').length).toBe(10);
      expect(racine.querySelectorAll('.liste-mobile .agent-fiche').length).toBe(10);

      const boutons = pagination.querySelectorAll('button');
      expect(boutons[0].disabled).toBe(true);
      boutons[1].click();
      fixture.detectChanges();
      expect(pagination.textContent).toContain('Page 2 sur 2');
      expect(boutons[1].disabled).toBe(true);
      expect(racine.querySelectorAll('.agent-tableau tbody tr').length).toBe(1);
      expect(racine.querySelectorAll('.liste-mobile .agent-fiche').length).toBe(1);

      const recherche = racine.querySelector('.agent-filtres-recherche input') as HTMLInputElement;
      recherche.value = 'Dossier 01';
      recherche.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(racine.querySelector('.agent-pagination')).toBeNull();
      expect(racine.querySelector('.agent-filtres-compteur')?.textContent).toContain('1 résultat(s)');
      expect(racine.querySelectorAll('.agent-tableau tbody tr').length).toBe(1);

      page.reinitialiserFiltres();
      fixture.detectChanges();
      expect(racine.querySelector('.agent-pagination')?.textContent).toContain('Page 1 sur 2');
    });
  }

  it('revient à la première page en changeant d’onglet et ajuste la page après actualisation', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.changerVue('contrats');
    const contrats = Array.from({ length: 11 }, (_, i) => ({
      ...page.contrats()[0], idContrat: i + 1, statutContrat: 'EN_ATTENTE' as const,
    }));
    page.contrats.set(contrats);
    page.changerPage(page.pageContrats, 2, page.totalPagesContrats());
    fixture.detectChanges();
    page.changerOngletContrat('VALIDE');
    expect(page.pageContrats()).toBe(1);
    page.changerOngletContrat('EN_ATTENTE');
    page.changerPage(page.pageContrats, 2, page.totalPagesContrats());
    page.chargerContrats();
    fixture.detectChanges();
    expect(page.pageContrats()).toBe(1);
    expect(page.contratsPage().length).toBe(1);
    expect(fixture.nativeElement.querySelector('.agent-pagination')).toBeNull();
  });

  it('ne montre pas la navigation quand une liste contient exactement dix dossiers', () => {
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.changerVue('inscriptions');
    page.toutesLesInscriptions.set(Array.from({ length: 10 }, (_, i) => ({
      idInscription: i + 1, statutInscription: 'EN_ATTENTE', assure: { nomAssure: `Dossier ${i + 1}` },
    })));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.agent-tableau tbody tr').length).toBe(10);
    expect(fixture.nativeElement.querySelector('.agent-pagination')).toBeNull();
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
