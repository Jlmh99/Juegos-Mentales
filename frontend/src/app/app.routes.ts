import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin-guard';
import { authGuard } from './guards/auth-guard';
import { Admin } from './pages/admin/admin'; // El componente que quieres proteger
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';

export const routes: Routes = [
    // 1. Primero las rutas específicas
    { path: 'home', component: Home, 
    canActivate: [authGuard] },
    { path: 'register', component: Register },
    { path: 'login', component: Login },
    
    // 2. Ruta por defecto (cuando la URL está vacía)
    { path: '', redirectTo: 'login', pathMatch: 'full' },

    {
    path: 'admin',component: Admin,
    canActivate: [adminGuard]
    },
    
    // 3. EL COMODÍN SIEMPRE AL FINAL
    { path: '**', redirectTo: 'home' }
    
];
