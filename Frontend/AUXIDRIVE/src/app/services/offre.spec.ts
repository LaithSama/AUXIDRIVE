import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { OffreService } from './offre';

describe('OffreService', () => {
  let service: OffreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
    service = TestBed.inject(OffreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
