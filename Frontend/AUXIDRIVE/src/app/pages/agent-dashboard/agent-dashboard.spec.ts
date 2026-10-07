import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentDashboard } from './agent-dashboard';

describe('AgentDashboard', () => {
  let component: AgentDashboard;
  let fixture: ComponentFixture<AgentDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgentDashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(AgentDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
