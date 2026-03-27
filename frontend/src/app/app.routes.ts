import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard'; // El guard que acabas de generar
import { Admin } from './pages/admin/admin'; // El componente que quieres proteger
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

    {
    path: 'admin',component: Admin,
    canActivate: [authGuard]
    },
    
    // 3. EL COMODÍN SIEMPRE AL FINAL
    { path: '**', redirectTo: 'home' }
    
];
