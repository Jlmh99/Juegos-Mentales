import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin implements OnInit {
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  mostrarModal = false;
  usuarioAEliminar: any = null;

  usuarioEditando: any = null;
  mostrarModalEditar = false;

  usuarios = signal<any[]>([]);
  usuarioActual = JSON.parse(localStorage.getItem('user') || '{}');

  nuevoUsuario = {
    username: '',
    email: '',
    password: '',
    rol: 'USER'
  };

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.cargarUsuarios();
  }

  cargarUsuarios() {
    this.auth.getUsers().subscribe(data => {
      this.usuarios.set(data);
    });
  }

  eliminar(id: number) {
    this.auth.deleteUser(id).subscribe(() => {
      this.cargarUsuarios();
    });
  }

  crear() {
    this.auth.createUser(this.nuevoUsuario).subscribe(() => {
      this.cargarUsuarios();
      this.nuevoUsuario = { username: '', email: '', password: '', rol: 'USER' };
    });
  }

  abrirModalEditar(user: any) {
    this.usuarioEditando = { ...user }; // copia
    this.mostrarModalEditar = true;
  }

  guardarCambios() {
    // 1. Creamos una copia para no afectar el objeto original del formulario
    const userEnviar = { ...this.usuarioEditando };

    // 2. Limpieza de datos: si el campo password está vacío, lo eliminamos
    // Esto evita enviar una cadena vacía al servidor y que se guarde mal
    if (!userEnviar.password || userEnviar.password.trim() === '') {
      delete userEnviar.password;
    }

    // 3. Enviamos la copia (userEnviar) en lugar del objeto original
    this.auth.updateUser(userEnviar.id, userEnviar)
      .subscribe({
        next: () => {
          this.cargarUsuarios();
          this.cerrarModalEditar();
        },
        error: (err) => {
          console.error('Error al actualizar el usuario:', err);
          // Aquí podrías añadir una alerta para el usuario
        }
      });
  }

  cerrarModalEditar() {
    this.mostrarModalEditar = false;
    this.usuarioEditando = null;
  }

  abrirModalEliminar(user: any) {
    this.usuarioAEliminar = user;
    this.mostrarModal = true;
  }

  confirmarEliminar() {
    if (this.usuarioAEliminar) {
      this.eliminar(this.usuarioAEliminar.id);
    }
    this.cerrarModal();
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.usuarioAEliminar = null;
  }

  esUsuarioActual(user: any): boolean {
    return user.id === this.usuarioActual.id;
  }

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