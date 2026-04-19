import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private url = 'https://juegos-mentales-1jma.onrender.com/api';

  constructor(private http: HttpClient) {}

  hola() {
    return this.http.get(this.url + '/hola', { responseType: 'text' });
  }
}

