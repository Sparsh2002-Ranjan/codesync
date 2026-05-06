import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private api = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<any> {
    return this.http.get(`${this.api}/dashboard`);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/users`);
  }

  suspendUser(userId: string): Observable<any> {
    return this.http.put(`${this.api}/users/${userId}/suspend`, {});
  }

  reactivateUser(userId: string): Observable<any> {
    return this.http.put(`${this.api}/users/${userId}/reactivate`, {});
  }

  deleteUser(userId: string): Observable<any> {
    return this.http.delete(`${this.api}/users/${userId}`);
  }

  getActiveSessions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/sessions`);
  }

  terminateSession(sessionId: string): Observable<any> {
    return this.http.post(`${this.api}/sessions/${sessionId}/terminate`, {});
  }

  broadcast(title: string, message: string, recipientIds?: string[]): Observable<any> {
    return this.http.post(`${this.api}/notifications/broadcast`, {
      title, message, recipientIds: recipientIds || [], actorId: 'admin'
    });
  }
}
