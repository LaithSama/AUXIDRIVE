import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { environment } from '../../../../environments/environment';
import { DashboardContrats } from './contrats';

describe('DashboardContrats - souscription', () => {
  let fixture: ComponentFixture<DashboardContrats>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardContrats],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardContrats);
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/api/contrats/compte`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/api/offres`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/api/vehicules/compte`).flush([]);
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    httpMock.verify();
  });

  it('affiche le véhicule et le contrat responsables du conflit', () => {
    const composant = fixture.componentInstance;
    composant.offres = [{ idOffre: 1, libelleOffre: 'Auto', typeOffre: 'MONOVEHICULE',
      dureeMinMois: 3, nbVehiculesMin: 1, tauxRemise: 0, description: '' }];
    composant.formIdOffre = 1;
    composant.formVehicules = [10];
    composant.formDureeMois = 3;

    composant.souscrire();
    const requete = httpMock.expectOne(`${environment.apiUrl}/api/contrats`);
    expect(requete.request.body.idVehicules).toEqual([10]);
    requete.flush({ message: 'Le véhicule ABC-123 est déjà couvert par le contrat POL-001' },
      { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(composant.erreur).toContain('ABC-123');
    expect(composant.erreur).toContain('POL-001');
  });
});
