import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Crucigrama } from './crucigrama';

describe('Crucigrama', () => {
  let component: Crucigrama;
  let fixture: ComponentFixture<Crucigrama>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Crucigrama]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Crucigrama);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
