import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Vehicule } from '../../../core/interfaces/vehicule';
import { environment } from '../../../../environments/environment';
import { DashboardVehicules } from './vehicules';

const vehicules: Vehicule[] = [
  { idVehicule: 1, numImmatriculation: 'ABC-123', numChassis: 'CHASSIS1', marque: 'Renault', modele: 'Clio', puissanceFiscale: 5, energie: 'Essence', nombrePlaces: 5 },
  { idVehicule: 2, numImmatriculation: 'XYZ-789', numChassis: 'CHASSIS2', marque: 'Toyota', modele: 'Yaris', puissanceFiscale: 4, energie: 'Hybride', nombrePlaces: 5 },
];

describe('DashboardVehicules - suppression', () => {
  let fixture: ComponentFixture<DashboardVehicules>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardVehicules],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardVehicules);
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/api/vehicules/compte`).flush(vehicules);
    httpMock.expectOne(`${environment.apiUrl}/api/referentiels/codes-tarifaires`).flush([]);
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    httpMock.verify();
  });

  it('demande confirmation puis supprime plusieurs véhicules', () => {
    fixture.componentInstance.basculerSelection(1, true);
    fixture.componentInstance.basculerSelection(2, true);
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.veh-actions > button').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alertdialog"]')?.textContent).toContain('2 véhicules');
    expect(httpMock.match((req) => req.method === 'DELETE')).toHaveLength(0);

    fixture.nativeElement.querySelector('.veh-confirmation-boutons .btn-primaire').click();
    httpMock.expectOne(`${environment.apiUrl}/api/vehicules/1`).flush({ supprime: true, idVehicule: 1 });
    httpMock.expectOne(`${environment.apiUrl}/api/vehicules/2`).flush({ supprime: true, idVehicule: 2 });
    fixture.detectChanges();

    expect(fixture.componentInstance.vehicules).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('.veh-actions')?.textContent).toContain('2 véhicules supprimés');
  });

  it('conserve les véhicules liés à un contrat et signale les échecs partiels', () => {
    fixture.componentInstance.basculerSelection(1, true);
    fixture.componentInstance.basculerSelection(2, true);
    fixture.componentInstance.confirmationSuppression = true;
    fixture.componentInstance.supprimerSelection();

    httpMock.expectOne(`${environment.apiUrl}/api/vehicules/1`).flush({ supprime: true, idVehicule: 1 });
    httpMock.expectOne(`${environment.apiUrl}/api/vehicules/2`)
      .flush({}, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(fixture.componentInstance.vehicules.map((v) => v.idVehicule)).toEqual([2]);
    expect(fixture.componentInstance.selection.has(2)).toBe(true);
    expect(fixture.nativeElement.querySelector('.veh-actions .message-erreur')?.textContent)
      .toContain('XYZ-789');
  });
});
