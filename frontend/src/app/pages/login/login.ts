import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule,CommonModule,RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  user = {
    email: '',
    password: ''
  };

  mensaje = '';
  codigo = '';
  mostrar2FA = false;

  constructor(private auth: AuthService, private router: Router,
  private cd: ChangeDetectorRef) {}

  iniciarSesion() {
  // Añadimos <any> aquí para que TypeScript no se queje del .mensaje
  this.auth.login(this.user).subscribe((res: any) => {

    console.log('--- Datos recibidos ---');
    console.log('Tipo:', typeof res);
    console.log('Contenido:', res);

    // Guardamos el mensaje (si es objeto usa .mensaje, si es string usa res)
    this.mensaje = res.mensaje || res;

    // 1. Verificamos si requiere 2FA
      if (res === '2FA requerido' || res.mensaje === '2FA requerido') {
        this.mostrar2FA = true;

        this.cd.detectChanges(); // 🔥 CLAVE

        return;
      }

    // 4. Verificamos Login (Checa ambas posibilidades)
    if (res === 'Login correcto' || res.mensaje === 'Login correcto') {
      console.log('Login exitoso. Guardando usuario...');
      
     // 🔐 IMPLEMENTACIÓN DEL TOKEN Y ROL
        // Si res es objeto, extraemos datos. Si no, usamos valores por defecto.
        if (typeof res === 'object') {
          localStorage.setItem('token', res.token);
          localStorage.setItem('rol', res.rol);
          localStorage.setItem('user', JSON.stringify(res)); // Guardamos todo por si acaso
        }

        console.log('Token y Rol guardados correctamente');
        this.router.navigate(['/home']);
        this.cd.detectChanges();
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
        // 🔐 IMPLEMENTACIÓN DEL TOKEN Y ROL TRAS 2FA
          localStorage.setItem('token', res.token);
          localStorage.setItem('rol', res.rol);
          localStorage.setItem('user', JSON.stringify(res));
          console.log('Usuario guardado:', res);
          console.log('Sesión iniciada con 2FA. Token guardado.');
        // 3. Ahora sí, el Guard verá los datos y te dejará entrar
        this.router.navigate(['/home']);
        this.cd.detectChanges(); // 🔥 también aquí
        } else {
        this.mensaje = res.mensaje || 'Código inválido';
      }
    });
  }
}
