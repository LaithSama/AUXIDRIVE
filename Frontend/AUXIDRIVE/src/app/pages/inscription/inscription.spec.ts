import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Inscription } from './inscription';

const API = 'http://localhost:8080/api/inscriptions';

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
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
});
