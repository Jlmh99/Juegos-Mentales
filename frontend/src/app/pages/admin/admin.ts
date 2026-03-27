import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html'
})
export class Admin implements OnInit {

  usuarios: any[] = [];

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
      this.usuarios = data;
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
}