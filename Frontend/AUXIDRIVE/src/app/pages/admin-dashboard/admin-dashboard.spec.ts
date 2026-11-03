import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Assure } from '../../core/interfaces/assure';
import { CompteClientVue } from '../../core/interfaces/compte-client';
import { Statistiques } from '../../core/interfaces/statistiques';
import { Utilisateur } from '../../core/interfaces/utilisateur';
import { AdminService } from '../../services/admin-service';
import { AuthService } from '../../services/auth-service';
import { GraphiquesAdminService } from '../../services/graphiques-admin.service';
import { UtilisateurService } from '../../services/utilisateur-service';
import { AdminDashboard } from './admin-dashboard';

describe('AdminDashboard : modification', () => {
  const assure: Assure = {
    idAssure: 7, numerosAssure: '20260007', nomAssure: 'Rakoto', prenomAssure: 'Jean',
    personneMorale: false, dateNaissance: '', sexe: '', civilite: '', nationalite: '',
    paysResidence: '', adresse: 'Ancienne adresse', contact: '', mail: 'jean@mail.mg',
    dateCreation: '2026-10-01',
  };
  const compte: CompteClientVue = {
    idCompteClient: 3, idAssure: 7, email: 'jean@mail.mg', statutCompte: 'ACTIF',
    nomAssure: 'Rakoto', prenomAssure: 'Jean',
  };
  const stats: Statistiques = {
    utilisateurs: 12, agents: 9, administrateurs: 3, comptesClients: 25,
    comptesClientsActifs: 20, assures: 30, contrats: 42, contratsEnAttente: 4,
    contratsValides: 32, contratsRefuses: 6, inscriptionsEnAttente: 2,
  };
  const admin = {
    getStatistiques: vi.fn(() => of(null)), getRoles: vi.fn(() => of([])),
    getComptesClients: vi.fn(() => of([compte])), getAssures: vi.fn(() => of([assure])),
    modifierAssure: vi.fn(), modifierCompteClient: vi.fn(),
  };

  beforeEach(async () => {
    admin.modifierAssure.mockReset();
    admin.modifierCompteClient.mockReset();
    await TestBed.configureTestingModule({
      imports: [AdminDashboard],
      providers: [
        provideRouter([]),
        { provide: AdminService, useValue: admin },
        { provide: AuthService, useValue: { getEmail: () => 'admin@mail.mg' } },
        { provide: UtilisateurService, useValue: { getUtilisateurs: () => of([]) } },
        { provide: GraphiquesAdminService, useValue: {
          getContrats: () => of([]), getInscriptions: () => of([]),
        } },
      ],
    }).compileComponents();
  });

  it('enregistre la modification d’un assuré puis ferme la modale', () => {
    admin.modifierAssure.mockReturnValue(of({ ...assure, adresse: 'Nouvelle adresse' }));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('assures');
    fixture.detectChanges();

    page.ouvrirEditionAssure(assure);
    fixture.detectChanges();
    const adresse = fixture.nativeElement.querySelector('#a-adresse') as HTMLInputElement;
    adresse.value = 'Nouvelle adresse';
    adresse.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.admin-modale-pied .btn-signal') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(admin.modifierAssure).toHaveBeenCalledWith(7, expect.objectContaining({ adresse: 'Nouvelle adresse' }));
    expect(page.sectionModale()).toBeNull();
    expect(page.messageSucces).toContain('modifié');
  });

  it('prévoit les cartes pour les listes sur tablette', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    fixture.componentInstance.vue.set('assures');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.admin-tableau-carte').classList.contains('d-lg-block')).toBe(true);
    expect(fixture.nativeElement.querySelector('.liste-mobile').classList.contains('d-lg-none')).toBe(true);
  });

  it('présente les champs assurés par groupes sans imposer le prénom aux anciennes fiches', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.ouvrirEditionAssure({ ...assure, prenomAssure: null } as unknown as Assure);
    fixture.detectChanges();

    const modale = fixture.nativeElement.querySelector('.admin-modale') as HTMLElement;
    expect(modale.getAttribute('aria-labelledby')).toBe('admin-modale-titre');
    expect(Array.from(modale.querySelectorAll('.admin-form-groupe-titre'), titre => titre.textContent?.trim()))
      .toEqual(['Identité', 'Résidence', 'Contact']);
    expect(modale.querySelector('#a-nom')?.getAttribute('aria-required')).toBe('true');
    expect(modale.querySelector('#a-prenom')?.getAttribute('aria-required')).toBe('false');
    expect(modale.querySelector('.admin-modale-pied .btn-signal')).not.toBeNull();
  });

  it('présente le compte rattaché comme référence et isole les identifiants après création', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.ouvrirEditionCompte(compte);
    fixture.detectChanges();

    const modale = fixture.nativeElement.querySelector('.admin-modale') as HTMLElement;
    expect(modale.querySelector('.admin-lecture-seule')?.textContent).toContain('Rakoto Jean');
    expect(modale.querySelector('.admin-champ-reference input')).toBeNull();

    page.motDePasseTemporaire.set('secret-temporaire');
    page.emailEnvoye.set(false);
    fixture.detectChanges();
    expect(modale.querySelector('.admin-identifiants')?.textContent).toContain('secret-temporaire');
    expect(modale.querySelector('.admin-identifiants')?.textContent).toContain('uniquement au client');
    expect(modale.querySelector('.message-erreur')?.textContent).toContain("l'e-mail n'a pas pu être envoyé");
  });

  it('affiche distinctement le nom de la cible dans la confirmation de suppression', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.demanderSuppression('assure', assure.idAssure, 'Rakoto Jean');
    fixture.detectChanges();

    const confirmation = fixture.nativeElement.querySelector('[role="alertdialog"]') as HTMLElement;
    expect(confirmation.getAttribute('aria-labelledby')).toBe('admin-suppression-titre');
    expect(confirmation.querySelector('.admin-suppression-cible')?.textContent).toContain('Rakoto Jean');
  });

  for (const section of ['utilisateurs', 'comptes', 'assures'] as const) {
    it(`pagine la liste ${section} sous les affichages bureau et mobile`, () => {
      const fixture = TestBed.createComponent(AdminDashboard);
      const page = fixture.componentInstance;
      page.vue.set(section);
      fixture.detectChanges();

      const ids = Array.from({ length: 11 }, (_, i) => i + 1);
      if (section === 'utilisateurs') {
        page.utilisateurs.set(ids.map(id => ({
          idUtil: id, nomUtil: `Nom ${String(id).padStart(2, '0')}`, email: `u${id}@mail.mg`,
          role: { idRole: 1, libRole: 'AGENT' }, statutUtilisateur: 'ACTIF',
        } as Utilisateur)));
      } else if (section === 'comptes') {
        page.comptes.set(ids.map(id => ({ ...compte, idCompteClient: id, email: `u${id}@mail.mg`, nomAssure: `Nom ${String(id).padStart(2, '0')}` })));
      } else {
        page.assures.set(ids.map(id => ({ ...assure, idAssure: id, numerosAssure: String(id).padStart(8, '0'), nomAssure: `Nom ${String(id).padStart(2, '0')}` })));
      }
      fixture.detectChanges();

      const racine = fixture.nativeElement as HTMLElement;
      expect(racine.querySelector('.admin-filtres-recherche')).not.toBeNull();
      expect(racine.querySelectorAll('.admin-filtres-options select').length).toBe(3);
      expect(racine.querySelector('.admin-filtres-pied .admin-btn-reset')).not.toBeNull();
      expect(racine.querySelectorAll('.liste-mobile .admin-fiche').length).toBe(10);
      expect(racine.querySelectorAll('.admin-tableau-carte .admin-btn-danger[aria-label]').length).toBe(10);
      const pagination = racine.querySelector('.admin-pagination') as HTMLElement;
      expect(pagination).toBeTruthy();
      expect(pagination.compareDocumentPosition(racine.querySelector('.liste-mobile')!) & Node.DOCUMENT_POSITION_PRECEDING).toBeTruthy();
      expect(pagination.textContent).toContain('Page 1 sur 2');
      expect(racine.querySelectorAll('.admin-tableau-carte tbody tr').length).toBe(10);
      expect(racine.querySelectorAll('.liste-mobile-carte').length).toBe(10);
      const boutons = pagination.querySelectorAll('button');
      expect(boutons[0].disabled).toBe(true);
      boutons[1].click();
      fixture.detectChanges();
      expect(pagination.textContent).toContain('Page 2 sur 2');
      expect(boutons[1].disabled).toBe(true);
      expect(racine.querySelectorAll('.admin-tableau-carte tbody tr').length).toBe(1);
      expect(racine.querySelectorAll('.liste-mobile-carte').length).toBe(1);

      const recherche = racine.querySelector('.admin-filtres-recherche input') as HTMLInputElement;
      recherche.value = 'Nom 01';
      recherche.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(racine.querySelector('.admin-pagination')).toBeNull();
      expect(racine.querySelector('.admin-filtres-compteur')?.textContent).toContain('1 résultat(s)');
      expect(racine.querySelectorAll('.admin-tableau-carte tbody tr').length).toBe(1);
    });
  }

  it('masque la navigation à 10 résultats et ajuste la page après une réduction de la liste', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('assures');
    fixture.detectChanges();
    const liste = Array.from({ length: 11 }, (_, i) => ({ ...assure, idAssure: i + 1, nomAssure: `Nom ${i + 1}` }));
    page.assures.set(liste.slice(0, 10));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.admin-pagination')).toBeNull();

    page.assures.set(liste);
    page.changerPage(page.pageAssures, 2, page.totalPagesAssures());
    fixture.detectChanges();
    expect(page.assuresPage().length).toBe(1);
    page.assures.set(liste.slice(0, 10));
    fixture.detectChanges();
    expect(page.pageCouranteAssures()).toBe(1);
    expect(page.assuresPage().length).toBe(10);
    expect(fixture.nativeElement.querySelector('.admin-pagination')).toBeNull();
  });

  it('affiche une erreur de modification d’assuré dans la modale', () => {
    admin.modifierAssure.mockReturnValue(throwError(() => ({ status: 400, error: { message: 'Donnée invalide' } })));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('assures');
    fixture.detectChanges();
    page.ouvrirEditionAssure(assure);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.admin-modale-pied .btn-signal') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(page.sectionModale()).toBe('assure');
    expect(fixture.nativeElement.querySelector('.admin-modale [role="alert"]').textContent)
      .toContain('Donnée invalide');
  });

  it('permet de modifier une fiche ancienne sans numéro ni prénom', () => {
    admin.modifierAssure.mockReturnValue(of({ ...assure, numerosAssure: null, prenomAssure: null }));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('assures');
    fixture.detectChanges();
    page.ouvrirEditionAssure({ ...assure, numerosAssure: null, prenomAssure: null } as unknown as Assure);
    fixture.detectChanges();

    const bouton = fixture.nativeElement.querySelector('.admin-modale-pied .btn-signal') as HTMLButtonElement;
    expect(bouton.disabled).toBe(false);
    bouton.click();
    fixture.detectChanges();

    expect(admin.modifierAssure).toHaveBeenCalledWith(7, expect.objectContaining({ nomAssure: 'Rakoto' }));
    expect(page.sectionModale()).toBeNull();
  });

  it('enregistre l’e-mail et le statut d’un compte client', () => {
    admin.modifierCompteClient.mockReturnValue(of({ ...compte, statutCompte: 'SUSPENDU' }));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('comptes');
    fixture.detectChanges();
    page.ouvrirEditionCompte(compte);
    page.formCompte.statutCompte = 'SUSPENDU';
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.admin-modale-pied .btn-signal') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(admin.modifierCompteClient).toHaveBeenCalledWith(3, {
      email: 'jean@mail.mg', statutCompte: 'SUSPENDU',
    });
    expect(page.sectionModale()).toBeNull();
    expect(page.messageSucces).toContain('modifié');
  });

  it('permet de changer le statut d’un ancien compte sans e-mail', () => {
    admin.modifierCompteClient.mockReturnValue(of({ ...compte, email: null, statutCompte: 'SUSPENDU' }));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('comptes');
    fixture.detectChanges();
    page.ouvrirEditionCompte({ ...compte, email: null } as unknown as CompteClientVue);
    page.formCompte.statutCompte = 'SUSPENDU';
    fixture.detectChanges();

    const bouton = fixture.nativeElement.querySelector('.admin-modale-pied .btn-signal') as HTMLButtonElement;
    expect(bouton.disabled).toBe(false);
    bouton.click();

    expect(admin.modifierCompteClient).toHaveBeenCalledWith(3, { email: '', statutCompte: 'SUSPENDU' });
    expect(page.sectionModale()).toBeNull();
  });

  it('affiche l’erreur de modification d’un compte client dans la modale', () => {
    admin.modifierCompteClient.mockReturnValue(throwError(() => ({
      status: 409, error: { message: 'Cette adresse e-mail est déjà utilisée' },
    })));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('comptes');
    fixture.detectChanges();
    page.ouvrirEditionCompte(compte);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.admin-modale-pied .btn-signal') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(page.sectionModale()).toBe('compte');
    expect(fixture.nativeElement.querySelector('.admin-modale [role="alert"]').textContent)
      .toContain('Cette adresse e-mail est déjà utilisée');
  });

  it('place les dossiers en attente avant les indicateurs et les graphiques', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    page.statistiques.set(stats);
    fixture.detectChanges();

    const suivi = fixture.nativeElement.querySelector('.admin-suivi') as HTMLElement;
    expect(suivi.textContent).toContain('2');
    expect(suivi.textContent).toContain('Inscriptions en attente');
    expect(suivi.textContent).toContain('4');
    expect(suivi.textContent).toContain('Contrats en attente');
    expect(fixture.nativeElement.querySelectorAll('.admin-suivi-carte-active').length).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('.admin-kpi').length).toBe(4);
    expect(fixture.nativeElement.querySelectorAll('.gr-carte').length).toBe(6);
    expect(suivi.compareDocumentPosition(fixture.nativeElement.querySelector('.admin-vue-ensemble'))
      & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

    const naviguer = vi.spyOn(page, 'allerEspaceAgent').mockImplementation(() => {});
    (suivi.querySelector('.admin-suivi-action') as HTMLButtonElement).click();
    expect(naviguer).toHaveBeenCalledOnce();
  });

  it('ne présente pas une absence de dossiers en attente comme une alerte', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    fixture.detectChanges();
    fixture.componentInstance.statistiques.set({ ...stats, inscriptionsEnAttente: 0, contratsEnAttente: 0 });
    fixture.detectChanges();

    const suivi = fixture.nativeElement.querySelector('.admin-suivi') as HTMLElement;
    expect(suivi.textContent).toContain('Aucune inscription à traiter');
    expect(suivi.textContent).toContain('Aucun contrat à traiter');
    expect(suivi.querySelectorAll('.admin-suivi-carte-active').length).toBe(0);
  });

  it('sépare le chargement, l’erreur et le vide des comptes clients', () => {
    const attente = new Subject<CompteClientVue[]>();
    admin.getComptesClients.mockReturnValueOnce(attente.asObservable());
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('comptes');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-squelette[role="status"]')?.textContent)
      .toContain('Chargement des comptes clients');
    expect(fixture.nativeElement.querySelector('app-etat-vide')).toBeNull();

    attente.error({ status: 500 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.message-erreur')?.textContent)
      .toContain('Impossible de charger les comptes clients');
    expect(fixture.nativeElement.querySelector('app-etat-vide')).toBeNull();

    admin.getComptesClients.mockReturnValueOnce(of([]));
    page.chargerComptes();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-etat-vide')?.textContent)
      .toContain('Aucun compte client trouvé');
  });

  it('propose de créer un assuré plutôt qu’un compte sans assuré disponible', () => {
    admin.getAssures.mockReturnValueOnce(of([]));
    admin.getComptesClients.mockReturnValueOnce(of([]));
    const fixture = TestBed.createComponent(AdminDashboard);
    const page = fixture.componentInstance;
    page.vue.set('comptes');
    fixture.detectChanges();
    const bouton = fixture.nativeElement.querySelector('app-etat-vide button') as HTMLButtonElement;
    expect(bouton.textContent).toContain('Ajouter un assuré');
    bouton.click();
    fixture.detectChanges();
    expect(page.vue()).toBe('assures');
    expect(fixture.nativeElement.querySelector('app-etat-vide button')?.textContent)
      .toContain('Créer un assuré');
  });
});
