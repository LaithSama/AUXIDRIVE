import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
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
});
