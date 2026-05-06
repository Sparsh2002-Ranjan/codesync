import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models';

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
  avatarUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = `${environment.apiUrl}/auth`;
  private _currentUser = signal<User | null>(this.loadUserFromStorage());

  readonly currentUser = this._currentUser.asReadonly();

  constructor(private http: HttpClient, private router: Router) {}

  // ── Load saved user from localStorage on page refresh ──────
  private loadUserFromStorage(): User | null {
    try {
      const saved = localStorage.getItem('codesync_user');
      return saved ? JSON.parse(saved) : null;
    } catch { return null; }
  }

  private saveToStorage(token: string, user: User) {
    localStorage.setItem('codesync_token', token);
    localStorage.setItem('codesync_user', JSON.stringify(user));
  }

  private clearStorage() {
    localStorage.removeItem('codesync_token');
    localStorage.removeItem('codesync_user');
  }

  // ── Auth ────────────────────────────────────────────────────
  register(data: { username: string; email: string; password: string; fullName: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/register`, data).pipe(
      tap(res => this.handleAuthResponse(res))
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/login`, { email, password }).pipe(
      tap(res => this.handleAuthResponse(res))
    );
  }

  private handleAuthResponse(res: AuthResponse) {
    const user: User = {
      userId: res.userId,
      username: res.username,
      email: res.email,
      fullName: res.fullName,
      role: res.role as 'DEVELOPER' | 'ADMIN',
      avatarUrl: res.avatarUrl,
      isActive: true,
      provider: 'LOCAL',
      createdAt: new Date().toISOString()
    };
    this.saveToStorage(res.token, user);
    this._currentUser.set(user);
  }

  logout() {
    const token = localStorage.getItem('codesync_token');
    if (token) {
      this.http.post(`${this.api}/logout`, {}).subscribe({ error: () => {} });
    }
    this.clearStorage();
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  // ── Profile ──────────────────────────────────────────────────
  getProfile(userId: string): Observable<User> {
    return this.http.get<User>(`${this.api}/profile/${userId}`);
  }

  updateProfile(userId: string, updates: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.api}/profile/${userId}`, updates).pipe(
      tap(updated => {
        this._currentUser.set(updated);
        localStorage.setItem('codesync_user', JSON.stringify(updated));
      })
    );
  }

  changePassword(userId: string, currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.api}/password/${userId}`, { currentPassword, newPassword });
  }

  // ── User search ──────────────────────────────────────────────
  searchUsers(query: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.api}/search?query=${query}`);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.api}/users`);
  }

  // ── Admin ────────────────────────────────────────────────────
  suspendUser(userId: string): Observable<any> {
    return this.http.put(`${this.api}/deactivate/${userId}`, {});
  }

  activateUser(userId: string): Observable<any> {
    return this.http.put(`${this.api}/activate/${userId}`, {});
  }

  deleteUser(userId: string): Observable<any> {
    return this.http.delete(`${this.api}/users/${userId}`);
  }

  // ── Helpers ──────────────────────────────────────────────────
  get isLoggedIn(): boolean { return this._currentUser() !== null; }
  get isAdmin(): boolean { return this._currentUser()?.role === 'ADMIN'; }
  get token(): string | null { return localStorage.getItem('codesync_token'); }
}
