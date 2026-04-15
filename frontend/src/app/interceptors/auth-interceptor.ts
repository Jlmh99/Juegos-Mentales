import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const router = inject(Router); // 👈 Inyectamos el Router para poder navegar

  let request = req;

  // 1. Si hay token, clonamos la petición
  if (token) {
    request = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // 2. Manejamos la respuesta y capturamos errores
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      
      if (error.status === 401) {
        // Caso: Token expirado o no enviado
        console.error('Sesión expirada o no autorizada');
        localStorage.clear(); // Limpiamos todo
        router.navigate(['/login']);
      } 
      
      else if (error.status === 403) {
        // Caso: El token es válido pero el usuario no es ADMIN
        console.error('Acceso prohibido: No tienes permisos de administrador');
        alert('No tienes permisos para acceder a este recurso.');
        router.navigate(['/home']); // Lo mandamos a un lugar seguro
      }

      // Devolvemos el error para que el componente también sepa que falló si es necesario
      return throwError(() => error);
    })
  );
};