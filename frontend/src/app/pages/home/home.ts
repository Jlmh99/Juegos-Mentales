import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, computed, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
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

  esAdmin(): boolean {

    // Solo ejecutamos si estamos en el navegador
    if (isPlatformBrowser(this.platformId)) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      return user.rol === 'ADMIN';
    }
    
    return false; // Si está en el servidor, por defecto no es admin
  }

  logout() {
  if (isPlatformBrowser(this.platformId)) {
    localStorage.removeItem('user'); // Borramos los datos
    this.router.navigate(['/login']); // Directo al login
  }
}
}
