import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const rol = localStorage.getItem('rol'); // Es más directo que parsear el JSON

  if (rol === 'ADMIN') {
    return true;
  }

  // Si es un USER queriendo entrar a Admin, lo mandamos al home
  console.warn('Acceso denegado: No es ADMIN');
  router.navigate(['/home']); 
  return false;
};
