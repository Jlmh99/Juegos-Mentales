import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html'
})
export class Login {

  user = {
    email: '',
    password: ''
  };

  mensaje = '';

  constructor(private auth: AuthService, private router: Router) {}

  iniciarSesion() {
    this.auth.login(this.user).subscribe(res => {

      this.mensaje = res;

      if (res === 'Login correcto') {
        this.router.navigate(['/']); // redirige al home
      }
    });
  }
}
