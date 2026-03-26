import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private api = 'http://localhost:8080/api/test';
  private urlRegister = 'http://localhost:8080/api/auth/register';

  constructor(private http: HttpClient) {}

  test():Observable<string> {
    // 1. Crea las credenciales (user:contraseña_de_consola)
    // Nota: Sustituye 'tu_password_aqui' por el que sale en tu consola de Spring
    const authHeader = 'Basic ' + btoa('user:5bf94bc5-175e-4783-b808-bd132bbcc69a');
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
}