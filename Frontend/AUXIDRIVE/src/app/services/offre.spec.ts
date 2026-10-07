import { TestBed } from '@angular/core/testing';

import { Offre } from './offre';

describe('Offre', () => {
  let service: Offre;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Offre);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
