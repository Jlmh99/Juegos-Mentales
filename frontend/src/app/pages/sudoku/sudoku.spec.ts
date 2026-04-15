import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sudoku } from './sudoku';

describe('Sudoku', () => {
  let component: Sudoku;
  let fixture: ComponentFixture<Sudoku>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sudoku]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sudoku);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
