import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule,CommonModule,],
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

  constructor(private auth: AuthService) {}

  registrar() {
    this.auth.register(this.user).subscribe(res => {
      this.mensaje = res;
    });
  }
}
