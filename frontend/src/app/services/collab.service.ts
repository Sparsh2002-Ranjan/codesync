import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject, Observable } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';
import { Participant, CURSOR_COLORS } from '../models';

export interface CollabMessage {
  type: 'cursor' | 'edit' | 'join' | 'leave' | 'kicked' | 'session_ended' | 'invite' | 'join_request' | 'join_accepted';
  payload: any;
}

@Injectable({ providedIn: 'root' })
export class CollabService {
  private api = `${environment.apiUrl}/sessions`;
  private notifApi = `${environment.apiUrl}/notifications`;
  private stompClient: Client | null = null;

  private _participants  = signal<Participant[]>([]);
  private _isConnected   = signal(false);
  private _sessionId     = signal<string | null>(null);
  // Shared signal that editor reads directly — avoids ngModel DOM sync issue
  readonly remoteContent = signal<{ content: string; fileId: string; ts: number } | null>(null);

  readonly participants = this._participants.asReadonly();
  readonly isConnected  = this._isConnected.asReadonly();
  readonly sessionId    = this._sessionId.asReadonly();

  messages$ = new Subject<CollabMessage>();

  // Pending session invites for current user
  pendingInvites = signal<{ sessionId: string; projectName: string; fromUser: string; projectId: string }[]>([]);

  // Pending join requests for the HOST (users who arrived via direct link)
  pendingJoinRequests = signal<{ sessionId: string; projectId: string; requestingUserId: string; requestingUsername: string }[]>([]);

  constructor(private http: HttpClient) {}

  // ── REST calls ────────────────────────────────────────────────
  createSession(projectId: string, fileId: string, ownerId: string,
                language: string, maxParticipants = 10): Observable<any> {
    return this.http.post(this.api, {
      projectId, fileId, ownerId, language, maxParticipants,
      isPasswordProtected: false
    });
  }

  joinSessionApi(sessionId: string, userId: string, password = ''): Observable<Participant> {
    const url = `${this.api}/${sessionId}/join?userId=${userId}${password ? '&password=' + password : ''}`;
    return this.http.post<Participant>(url, {});
  }

  leaveSessionApi(sessionId: string, userId: string): Observable<any> {
    return this.http.post(`${this.api}/${sessionId}/leave?userId=${userId}`, {});
  }

  endSessionApi(sessionId: string): Observable<any> {
    return this.http.post(`${this.api}/${sessionId}/end`, {});
  }

  kickParticipantApi(sessionId: string, participantId: string): Observable<any> {
    return this.http.post(`${this.api}/${sessionId}/kick/${participantId}`, {});
  }

