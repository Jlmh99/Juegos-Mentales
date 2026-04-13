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
  // 1. Añadimos <any> aquí para que TypeScript no se queje del .mensaje
  this.auth.login(this.user).subscribe((res: any) => {

    console.log('--- Datos recibidos ---');
    console.log('Tipo:', typeof res);
    console.log('Contenido:', res);

    // 2. Guardamos el mensaje (si es objeto usa .mensaje, si es string usa res)
    this.mensaje = res.mensaje || res;

    // 3. Verificamos 2FA (Checa ambas posibilidades: objeto o string)
    if (res === '2FA requerido' || res.mensaje === '2FA requerido') {
      console.log('Entrando a modo 2FA');
      this.mostrar2FA = true;
    }

    // 4. Verificamos Login (Checa ambas posibilidades)
    if (res === 'Login correcto' || res.mensaje === 'Login correcto') {
      console.log('Login exitoso. Guardando usuario...');
      
      // Guardamos la respuesta completa en el storage
      localStorage.setItem('user', JSON.stringify(res));

      // 5. Verificamos el rol en la consola
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      console.log('Rol en Storage:', user.rol);

      // 6. Navegamos al home
      this.router.navigate(['/home']);
    }
  }, error => {
    console.error('Error en login:', error);
    this.mensaje = 'Error de conexión';
  });
}

  verificarCodigo() {
  this.auth.verify({
    email: this.user.email,
    codigo: this.codigo
  }).subscribe((res: any) => {
    
    // 1. Verificamos el mensaje dentro del objeto
    if (res.mensaje === 'Login correcto') {
      
      // 2. ¡CRUCIAL! Guardar en localStorage para que el Guard te deje pasar
      // Si res es string, creamos el objeto manual; si es objeto, lo guardamos tal cual
      localStorage.setItem('user', JSON.stringify(res));
      console.log('Usuario guardado:', res);

      // 3. Ahora sí, el Guard verá los datos y te dejará entrar
      this.router.navigate(['/home']);
    } else {
      this.mensaje = res.mensaje;
    }
  });
}
}
