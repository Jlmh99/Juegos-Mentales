import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ApiService } from './services/api';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
 //esto es para los link externos
  protected readonly title = signal('frontend');
  mensaje = signal('');   // 👈 usamos signal como Angular moderno

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.api.hola().subscribe(data => {
      this.mensaje.set(data);
    });
  }
 //para la busqueda interna
  searchText = signal('');

  juegos = signal([
    {
      nombre: 'Sudoku',
      descripcion: 'Juego lógico numérico que estimula la mente.',
      ruta: '#'
    },
    {
      nombre: 'Crucigrama',
      descripcion: 'Juego de palabras que mejora el vocabulario.',
      ruta: '#'
    }
  ]);

  juegosFiltrados = computed(() =>
    this.juegos().filter(juego =>
      juego.nombre.toLowerCase().includes(this.searchText().toLowerCase())
    )
  );
}