  getParticipants(sessionId: string): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.api}/${sessionId}/participants`);
  }

  getSessionById(sessionId: string): Observable<any> {
    return this.http.get(`${this.api}/${sessionId}`);
  }

  requestJoinApi(sessionId: string, userId: string, username: string): Observable<any> {
    return this.http.post(`${this.api}/${sessionId}/request-join?userId=${userId}&username=${encodeURIComponent(username)}`, {});
  }

  acceptJoinApi(sessionId: string, userId: string): Observable<Participant> {
    return this.http.post<Participant>(`${this.api}/${sessionId}/accept-join?userId=${userId}`, {});
  }

  rejectJoinRequest(sessionId: string, userId: string) {
    this.pendingJoinRequests.update(r => r.filter(x => !(x.sessionId === sessionId && x.requestingUserId === userId)));
  }

  // ── Send session invite notification ─────────────────────────
  sendInvite(recipientId: string, actorId: string, actorUsername: string,
             sessionId: string, projectId: string, projectName: string): Observable<any> {
    return this.http.post(this.notifApi, {
      recipientId,
      actorId,
      type: 'SESSION_INVITE',
      title: `${actorUsername} invited you to collaborate`,
      message: `Join live session on project "${projectName}"`,
      relatedId: sessionId,
      relatedType: 'session',
      deepLinkUrl: `/editor?projectId=${projectId}&sessionId=${sessionId}`
    });
  }

  // ── WebSocket connection ──────────────────────────────────────
  connectWebSocket(sessionId: string, userId: string, username: string) {
    // Disconnect any existing session WebSocket before reconnecting
    if (this.stompClient?.active) {
      this.stompClient.deactivate();
    }
    this._sessionId.set(sessionId);

    this.stompClient = new Client({
      // Use SockJS as transport — matches backend registerStompEndpoints("/ws").withSockJS()
      webSocketFactory: () => new SockJS(environment.wsUrl),

      onConnect: () => {
        this._isConnected.set(true);
        console.log('✅ WebSocket connected to session:', sessionId);
        // Shut down the standalone notification client if it was open
        if (this.notifClient?.active) {
          this.notifClient.deactivate();
          this.notifClient = null;
        }

        // ── Subscribe to code edits ───────────────────────────
        this.stompClient!.subscribe(
          `/topic/session/${sessionId}/edits`,
          (msg) => {
            const edit = JSON.parse(msg.body);
            if (edit.userId !== userId) {
              // Update shared signal directly — editor watches this
              this.remoteContent.set({
                content: edit.content,
                fileId: edit.fileId,
                ts: Date.now()
              });
              this.messages$.next({ type: 'edit', payload: edit });
            }
          }
        );

        // ── Subscribe to cursor updates ───────────────────────
        this.stompClient!.subscribe(
          `/topic/session/${sessionId}/cursors`,
          (msg) => {
            const cursor = JSON.parse(msg.body);
            if (cursor.userId !== userId) {
              this._participants.update(parts =>
                parts.map(p => p.userId === cursor.userId
                  ? { ...p, cursorLine: cursor.line, cursorCol: cursor.col }
                  : p
                )
              );
              this.messages$.next({ type: 'cursor', payload: cursor });
            }
          }
        );

        // ── Subscribe to session events ───────────────────────
        this.stompClient!.subscribe(
          `/topic/session/${sessionId}/events`,
          (msg) => {
            const event = JSON.parse(msg.body);
            if (event.type === 'PARTICIPANT_JOINED') {
              this.refreshParticipants(sessionId);
              this.messages$.next({ type: 'join', payload: event });
            } else if (event.type === 'PARTICIPANT_LEFT') {
              this._participants.update(p => p.filter(x => x.userId !== event.userId));
              this.messages$.next({ type: 'leave', payload: event });
            } else if (event.type === 'PARTICIPANT_KICKED') {
              this._participants.update(p => p.filter(x => x.participantId !== event.participantId));
              this.messages$.next({ type: 'kicked', payload: event });
            } else if (event.type === 'SESSION_ENDED') {
              this.disconnectWebSocket();
              this.messages$.next({ type: 'session_ended', payload: event });
            }
          }
        );

        // ── Subscribe to personal notifications (invites) ─────
        // Each user listens on their own userId topic
        this.stompClient!.subscribe(
          `/user/${userId}/queue/notifications`,
          (msg) => {
            try {
              const data = JSON.parse(msg.body);
              const notif = data.notification || data;
              if (notif.type === 'SESSION_INVITE') {
                this.pendingInvites.update(inv => [...inv, {
                  sessionId: notif.relatedId,
                  projectName: notif.message,
                  fromUser: notif.actorId,
                  projectId: notif.deepLinkUrl?.split('projectId=')[1]?.split('&')[0] || ''
                }]);
                this.messages$.next({ type: 'invite', payload: notif });
              } else if (notif.type === 'JOIN_REQUEST') {
                // Host receives a join request from someone who used the share link
                this.pendingJoinRequests.update(r => [...r, {
                  sessionId: notif.sessionId,
                  projectId: notif.projectId,
                  requestingUserId: notif.requestingUserId,
                  requestingUsername: notif.requestingUsername
                }]);
                this.messages$.next({ type: 'join_request', payload: notif });
              } else if (notif.type === 'JOIN_ACCEPTED') {
                // The requester was accepted — they should now join the session
                this.messages$.next({ type: 'join_accepted', payload: notif });
              }
            } catch (e) { console.error('Notif parse error', e); }
          }
        );

        this.refreshParticipants(sessionId);
      },

      onDisconnect: () => {
        this._isConnected.set(false);
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this._isConnected.set(false);
      },

      reconnectDelay: 3000,
    });

    this.stompClient.activate();
  }

  // Also connect for notifications even without a session (global user connection)
  // Stores a reference so we can reuse or disconnect it later
  private notifClient: Client | null = null;

  connectNotifications(userId: string) {
    // If already connected via session WebSocket, subscribe there instead
    if (this._isConnected() && this.stompClient?.active) {
      // The session WebSocket already has the notification subscription - no need for a second client
      return;
    }
    // If a notif client is already active, don't create another
    if (this.notifClient?.active) return;

    this.notifClient = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      onConnect: () => {
        this.notifClient!.subscribe(`/user/${userId}/queue/notifications`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            const notif = data.notification || data;
            if (notif.type === 'SESSION_INVITE') {
              this.pendingInvites.update(inv => [...inv, {
                sessionId: notif.relatedId,
                projectName: notif.message,
                fromUser: notif.actorId || notif.actorUsername || 'Someone',
                projectId: (notif.deepLinkUrl || '').split('projectId=')[1]?.split('&')[0] || ''
              }]);
              this.messages$.next({ type: 'invite', payload: notif });
            } else if (notif.type === 'JOIN_REQUEST') {
              this.pendingJoinRequests.update(r => [...r, {
                sessionId: notif.sessionId,
                projectId: notif.projectId,
                requestingUserId: notif.requestingUserId,
                requestingUsername: notif.requestingUsername
              }]);
              this.messages$.next({ type: 'join_request', payload: notif });
            } else if (notif.type === 'JOIN_ACCEPTED') {
              this.messages$.next({ type: 'join_accepted', payload: notif });
            }
          } catch (e) {}
        });
      },
      reconnectDelay: 5000,
    });
    this.notifClient.activate();
  }

  refreshParticipants(sessionId: string) {
    this.getParticipants(sessionId).subscribe(parts => {
      this._participants.set(
        parts.map((p, i) => ({
          ...p,
          color: p.color || CURSOR_COLORS[i % CURSOR_COLORS.length]
        }))
      );
    });
  }

  disconnectWebSocket() {
    if (this.stompClient?.active) this.stompClient.deactivate();
    this._isConnected.set(false);
    this._sessionId.set(null);
    this._participants.set([]);
    this.remoteContent.set(null);
  }

  // ── Send messages ─────────────────────────────────────────────
  sendCursorUpdate(sessionId: string, userId: string, username: string,
                   color: string, line: number, col: number) {
    if (!this.stompClient?.active) return;
    this.stompClient.publish({
      destination: `/app/session/cursor`,
      body: JSON.stringify({ sessionId, userId, username, color, line, col })
    });
  }

  sendEdit(sessionId: string, userId: string, fileId: string, content: string) {
    if (!this.stompClient?.active) return;
    this.stompClient.publish({
      destination: `/app/session/edit`,
      body: JSON.stringify({ sessionId, userId, fileId, content, timestamp: Date.now() })
    });
  }

  kickParticipant(participantId: string) {
    this._participants.update(p => p.filter(x => x.participantId !== participantId));
  }

  dismissInvite(sessionId: string) {
    this.pendingInvites.update(inv => inv.filter(i => i.sessionId !== sessionId));
  }

  // ── Full join flow ────────────────────────────────────────────
  joinSession(sessionId: string, userId: string, username: string, password = '') {
    this.joinSessionApi(sessionId, userId, password).subscribe({
      next: () => this.connectWebSocket(sessionId, userId, username),
      error: (err) => console.error('Failed to join session:', err)
    });
  }

  leaveSession(sessionId: string, userId: string) {
    this.leaveSessionApi(sessionId, userId).subscribe();
    this.disconnectWebSocket();
  }
}
