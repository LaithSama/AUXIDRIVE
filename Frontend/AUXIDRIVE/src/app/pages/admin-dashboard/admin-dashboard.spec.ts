import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Assure } from '../../core/interfaces/assure';
import { CompteClientVue } from '../../core/interfaces/compte-client';
import { Statistiques } from '../../core/interfaces/statistiques';
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
});
