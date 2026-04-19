import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule,CommonModule,RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  user = {
    username: '',
    email: '',
    password: ''
  };

  mensaje = '';

  confirmPassword = '';
  mostrarPassword = false;

  togglePassword() {
    this.mostrarPassword = !this.mostrarPassword;
  }

  constructor(private auth: AuthService,private cd: ChangeDetectorRef) {}

  registrar() {

    if (!this.user.username || !this.user.email || !this.user.password || !this.confirmPassword) {
      this.mensaje = '❌ Todos los campos son obligatorios';
      return;
    }

    if (this.user.password.length < 6) {
      this.mensaje = '❌ La contraseña debe tener al menos 6 caracteres';
      return;
    }

    if (this.user.password !== this.confirmPassword) {
      this.mensaje = '❌ Las contraseñas no coinciden';
      return;
    }

    this.auth.register(this.user).subscribe(res => {
      this.mensaje = res;

     this.cd.detectChanges(); // 🔥 CLAVE

      if (res.includes('correctamente')) {
        this.user = {
          username: '',
          email: '',
          password: ''
        };
        this.confirmPassword = '';
      }

    }, () => {
      this.mensaje = '❌ Error en el servidor';
    });
  }
}
