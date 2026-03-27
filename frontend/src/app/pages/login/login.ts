import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule,CommonModule],
  templateUrl: './login.html'
})
export class Login {

  user = {
    email: '',
    password: ''
  };

  mensaje = '';
  codigo = '';
  mostrar2FA = false;

  constructor(private auth: AuthService, private router: Router) {}

  iniciarSesion() {
    this.auth.login(this.user).subscribe(res => {

      this.mensaje = res;

      if (res === '2FA requerido') {
      this.mostrar2FA = true;
    }

      if (res === 'Login correcto') {
        localStorage.setItem('user', JSON.stringify(res));

        const user = JSON.parse(localStorage.getItem('user') || '{}');
        console.log(user.rol);
        this.router.navigate(['/']);
      }
    });
  }

  verificarCodigo() {
    this.auth.verify({
      email: this.user.email,
      codigo: this.codigo
    }).subscribe(res => {

      this.mensaje = res;

      if (res === 'Login correcto') {
        this.router.navigate(['/']);
      }
    });
  }
}
