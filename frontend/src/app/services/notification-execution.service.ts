import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Notification } from '../models';

// ── NOTIFICATION SERVICE ─────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private api = `${environment.apiUrl}/notifications`;
  private _notifications = signal<Notification[]>([]);

  constructor(private http: HttpClient) {}

  loadNotifications(userId: string) {
    this.http.get<Notification[]>(`${this.api}/recipient/${userId}`)
      .subscribe({ next: data => this._notifications.set(data), error: () => {} });
  }

  get all() { return this._notifications.asReadonly(); }
  get unreadCount(): number { return this._notifications().filter(n => !n.isRead).length; }

  markRead(id: string): Observable<any> {
    return this.http.put(`${this.api}/${id}/read`, {});
  }
  markAllRead(userId: string): Observable<any> {
    return this.http.put(`${this.api}/recipient/${userId}/read-all`, {});
  }
  deleteRead(userId: string): Observable<any> {
    return this.http.delete(`${this.api}/recipient/${userId}/read`);
  }
  getUnreadCount(userId: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.api}/recipient/${userId}/unread-count`);
  }
  markReadLocal(id: string) {
    this._notifications.update(ns =>
      ns.map(n => n.notificationId === id ? { ...n, isRead: true } : n)
    );
  }
  markAllReadLocal() {
    this._notifications.update(ns => ns.map(n => ({ ...n, isRead: true })));
  }
}
