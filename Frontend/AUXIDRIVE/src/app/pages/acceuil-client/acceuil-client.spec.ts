import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AcceuilClient } from './acceuil-client';

describe('AcceuilClient', () => {
  let component: AcceuilClient;
  let fixture: ComponentFixture<AcceuilClient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AcceuilClient],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AcceuilClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose the marketing content', () => {
    expect(component.horaires.length).toBeGreaterThan(0);
    expect(component.reflexes.length).toBe(4);
    expect(component.telephones.length).toBe(2);
  });

  it('affiche les liens de navigation mobile puis ferme le menu après sélection', () => {
    fixture.detectChanges();
    const bouton: HTMLButtonElement = fixture.nativeElement.querySelector('.accueil-menu-bouton');
    const menu: HTMLElement = fixture.nativeElement.querySelector('#accueil-menu');
    expect(bouton.classList.contains('d-lg-none')).toBe(true);
    expect(menu.classList.contains('d-lg-none')).toBe(true);
    expect(fixture.nativeElement.querySelector('.entete .lien').classList.contains('d-lg-inline')).toBe(true);

    bouton.click();
    fixture.detectChanges();
    expect(bouton.getAttribute('aria-expanded')).toBe('true');
    expect(menu.classList.contains('accueil-menu-ouvert')).toBe(true);

    menu.querySelector<HTMLAnchorElement>('a')!.click();
    fixture.detectChanges();
    expect(bouton.getAttribute('aria-expanded')).toBe('false');
    expect(menu.classList.contains('accueil-menu-ouvert')).toBe(false);
  });

  it('présente le parcours après la demande et garde le formulaire au premier plan', () => {
    fixture.detectChanges();
    const accueil: HTMLElement = fixture.nativeElement.querySelector('.accueil');
    expect(accueil.querySelector('.accueil-visuel svg')).toBeTruthy();
    expect(accueil.querySelector('.accueil-suite')?.textContent).toContain('Un agent le vérifie');
    expect(accueil.querySelector('.plaque-form button[type="submit"]')?.textContent).toContain('Faire ma demande');
    expect(accueil.querySelector('.plaque-form')!.compareDocumentPosition(accueil.querySelector('.accueil-suite')!)
      & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('compare les offres avec les caractéristiques renvoyées par le catalogue', () => {
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('http://localhost:8080/api/offres').flush([
      { libelleOffre: 'Monovéhicule', typeOffre: 'MONOVEHICULE', dureeMinMois: 1, nbVehiculesMin: 1, tauxRemise: 0, description: '' },
      { libelleOffre: 'Flotte', typeOffre: 'FLOTTE', dureeMinMois: 18, nbVehiculesMin: 4, tauxRemise: 7, description: '' },
    ]);
    fixture.detectChanges();

    const cartes: NodeListOf<HTMLElement> = fixture.nativeElement.querySelectorAll('.formule');
    expect(cartes.length).toBe(2);
    expect(cartes[0].textContent).toContain('De 1 à 12 mois');
    expect(cartes[1].textContent).toContain('À partir de 4 véhicules');
    expect(cartes[1].textContent).toContain('18 mois minimum');
    expect(cartes[1].textContent).toContain('7 % de remise');
    expect(cartes[1].querySelector('.formule-lien')?.textContent).toContain('Faire une demande');
  });
});
