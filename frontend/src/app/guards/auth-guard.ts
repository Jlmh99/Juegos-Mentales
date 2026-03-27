import { CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = () => {

  const user = localStorage.getItem('user');

  if (user) return true;

  return false;
};


export const adminGuard: CanActivateFn = () => {

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  return user.rol === 'ADMIN';
};
