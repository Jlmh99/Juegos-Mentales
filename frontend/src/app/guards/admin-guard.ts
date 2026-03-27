import { CanActivateFn } from '@angular/router';

export const adminGuard: CanActivateFn = () => {

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  return user.rol === 'ADMIN';
};
