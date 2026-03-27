import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';

export const routes: Routes = [
    // 1. Primero las rutas específicas
    { path: 'home', component: Home },
    { path: 'register', component: Register },
    { path: 'login', component: Login },
    
    // 2. Ruta por defecto (cuando la URL está vacía)
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    
    // 3. EL COMODÍN SIEMPRE AL FINAL
    { path: '**', redirectTo: 'home' }
    
];
