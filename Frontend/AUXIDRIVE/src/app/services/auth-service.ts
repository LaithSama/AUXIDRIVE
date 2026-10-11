import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthResponse } from '../core/interfaces/auth-response';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth/login';

  login(email: string, motDePasse: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, { email, motDePasse }).pipe(
      tap((response: AuthResponse) => {
        sessionStorage.setItem('token', response.token);
        sessionStorage.setItem('role', response.role);
        sessionStorage.setItem('email', email);
      })
    );
  }
  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  getRole(): string | null {
    return sessionStorage.getItem('role');
  }

  getEmail(): string | null {
    return sessionStorage.getItem('email');
  }

  logout() {
    sessionStorage.clear();
  }
}