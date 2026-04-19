import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private api = 'https://juegos-mentales-1jma.onrender.com/api/test';
  private urlRegister = 'https://juegos-mentales-1jma.onrender.com/api/auth/register';
  private urlLogin = 'https://juegos-mentales-1jma.onrender.com/api/auth/login';
  private urlVerify = 'https://juegos-mentales-1jma.onrender.com/api/auth/verify';

  constructor(private http: HttpClient) {}

  test():Observable<string> {
    // 1. Crea las credenciales (user:contraseña_de_consola)
    // Nota: Sustituye 'tu_password_aqui' por el que sale en tu consola de Spring
    const authHeader = 'Basic ' + btoa('user:admin321');//modifique spring para que esta sea la contra
    const headers = new HttpHeaders({
      'Authorization': authHeader
    });

    return this.http.get(this.api, { headers, responseType: 'text' });
  }
  // Método para registrar nuevos alumnos
  register(data: any): Observable<string> {
    return this.http.post(this.urlRegister, data, {
      responseType: 'text' // 👈 Vital porque el backend responde con un String simple
    });
  }

  // Método de Login (Ahora igual que register)
  login(data: any): Observable<any> {
    return this.http.post(this.urlLogin, data);
  }

  verify(data: any): Observable<any> {
    return this.http.post(this.urlVerify, data, {
      
    });
  }

  getUsers() {
    return this.http.get<any[]>('https://juegos-mentales-1jma.onrender.com/api/users');
  }

  deleteUser(id: number) {
    return this.http.delete(`https://juegos-mentales-1jma.onrender.com/api/users/${id}`, { responseType: 'text' });
  }

  createUser(user: any) {
    return this.http.post('https://juegos-mentales-1jma.onrender.com/api/users', user);
  }

  updateUser(id: number, user: any) {
    return this.http.put(`https://juegos-mentales-1jma.onrender.com/api/users/${id}`, user);
  }

}