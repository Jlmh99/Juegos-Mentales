import { Routes } from '@angular/router';
import { Home } from './pages/home/home';

export const routes: Routes = [
    { path: '', component: Home }, // Ruta inicial (vacía)
  // { path: 'login', component: Login }, <-- Ejemplo de futura página
  { path: '**', redirectTo: '' } // Redirige a home si la ruta no existe
];
