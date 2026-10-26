import { provideHttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Dashboard } from './dashboard';

@Component({ template: '' })
class RouteStub {}

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideRouter([{ path: '**', component: RouteStub }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('ouvre et ferme le menu mobile', () => {
    fixture.detectChanges();
    const bouton: HTMLButtonElement = fixture.nativeElement.querySelector('.menu-bouton');
    const navigation: HTMLElement = fixture.nativeElement.querySelector('#sidebarMenu');
    expect(bouton.classList.contains('d-lg-none')).toBe(true);
    expect(navigation.classList.contains('col-lg-2')).toBe(true);
    expect(fixture.nativeElement.querySelector('.dashboard-contenu').classList.contains('col-lg-10')).toBe(true);

    expect(bouton.getAttribute('aria-expanded')).toBe('false');
    bouton.click();
    fixture.detectChanges();
    expect(bouton.getAttribute('aria-expanded')).toBe('true');
    expect(navigation.classList.contains('sidebar-ouverte')).toBe(true);

    navigation.querySelector<HTMLAnchorElement>('a.sidebar-lien')!.click();
    fixture.detectChanges();
    expect(bouton.getAttribute('aria-expanded')).toBe('false');
    expect(navigation.classList.contains('sidebar-ouverte')).toBe(false);
  });
});
