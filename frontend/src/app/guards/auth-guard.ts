import { CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = () => {

const user = JSON.parse(localStorage.getItem('user') || '{}');
return !!user.email;

  return false;
};



