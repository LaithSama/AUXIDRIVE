import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcceuilClient } from './acceuil-client';

describe('AcceuilClient', () => {
  let component: AcceuilClient;
  let fixture: ComponentFixture<AcceuilClient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AcceuilClient],
    }).compileComponents();

    fixture = TestBed.createComponent(AcceuilClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
