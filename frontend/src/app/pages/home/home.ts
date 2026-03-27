import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal } from '@angular/core';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
//esto es para los link externos
  protected readonly title = signal('frontend');
  
  mensajeBackend = signal(''); // Señal para guardar la respuesta del backend

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {

    //este es para el mensajeBackend
    this.auth.test().subscribe(res => {
    this.mensajeBackend.set(res);
    });

    this.auth.test().subscribe({
    next: res => this.mensajeBackend.set(res),
    error: err => console.error("Error detallado:", err)
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

  esAdmin() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.rol === 'ADMIN';
}
}
