import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Profil } from '../../../core/interfaces/profil';
import { DashboardProfil } from './profil';

const API = 'http://localhost:8080/api/profil';

const PROFIL: Profil = {
  compte: { email: 'client@auxi.mg', dateCreation: '2026-01-05T09:00:00', dateSuppressionPlanifiee: null },
  assure: {
    numerosAssure: 'A-001',
    civilite: 'M.',
    dateNaissance: '1990-05-12',
    nomAssure: 'Ranaivo',
    prenomAssure: 'Hery',
    adresse: 'Antananarivo',
    contact: '034 00 000 00',
    mail: 'client@auxi.mg',
  },
};

describe('DashboardProfil', () => {
  let component: DashboardProfil;
  let fixture: ComponentFixture<DashboardProfil>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardProfil],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardProfil);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const requete = httpMock.expectOne(API);
    expect(requete.request.method).toBe('GET');
    requete.flush(PROFIL);
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load the profile', () => {
    expect(component).toBeTruthy();
    expect(component.chargement).toBe(false);
    expect(component.nom).toBe('Ranaivo');
    expect(component.prenom).toBe('Hery');
    expect(component.emailConnexion).toBe('client@auxi.mg');
    expect(component.suppressionPlanifiee).toBe(false);
  });

  it('validates the information form and the password form', () => {
    component.nom = '';
    expect(component.informationsValides).toBe(false);
    component.nom = 'Ranaivo';
    expect(component.informationsValides).toBe(true);

    component.mdpActuel = 'ancien';
    component.mdpNouveau = 'court';
    component.mdpConfirmation = 'court';
    expect(component.motDePasseValide).toBe(false);
    component.mdpNouveau = 'abcdefghij';
    component.mdpConfirmation = 'abcdefghij';
    expect(component.motDePasseValide).toBe(true);
    component.mdpConfirmation = 'autre';
    expect(component.motDePasseValide).toBe(false);
  });

  it('enregistrer() sends the updated information', () => {
    component.adresse = 'Toamasina';
    component.enregistrer();

    const requete = httpMock.expectOne((r) => r.url === API && r.method === 'PUT');
    expect(requete.request.body.adresse).toBe('Toamasina');
    requete.flush({ ...PROFIL, assure: { ...PROFIL.assure, adresse: 'Toamasina' } });

    expect(component.messageSucces).toContain('enregistrées');
    expect(component.adresse).toBe('Toamasina');
  });

  it('enregistrer() reports a duplicate login e-mail', () => {
    component.emailConnexion = 'dupe@auxi.mg';
    component.enregistrer();

    const requete = httpMock.expectOne((r) => r.url === API && r.method === 'PUT');
    requete.flush({ message: 'existe' }, { status: 409, statusText: 'Conflict' });

    expect(component.messageErreur).toContain('déjà utilisé');
    expect(component.messageSucces).toBe('');
  });

  it('plans then cancels the account deletion', () => {
    component.planifierSuppression();
    const planifiee = httpMock.expectOne(`${API}/suppression`);
    expect(planifiee.request.method).toBe('POST');
    planifiee.flush({ dateSuppressionPlanifiee: '2026-10-11T10:00:00' });

    expect(component.suppressionPlanifiee).toBe(true);
    expect(component.dateSuppressionFr).toContain('octobre');

    component.annulerSuppression();
    const annulee = httpMock.expectOne(`${API}/suppression/annuler`);
    annulee.flush({ dateSuppressionPlanifiee: null });

    expect(component.suppressionPlanifiee).toBe(false);
    expect(component.messageSucces).toContain('annulée');
  });
});
