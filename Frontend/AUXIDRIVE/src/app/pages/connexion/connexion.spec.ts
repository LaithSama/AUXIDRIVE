import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgForm } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';

import { Connexion } from './connexion';

describe('Connexion', () => {
  let component: Connexion;
  let fixture: ComponentFixture<Connexion>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Connexion],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Connexion);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => httpMock.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('refuse un formulaire incomplet avant toute requête', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.onSubmit(fixture.debugElement.query(By.directive(NgForm)).injector.get(NgForm));
    fixture.detectChanges();
    expect(httpMock.match('http://localhost:8080/api/auth/login')).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('adresse e-mail valide');
  });

  it('bloque les doubles envois et permet de réessayer après une erreur', () => {
    component.email = 'agent@mail.mg';
    component.motDePasse = 'secret';
    component.onSubmit();
    component.onSubmit();
    fixture.detectChanges();
    expect((fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement).disabled).toBe(true);
    const requetes = httpMock.match('http://localhost:8080/api/auth/login');
    expect(requetes).toHaveLength(1);
    requetes[0].flush('Identifiants invalides', { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();
    expect(component.errorMessage).toBe('Identifiants invalides.');
    expect((fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement).disabled).toBe(false);
    component.onSubmit();
    httpMock.expectOne('http://localhost:8080/api/auth/login').flush('Compte désactivé', { status: 403, statusText: 'Forbidden' });
    expect(component.errorMessage).toBe('Compte non validé ou désactivé.');
  });

  it('distingue une panne réseau d’une erreur serveur', () => {
    component.email = 'agent@mail.mg';
    component.motDePasse = 'secret';
    component.onSubmit();
    httpMock.expectOne('http://localhost:8080/api/auth/login').error(new ProgressEvent('error'));
    expect(component.errorMessage).toContain('Connexion au serveur impossible');
    component.onSubmit();
    httpMock.expectOne('http://localhost:8080/api/auth/login').flush({}, { status: 500, statusText: 'Server Error' });
    expect(component.errorMessage).toContain('erreur serveur');
  });
});
