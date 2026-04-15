import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const token = localStorage.getItem('token');
  const router = inject(Router); // Necesitas importar inject y Router

  if (token) {
    return true;
  }

  router.navigate(['/login']); // Si no hay token, mándalo al login
  return false;
};



