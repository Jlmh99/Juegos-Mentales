import { CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = () => {

  const user = localStorage.getItem('user');

  if (user) return true;

  return false;
};



