import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private url = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  hola() {
    return this.http.get(this.url + '/hola', { responseType: 'text' });
  }
}

