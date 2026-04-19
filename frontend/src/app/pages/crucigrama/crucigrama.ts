import { CommonModule } from '@angular/common';
import { Component, computed, OnDestroy, OnInit, signal } from '@angular/core'; // Añadido OnDestroy
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-crucigrama',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crucigrama.html',
  styleUrl: './crucigrama.css'
})
export class Crucigrama implements OnInit, OnDestroy {

  constructor(private router: Router) {}

  // Definimos las palabras: 
  // CASA (Horizontal en fila 0)
  // CLIMA (Vertical en columna 0)
  // IRIS (Horizontal en fila 2)
  
  readonly tableroInicial = [
    ['C', '', '', ''], // CASA (H)
    ['', null, null, null], // L (de CLIMA)
    ['', '', '', ''], // AMOR (H) e I (de CLIMA)
    ['', null, null, null], // M (de CLIMA)
    ['', null, null, null]  // A (de CLIMA)
  ];

  // IMPORTANTE: Crear una copia real por cada fila para evitar que se dupliquen letras
  tablero = signal<any[][]>(this.generarCopiaTablero());

  soluciones = [
    { 
      palabra: 'CASA', 
      posiciones: [[0,0],[0,1],[0,2],[0,3]], 
      pista: 'Lugar donde vives con tu familia.',
      orientacion: 'H' 
    },
    { 
      palabra: 'CLIMA', 
      posiciones: [[0,0],[1,0],[2,0],[3,0],[4,0]], 
      pista: 'Estado del tiempo (calor, frío, lluvia).',
      orientacion: 'V' 
    },
    { 
      palabra: 'IRIS', 
      posiciones: [[2,0],[2,1],[2,2],[2,3]], 
      pista: 'Parte coloreada del ojo o arco...',
      orientacion: 'H' 
    }
  ];

  pistasHorizontales = computed(() => this.soluciones.filter(s => s.orientacion === 'H'));
  pistasVerticales = computed(() => this.soluciones.filter(s => s.orientacion === 'V'));

  mensaje = signal('');
  completado = signal(false);
  tiempo = signal(0);
  intervalo: any;

  ngOnInit() { this.iniciarTimer(); }
  ngOnDestroy() { this.detenerTimer(); }

  // Función para evitar el error de referencia compartida
  generarCopiaTablero() {
    return this.tableroInicial.map(fila => [...fila]);
  }

  iniciarTimer() {
    this.detenerTimer();
    this.intervalo = setInterval(() => this.tiempo.update(t => t + 1), 1000);
  }

  detenerTimer() { if (this.intervalo) clearInterval(this.intervalo); }

  esError(i: number, j: number): boolean {
    const letra = this.tablero()[i][j]?.toUpperCase();
    if (!letra) return false;

    const sols = this.soluciones.filter(s => s.posiciones.some(p => p[0] === i && p[1] === j));
    for (let s of sols) {
      const idx = s.posiciones.findIndex(p => p[0] === i && p[1] === j);
      if (letra !== s.palabra[idx]) return true;
    }
    return false;
  }

  verificar() {
    let todoOk = true;
    let vacio = false;

    for (let s of this.soluciones) {
      const palabraUser = s.posiciones.map(([r, c]) => this.tablero()[r][c]).join('').toUpperCase();
      if (palabraUser.length < s.palabra.length) vacio = true;
      if (palabraUser !== s.palabra) todoOk = false;
    }

    if (todoOk) {
      this.mensaje.set('🎉 ¡Completado con éxito!');
      this.completado.set(true);
      this.detenerTimer();
    } else {
      this.mensaje.set(vacio ? '⚠️ Aún faltan letras' : '❌ Revisa las letras en rojo');
    }
  }

  formatoTiempo = computed(() => {
    const t = this.tiempo();
    const min = Math.floor(t / 60);
    const sec = t % 60;
    return `${min}:${sec.toString().padStart(2, '0')}`;
  });

  generarNuevo() {
    this.tablero.set(this.generarCopiaTablero());
    this.mensaje.set('');
    this.completado.set(false);
    this.tiempo.set(0);
    this.iniciarTimer();
  }

  volver() { this.router.navigate(['/home']); }
}