import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../../services/auth-service';
import { AgentDashboard } from './agent-dashboard';

describe('AgentDashboard', () => {
  const http = { get: vi.fn(), put: vi.fn() };

  beforeEach(async () => {
    http.get.mockImplementation((url: string) => {
      if (url.endsWith('/inscriptions')) return of([
        { statutInscription: 'EN_ATTENTE' }, { statutInscription: 'VALIDEE' },
      ]);
      if (url.endsWith('/contrats')) return of([
        { statutContrat: 'EN_ATTENTE' }, { statutContrat: 'VALIDE' },
      ]);
      if (url.endsWith('/documents/a-verifier')) return of([
        { idDocument: 7, typeDocument: 'CIN', formatFichier: 'jpg', statutDocuments: 'EN_ATTENTE', origine: 'PROFIL' },
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
    expect(fixture.nativeElement.querySelectorAll('.agent-apercu-carte').length).toBe(4);
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
    expect(Array.from(compteurs, (element: any) => element.textContent.trim())).toEqual(['1', '1', '2', '1']);
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
    page.documentsVerification.set([]);
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

  it('affiche un squelette puis une erreur, sans état vide, pour une liste indisponible', () => {
    const attente = new Subject<any[]>();
    http.get.mockImplementation((url: string) => url.endsWith('/contrats') ? attente.asObservable() : of([]));
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    page.vue.set('contrats');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-squelette[role="status"]')?.textContent)
      .toContain('Chargement des contrats');
    expect(fixture.nativeElement.querySelector('app-etat-vide')).toBeNull();

    const erreurConsole = vi.spyOn(console, 'error').mockImplementation(() => {});
    attente.error({ status: 500 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.message-erreur')?.textContent)
      .toContain('Impossible de charger les contrats');
    expect(fixture.nativeElement.querySelector('app-etat-vide')).toBeNull();
    erreurConsole.mockRestore();
  });

  it('vérifie les pièces ajoutées depuis le profil client', () => {
    http.put.mockReturnValue(of({}));
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.changerVue('documents');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.agent-document-verif').length).toBe(1);
    expect(fixture.nativeElement.querySelector('.agent-document-verif').textContent)
      .toContain('Carte d’identité');

    page.motifVerification[7] = 'Photo floue';
    page.examinerDocumentVerification(page.documentsVerification()[0], 'rejeter');

    expect(http.put).toHaveBeenCalledWith(
      expect.stringContaining('/documents/7/rejeter'),
      null,
      expect.objectContaining({ params: { motif: 'Photo floue' } }),
    );
  });

  it('affiche les pièces des deux espaces dans une seule modale et ferme l’aperçu sans fermer l’inscription', () => {
    const creerUrl = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:apercu-test');
    const revoquerUrl = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});
    http.get.mockImplementation((url: string) => {
      if (url.endsWith('/fichier')) return of(new Blob(['photo'], { type: 'image/jpeg' }));
      if (url.endsWith('/inscriptions/42')) return of({ documents: [
        { idDocument: 8, typeDocument: 'CARTE_GRISE', formatFichier: 'pdf', statutDocuments: 'EN_ATTENTE' },
      ] });
      if (url.endsWith('/documents/a-verifier')) return of([
        { idDocument: 7, typeDocument: 'CIN', formatFichier: 'jpg', statutDocuments: 'EN_ATTENTE', origine: 'PROFIL' },
      ]);
      return of([]);
    });

    try {
      const fixture = TestBed.createComponent(AgentDashboard);
      const page = fixture.componentInstance;
      fixture.detectChanges();
      page.changerVue('documents');
      fixture.detectChanges();
      (fixture.nativeElement.querySelector('.agent-document-verif-actions button') as HTMLButtonElement).click();
      fixture.detectChanges();

      const racine = fixture.nativeElement as HTMLElement;
      expect(racine.querySelectorAll('.document-modale-apercu').length).toBe(1);
      expect(racine.querySelector('.document-modale-apercu')?.getAttribute('aria-modal')).toBe('true');
      expect(racine.querySelector('.document-modale-apercu')?.textContent).toContain('Carte d’identité');
      expect(racine.querySelector('.document-modale-apercu img')).not.toBeNull();
      expect(racine.querySelector('.agent-document-apercu')).toBeNull();

      (racine.querySelector('.document-fermer') as HTMLButtonElement).click();
      fixture.detectChanges();
      expect(racine.querySelector('.document-modale-apercu')).toBeNull();
      expect(revoquerUrl).toHaveBeenCalledWith('blob:apercu-test');

      page.ouvrirModale({ idInscription: 42, statutInscription: 'EN_ATTENTE', identifiants: { cin: '123456' } }, 'VALIDER');
      fixture.detectChanges();
      page.voirDocument(page.documentsInscription[0]);
      fixture.detectChanges();
      expect(racine.querySelectorAll('.document-modale-apercu').length).toBe(1);
      expect(racine.querySelector('.document-modale-apercu iframe')).not.toBeNull();
      expect(racine.querySelector('.document-modale-apercu')?.textContent).toContain('Carte grise');
      document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
      fixture.detectChanges();
      expect(racine.querySelector('.document-modale-apercu')).toBeNull();
      expect(racine.querySelector('.agent-modale')).not.toBeNull();

      page.voirDocument(page.documentsInscription[0]);
      fixture.detectChanges();
      (racine.querySelector('.document-voile') as HTMLElement).click();
      fixture.detectChanges();
      expect(racine.querySelector('.document-modale-apercu')).toBeNull();
      expect(creerUrl).toHaveBeenCalledTimes(3);
    } finally {
      creerUrl.mockRestore();
      revoquerUrl.mockRestore();
    }
  });

  it('ne rouvre pas un aperçu après un changement de vue pendant le chargement', () => {
    const fichier = new Subject<Blob>();
    http.get.mockImplementation((url: string) => url.endsWith('/fichier') ? fichier.asObservable()
      : url.endsWith('/documents/a-verifier') ? of([
        { idDocument: 7, typeDocument: 'CIN', formatFichier: 'jpg', origine: 'PROFIL' },
      ]) : of([]));
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.changerVue('documents');
    page.voirDocumentVerification(page.documentsVerification()[0]);
    page.changerVue('tableau');
    fichier.next(new Blob(['photo']));
    fixture.detectChanges();
    expect(page.apercuDocument).toBeNull();
    expect(fixture.nativeElement.querySelector('.document-modale-apercu')).toBeNull();
  });

  it('bloque la création du compte tant que les pièces ne sont pas validées', () => {
    http.get.mockImplementation((url: string) => url.endsWith('/inscriptions/42')
      ? of({ documents: [{ idDocument: 7, typeDocument: 'CIN', formatFichier: 'jpg', statutDocuments: 'EN_ATTENTE' }] })
      : of([]));
    http.put.mockReturnValue(of({}));
    const fixture = TestBed.createComponent(AgentDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.ouvrirModale({ idInscription: 42, statutInscription: 'EN_ATTENTE', identifiants: { cin: '123456' } }, 'VALIDER');
    page.numAssureSaisi = '10001';
    fixture.detectChanges();
    expect(page.documentsPrets).toBe(false);
    expect((fixture.nativeElement.querySelector('.agent-modale-piede .btn-signal') as HTMLButtonElement).disabled).toBe(true);

    page.examinerDocument(page.documentsInscription[0], 'valider');
    expect(http.put).toHaveBeenCalledWith(expect.stringContaining('/documents/7/valider'), null, expect.anything());
  });
});
