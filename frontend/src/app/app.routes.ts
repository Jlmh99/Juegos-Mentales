import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin-guard';
import { authGuard } from './guards/auth-guard';
import { Admin } from './pages/admin/admin'; // El componente que quieres proteger
import { Crucigrama } from './pages/crucigrama/crucigrama';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Sudoku } from './pages/sudoku/sudoku';

export const routes: Routes = [
    // 1. Rutas Públicas
    { path: 'login', component: Login },
    { path: 'register', component: Register },
    { path: 'sudoku', component: Sudoku },
    { path: 'crucigrama', component: Crucigrama },

    // 2. Rutas Protegidas (Requieren Login)
    { 
        path: 'home', 
        component: Home, 
        canActivate: [authGuard] 
    },

    // 3. Rutas de Administración (Requieren Rol ADMIN)
    {
        path: 'admin', 
        component: Admin,
        canActivate: [authGuard, adminGuard] // 🔥 IMPORTANTE: Pon ambos. 
    },
    
    // 4. Redirección inicial
    { path: '', redirectTo: 'login', pathMatch: 'full' },

    // 5. EL COMODÍN SIEMPRE AL FINAL
    { path: '**', redirectTo: 'home' }
];
