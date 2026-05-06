import { Component, inject, signal, HostListener, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification-execution.service';
import { CollabService } from '../../services/collab.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  auth         = inject(AuthService);
  notifService = inject(NotificationService);
  router       = inject(Router);
  collab       = inject(CollabService);

  showNotifPanel = signal(false);
  showUserMenu   = signal(false);
  showMobileMenu = signal(false);

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    const t = e.target as HTMLElement;
    if (!t.closest('.notif-trigger') && !t.closest('.notif-panel')) this.showNotifPanel.set(false);
    if (!t.closest('.user-trigger') && !t.closest('.user-menu')) this.showUserMenu.set(false);
  }

  ngOnInit() {
    const userId = this.auth.currentUser()?.userId;
    if (userId) {
      this.notifService.loadNotifications(userId);
      // Connect WebSocket for real-time invite notifications
      this.collab.connectNotifications(userId);
    }
  }

  get user() { return this.auth.currentUser(); }
  get unreadCount() { return this.notifService.unreadCount; }
  get notifications() { return this.notifService.all().slice(0, 5); }
  get pendingInvites() { return this.collab.pendingInvites(); }
  get pendingJoinRequests() { return this.collab.pendingJoinRequests(); }

  toggleNotif(e: MouseEvent) { e.stopPropagation(); this.showNotifPanel.update(v => !v); this.showUserMenu.set(false); }
  toggleUser(e: MouseEvent)  { e.stopPropagation(); this.showUserMenu.update(v => !v);  this.showNotifPanel.set(false); }

  markRead(id: string) { this.notifService.markRead(id).subscribe(() => this.notifService.markReadLocal(id)); }
  markAllRead() {
    const userId = this.auth.currentUser()?.userId;
    if (userId) this.notifService.markAllRead(userId).subscribe(() => this.notifService.markAllReadLocal());
  }

  acceptInvite(invite: { sessionId: string; projectId: string }) {
    this.collab.dismissInvite(invite.sessionId);
    this.router.navigate(['/editor'], {
      queryParams: { projectId: invite.projectId, sessionId: invite.sessionId }
    });
  }

  acceptInviteFromNotif(notif: any) {
    // Extract projectId from deepLinkUrl e.g. /editor?projectId=X&sessionId=Y
    const url = notif.deepLinkUrl || '';
    const projectId = url.split('projectId=')[1]?.split('&')[0] || '';
    const sessionId = notif.relatedId;
    this.markRead(notif.notificationId);
    this.router.navigate(['/editor'], { queryParams: { projectId, sessionId } });
  }

  acceptJoinRequest(req: { sessionId: string; projectId: string; requestingUserId: string; requestingUsername: string }) {
    this.collab.acceptJoinApi(req.sessionId, req.requestingUserId).subscribe({
      next: () => {
        // Remove from pending list
        this.collab.rejectJoinRequest(req.sessionId, req.requestingUserId);
        // Immediately refresh participant list so the count updates in the UI
        this.collab.refreshParticipants(req.sessionId);
      },
      error: (e) => console.error('Failed to accept join request', e)
    });
  }

  rejectJoinRequest(req: { sessionId: string; requestingUserId: string }) {
    this.collab.rejectJoinRequest(req.sessionId, req.requestingUserId);
  }

  dismissInvite(sessionId: string) { this.collab.dismissInvite(sessionId); }

  logout() { this.auth.logout(); }

  getInitials(name: string): string {
    return (name || 'U').split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getNotifIcon(type: string): string {
    const icons: Record<string, string> = {
      SESSION_INVITE: '🔗', COMMENT: '💬', MENTION: '@',
      SNAPSHOT: '📸', FORK: '🍴', BROADCAST: '📢'
    };
    return icons[type] || '🔔';
  }

  timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
  }
}
