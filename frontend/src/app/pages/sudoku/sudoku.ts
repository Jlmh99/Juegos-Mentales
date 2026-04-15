import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sudoku',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sudoku.html',
  styleUrl: './sudoku.css'
})
export class Sudoku {

  constructor(private router: Router) {
    this.generarNuevo();
  }

  initialGrid!: number[][];
  grid!: number[][];
  mensaje = '';
  tiempo = 0;
  intervalo: any;

  iniciarTimer() {
    this.intervalo = setInterval(() => {
      this.tiempo++;
    }, 1000);
  }

  detenerTimer() {
    clearInterval(this.intervalo);
  }

  resetearTimer() {
    this.detenerTimer();
    this.tiempo = 0;
    this.iniciarTimer();
  }

  // 🎲 TABLEROS PREDEFINIDOS (aleatorio)
  puzzles = [
    [
      [5,3,0,0,7,0,0,0,0],
      [6,0,0,1,9,5,0,0,0],
      [0,9,8,0,0,0,0,6,0],
      [8,0,0,0,6,0,0,0,3],
      [4,0,0,8,0,3,0,0,1],
      [7,0,0,0,2,0,0,0,6],
      [0,6,0,0,0,0,2,8,0],
      [0,0,0,4,1,9,0,0,5],
      [0,0,0,0,8,0,0,7,9]
    ],
    [
      [0,0,0,2,6,0,7,0,1],
      [6,8,0,0,7,0,0,9,0],
      [1,9,0,0,0,4,5,0,0],
      [8,2,0,1,0,0,0,4,0],
      [0,0,4,6,0,2,9,0,0],
      [0,5,0,0,0,3,0,2,8],
      [0,0,9,3,0,0,0,7,4],
      [0,4,0,0,5,0,0,3,6],
      [7,0,3,0,1,8,0,0,0]
    ]
  ];

  generarNuevo() {
    const random = Math.floor(Math.random() * this.puzzles.length);
    this.initialGrid = JSON.parse(JSON.stringify(this.puzzles[random]));
    this.grid = JSON.parse(JSON.stringify(this.initialGrid));
    this.mensaje = '';

    this.resetearTimer(); // 🔥 iniciar tiempo
  }

  // 🔒 celda fija
  esFija(i: number, j: number): boolean {
    return this.initialGrid[i][j] !== 0;
  }

  // ❌ detectar error en tiempo real
  esError(i: number, j: number): boolean {
    const val = this.grid[i][j];
    if (val === 0) return false;

    // fila
    for (let x = 0; x < 9; x++) {
      if (x !== j && this.grid[i][x] === val) return true;
    }

    // columna
    for (let x = 0; x < 9; x++) {
      if (x !== i && this.grid[x][j] === val) return true;
    }

    // bloque 3x3
    const startRow = Math.floor(i / 3) * 3;
    const startCol = Math.floor(j / 3) * 3;

    for (let x = 0; x < 3; x++) {
      for (let y = 0; y < 3; y++) {
        const r = startRow + x;
        const c = startCol + y;

        if ((r !== i || c !== j) && this.grid[r][c] === val) {
          return true;
        }
      }
    }

    return false;
  }

  // ✔ verificar completo
  verificar() {

    for (let row of this.grid) {
      if (row.includes(0)) {
        this.mensaje = '⚠️ Hay espacios vacíos';
        return;
      }
    }

    for (let i = 0; i < 9; i++) {
      for (let j = 0; j < 9; j++) {
        if (this.esError(i, j)) {
          this.mensaje = '❌ Hay errores en el tablero';
          return;
        }
      }
    }

    this.mensaje = '🎉 ¡Correcto!';
    this.detenerTimer(); // 🛑 detener tiempo
  }

  reiniciar() {
    this.grid = JSON.parse(JSON.stringify(this.initialGrid));
    this.mensaje = '';
    this.resetearTimer();
  }

  formatoTiempo(): string {
    const min = Math.floor(this.tiempo / 60);
    const seg = this.tiempo % 60;

    return `${min}:${seg < 10 ? '0' : ''}${seg}`;
  }

  volver() {
    this.router.navigate(['/home']); // 🔥 SIEMPRE HOME
  }
}
