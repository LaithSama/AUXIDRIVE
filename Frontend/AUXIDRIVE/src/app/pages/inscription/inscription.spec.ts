import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgForm } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';

import { Inscription } from './inscription';
import { environment } from '../../../environments/environment';

const API = `${environment.apiUrl}/api/inscriptions`;

describe('Inscription', () => {
  let component: Inscription;
  let fixture: ComponentFixture<Inscription>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inscription],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Inscription);
    component = fixture.componentInstance;
    component.identifiantsLegaux.cin = '101234567890';
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('exige un numéro de CIN ou sa photo avant transmission', () => {
    component.identifiantsLegaux.cin = '';
    component.onSubmit();
    expect(component.messageErreur).toContain('numéro de CIN');
    expect(httpMock.match(API)).toHaveLength(0);

    component.fichierCin = new File(['image'], 'cin.jpg', { type: 'image/jpeg' });
    component.onSubmit();
    const requete = httpMock.expectOne(API);
    expect((requete.request.body as FormData).get('cin')).toBe(component.fichierCin);
    requete.flush({ idInscription: 4 });
  });

  it('transmet le dossier et affiche la confirmation', () => {
    component.onSubmit();
    expect(component.enCours).toBe(true);

    const requete = httpMock.expectOne(API);
    expect(requete.request.method).toBe('POST');
    requete.flush({ idInscription: 12, statut: 'EN_ATTENTE', message: 'Demande enregistrée' });

    expect(component.enCours).toBe(false);
    expect(component.dossierEnvoye).toBe(true);
    expect(component.numeroDossier).toBe(12);
    expect(component.compteArebours).toBe(10);

    fixture.detectChanges();
    const confirmation = fixture.nativeElement.querySelector('.inscription-confirmation');
    expect(confirmation).toBeTruthy();
    expect(confirmation.textContent).toContain('Dossier n° 12');
    expect(confirmation.textContent).toContain('patienter');
  });

  it("refuse un second envoi pendant l'envoi en cours", () => {
    component.onSubmit();
    component.onSubmit();

    const requetes = httpMock.match(API);
    expect(requetes.length).toBe(1);
    requetes[0].flush({ idInscription: 3, statut: 'EN_ATTENTE', message: 'ok' });
  });

  it('signale les champs manquants sans envoyer le dossier', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.onSubmit(fixture.debugElement.query(By.directive(NgForm)).injector.get(NgForm));
    fixture.detectChanges();
    expect(httpMock.match(API)).toHaveLength(0);
    expect(component.enCours).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('champs obligatoires');
  });

  it('affiche le conflit et permet un nouvel envoi', () => {
    component.onSubmit();
    httpMock.expectOne(API).flush({ detail: 'Cette adresse e-mail est déjà utilisée' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(component.messageErreur).toContain('déjà utilisée');
    expect(component.enCours).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('déjà utilisée');
    component.onSubmit();
    httpMock.expectOne(API).flush({ idInscription: 9 });
    expect(component.dossierEnvoye).toBe(true);
  });

  it('explique une erreur de validation sans afficher de JSON brut', () => {
    component.onSubmit();
    httpMock.expectOne(API).flush({ status: 400, title: 'Bad Request' }, { status: 400, statusText: 'Bad Request' });
    expect(component.messageErreur).toContain('Vérifiez les informations');
    expect(component.messageErreur).not.toContain('{');
  });

  it('distingue les erreurs réseau, de taille de fichier et serveur', () => {
    component.onSubmit();
    httpMock.expectOne(API).error(new ProgressEvent('error'));
    expect(component.messageErreur).toContain('Connexion au serveur impossible');
    component.onSubmit();
    httpMock.expectOne(API).flush({}, { status: 413, statusText: 'Content Too Large' });
    expect(component.messageErreur).toContain('5 Mo maximum');
    component.onSubmit();
    httpMock.expectOne(API).flush({ message: 'Erreur interne' }, { status: 500, statusText: 'Server Error' });
    expect(component.messageErreur).toContain('Veuillez réessayer');
    expect(component.messageErreur).not.toContain('Erreur interne');
  });
});
