import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { ProjectService } from '../services/project.service';
import { NotificationService } from '../services/notification-execution.service';
import { AdminService } from '../services/admin.service';

// ── PROFILE ──────────────────────────────────────────────────
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
<div class="page-content">
  <div class="container" style="padding-top:40px;padding-bottom:60px;max-width:900px;">
    @if (user) {
      <div class="profile-hero">
        <div class="avatar avatar-xl">{{ initials }}</div>
        <div class="profile-info">
          <h1 style="font-size:24px;margin-bottom:4px;">{{ user.fullName }}</h1>
          <div class="mono text-accent" style="font-size:14px;margin-bottom:8px;">&#64;{{ user.username }}</div>
          <p class="text-secondary">{{ user.bio || 'No bio yet.' }}</p>
          <div class="flex gap-3 items-center" style="margin-top:12px;flex-wrap:wrap;">
            <span class="badge badge-accent">{{ user.role }}</span>
            <span class="badge badge-green">{{ user.provider }}</span>
          </div>
        </div>
        <button class="btn btn-ghost btn-sm" (click)="editing.set(!editing())">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          Edit Profile
        </button>
      </div>

      @if (editing()) {
        <div class="card" style="margin-top:24px;max-width:500px;">
          <h3 style="margin-bottom:16px;font-size:15px;">Edit Profile</h3>
          <div style="display:flex;flex-direction:column;gap:14px;">
            <div class="form-group"><label>Full Name</label><input class="input" [(ngModel)]="editName"></div>
            <div class="form-group"><label>Bio</label><input class="input" [(ngModel)]="editBio" placeholder="Tell people about yourself"></div>
            <div class="flex gap-2">
              <button class="btn btn-primary btn-sm" (click)="save()" [disabled]="saving()">
                @if (saving()) { <span class="spinner"></span> } @else { Save Changes }
              </button>
              <button class="btn btn-ghost btn-sm" (click)="editing.set(false)">Cancel</button>
            </div>
          </div>
        </div>
      }

      <div style="margin-top:32px;">
        <div class="section-label" style="margin-bottom:16px;">My Projects ({{ myProjects().length }})</div>
        @if (loadingProjects()) {
          <div class="flex items-center gap-2 text-muted" style="font-size:13px;"><span class="spinner"></span> Loading...</div>
        }
        <div style="display:flex;flex-direction:column;gap:8px;">
          @for (p of myProjects(); track p.projectId) {
            <a [routerLink]="['/project', p.projectId]" class="profile-proj-row">
              <span class="mono" style="font-weight:600;font-size:13px;">{{ p.name }}</span>
              <span class="badge {{ getLangClass(p.language) }}" style="font-size:10px;">{{ p.language }}</span>
              <span class="text-muted" style="font-size:12px;margin-left:auto;">⭐ {{ p.starCount }}</span>
            </a>
          }
        </div>
      </div>
    }
  </div>
</div>
  `,
  styles: [`
    .profile-hero { display:flex;align-items:flex-start;gap:20px;flex-wrap:wrap; }
    .profile-info { flex:1;min-width:200px; }
    .profile-proj-row { display:flex;align-items:center;gap:10px;padding:12px 16px;background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius);text-decoration:none;color:var(--text-primary);transition:all var(--transition); }
    .profile-proj-row:hover { border-color:var(--border-glow); }
  `]
})
export class ProfileComponent implements OnInit {
  auth = inject(AuthService);
  ps = inject(ProjectService);
  editing = signal(false);
  saving = signal(false);
  loadingProjects = signal(true);
  myProjects = signal<any[]>([]);
  get user() { return this.auth.currentUser(); }
  editName = this.user?.fullName || '';
  editBio = this.user?.bio || '';
  get initials() { return (this.user?.fullName || 'U').split(' ').map((n:string)=>n[0]).join('').toUpperCase().slice(0,2); }
  getLangClass(l: string) { return 'lang-' + l; }

  ngOnInit() {
    if (this.user) {
      this.editName = this.user.fullName || '';
      this.editBio = this.user.bio || '';
      this.ps.getByOwner(this.user.userId).subscribe({
        next: p => { this.myProjects.set(p); this.loadingProjects.set(false); },
        error: () => this.loadingProjects.set(false)
      });
    }
  }

  save() {
    if (!this.user) return;
    this.saving.set(true);
    this.auth.updateProfile(this.user.userId, { fullName: this.editName, bio: this.editBio }).subscribe({
      next: () => { this.saving.set(false); this.editing.set(false); },
      error: () => this.saving.set(false)
    });
  }
}

// ── NOTIFICATIONS ────────────────────────────────────────────
@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
<div class="page-content">
  <div class="container" style="padding-top:32px;padding-bottom:60px;max-width:700px;">
    <div class="flex justify-between items-center" style="margin-bottom:24px;">
      <h1 style="font-size:24px;">Notifications</h1>
      @if (ns.unreadCount > 0) {
        <button class="btn btn-ghost btn-sm" (click)="markAll()">Mark all as read</button>
      }
    </div>
    @if (loading()) {
      <div class="flex items-center gap-2 text-muted"><span class="spinner"></span> Loading...</div>
    }
    <div style="display:flex;flex-direction:column;gap:8px;">
      @for (n of ns.all(); track n.notificationId) {
        <div class="notif-row" [class.unread]="!n.isRead" (click)="markRead(n.notificationId)">
          <div class="notif-type-icon">{{ getIcon(n.type) }}</div>
          <div style="flex:1;">
            <div style="font-weight:600;font-size:13px;margin-bottom:2px;">{{ n.title }}</div>
            <div class="text-secondary" style="font-size:12px;line-height:1.4;">{{ n.message }}</div>
            <div class="mono text-muted" style="font-size:10px;margin-top:4px;">{{ timeAgo(n.createdAt) }}</div>
          </div>
          @if (!n.isRead) { <span class="dot dot-accent"></span> }
        </div>
      }
    </div>
  </div>
</div>
  `,
  styles: [`
    .notif-row { display:flex;align-items:flex-start;gap:14px;padding:16px;background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);cursor:pointer;transition:all var(--transition); }
    .notif-row:hover { border-color:var(--border-glow); }
    .notif-row.unread { border-color:rgba(0,212,255,0.2);background:rgba(0,212,255,0.03); }
    .notif-type-icon { width:40px;height:40px;background:var(--bg-elevated);border:1px solid var(--border);border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:18px;flex-shrink:0; }
  `]
})
export class NotificationsComponent implements OnInit {
  ns = inject(NotificationService);
  auth = inject(AuthService);
  loading = signal(true);

  ngOnInit() {
    const userId = this.auth.currentUser()?.userId;
    if (userId) {
      this.ns.loadNotifications(userId);
      this.loading.set(false);
    }
  }

  markRead(id: string) {
    this.ns.markRead(id).subscribe(() => this.ns.markReadLocal(id));
  }

  markAll() {
    const userId = this.auth.currentUser()?.userId;
    if (userId) this.ns.markAllRead(userId).subscribe(() => this.ns.markAllReadLocal());
  }

  getIcon(type: string) { const m:any={SESSION_INVITE:'🔗',COMMENT:'💬',MENTION:'@',SNAPSHOT:'📸',FORK:'🍴',BROADCAST:'📢'}; return m[type]||'🔔'; }
  timeAgo(d: string) { if (!d) return ''; const m=Math.floor((Date.now()-new Date(d).getTime())/60000); if(m<60)return m+'m ago'; if(m<1440)return Math.floor(m/60)+'h ago'; return Math.floor(m/1440)+'d ago'; }
}

// ── PROJECT DETAIL ───────────────────────────────────────────
@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
<div class="page-content">
  @if (loading()) {
    <div class="container" style="padding-top:60px;text-align:center;"><span class="spinner" style="width:32px;height:32px;border-width:3px;"></span></div>
  } @else if (project()) {
    <div class="container" style="padding-top:32px;padding-bottom:60px;">
      <div class="proj-detail-header">
        <div class="proj-detail-avatar">{{ project()!.name[0].toUpperCase() }}</div>
        <div style="flex:1;">
          <div class="flex items-center gap-3" style="flex-wrap:wrap;">
            <h1 class="mono" style="font-size:22px;">{{ project()!.name }}</h1>
            <span class="badge {{ getLangClass(project()!.language) }}">{{ project()!.language }}</span>
            <span class="badge {{ project()!.visibility==='PUBLIC' ? 'badge-green' : 'badge-orange' }}">{{ project()!.visibility }}</span>
          </div>
          <p class="text-secondary" style="font-size:13px;margin-top:6px;">{{ project()!.description }}</p>
          <div class="flex items-center gap-3 mono" style="font-size:12px;color:var(--text-muted);margin-top:8px;">
            <span>⭐ {{ project()!.starCount }} stars</span>
            <span>🍴 {{ project()!.forkCount }} forks</span>
          </div>
        </div>
        <div class="flex gap-2">
          <a [routerLink]="['/editor']" [queryParams]="{projectId: project()!.projectId}" class="btn btn-primary btn-sm">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            Open Editor
          </a>
          <button class="btn btn-ghost btn-sm">🍴 Fork</button>
          <button class="btn btn-ghost btn-sm" (click)="toggleStar()">{{ starred() ? '⭐ Starred' : '☆ Star' }}</button>
        </div>
      </div>

      <div class="proj-detail-body">
        <div class="proj-files-panel">
          <div class="panel-section-header">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
            Files ({{ files().length }})
          </div>
          @for (f of files(); track f.fileId) {
            <div class="file-list-row">
              <span>{{ f.isFolder ? '📁' : getFileIcon(f.name) }}</span>
              <span class="mono" style="font-size:13px;">{{ f.name }}</span>
              <span class="text-muted mono" style="font-size:10px;margin-left:auto;">{{ f.isFolder ? '' : (f.size || 0) + ' B' }}</span>
            </div>
          }
          @if (files().length === 0) {
            <div style="padding:20px;text-align:center;color:var(--text-muted);font-size:13px;">No files yet</div>
          }
        </div>
        <div class="proj-readme-panel">
          <div class="panel-section-header">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            README
          </div>
          <div style="padding:20px;font-size:13px;color:var(--text-secondary);line-height:1.7;">
            <h2 style="font-size:18px;color:var(--text-primary);margin-bottom:10px;" class="mono">{{ project()!.name }}</h2>
            <p>{{ project()!.description || 'No description provided.' }}</p>
            <p style="margin-top:12px;">Built with <span class="badge {{ getLangClass(project()!.language) }}">{{ project()!.language }}</span></p>
            <a [routerLink]="['/editor']" [queryParams]="{projectId: project()!.projectId}" class="btn btn-primary" style="margin-top:20px;display:inline-flex;">
              Open in Editor →
            </a>
          </div>
        </div>
      </div>
    </div>
  } @else {
    <div class="container" style="padding-top:60px;text-align:center;color:var(--text-muted);">Project not found</div>
  }
</div>
  `,
  styles: [`
    .proj-detail-header { display:flex;align-items:flex-start;gap:16px;margin-bottom:28px;flex-wrap:wrap; }
    .proj-detail-avatar { width:56px;height:56px;background:linear-gradient(135deg,var(--accent-dim),var(--purple-dim));border:1px solid var(--border-glow);border-radius:14px;display:flex;align-items:center;justify-content:center;font-family:var(--font-mono);font-weight:700;font-size:22px;color:var(--accent);flex-shrink:0; }
    .proj-detail-body { display:grid;grid-template-columns:280px 1fr;gap:16px; }
    .proj-files-panel,.proj-readme-panel { background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);overflow:hidden; }
    .panel-section-header { display:flex;align-items:center;gap:8px;padding:12px 16px;border-bottom:1px solid var(--border);font-size:12px;font-weight:600;color:var(--text-secondary); }
    .file-list-row { display:flex;align-items:center;gap:8px;padding:8px 16px;font-size:13px;border-bottom:1px solid rgba(37,48,69,0.4);transition:background var(--transition); }
    .file-list-row:hover { background:var(--bg-elevated); }
    @media(max-width:768px) { .proj-detail-body { grid-template-columns:1fr; } }
  `]
})
export class ProjectDetailComponent implements OnInit {
  ps = inject(ProjectService);
  auth = inject(AuthService);
  route = inject(ActivatedRoute);
  project = signal<any>(null);
  files = signal<any[]>([]);
  loading = signal(true);
  starred = signal(false);

  ngOnInit() {
    const id = this.route.snapshot.params['id'];
    if (!id) { this.loading.set(false); return; }
    this.ps.getById(id).subscribe({
      next: (p) => {
        this.project.set(p);
        this.starred.set(this.ps.isStarred(p.projectId));
        this.loading.set(false);
        this.ps.getFileTree(p.projectId).subscribe(f => this.files.set(f));
      },
      error: () => this.loading.set(false)
    });
  }

  toggleStar() {
    const userId = this.auth.currentUser()?.userId;
    if (!userId || !this.project()) return;
    this.ps.toggleStar(this.project().projectId, userId).subscribe(() =>
      this.starred.set(!this.starred())
    );
  }

  getLangClass(l: string) { return 'lang-' + l; }
  getFileIcon(name: string): string {
    if (name.endsWith('.py')) return '🐍'; if (name.endsWith('.java')) return '☕';
    if (name.endsWith('.ts')) return '📘'; if (name.endsWith('.js')) return '📜';
    return '📄';
  }
}

// ── ADMIN ────────────────────────────────────────────────────
@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
<div class="page-content">
  <div class="container" style="padding-top:32px;padding-bottom:60px;">
    <div style="margin-bottom:24px;">
      <div class="flex items-center gap-2" style="margin-bottom:4px;"><span class="badge badge-purple">ADMIN PANEL</span></div>
      <h1 style="font-size:26px;">Platform Management</h1>
    </div>

    <!-- Analytics -->
    @if (analytics()) {
      <div class="admin-stats">
        <div class="admin-stat-card"><div class="admin-stat-icon" style="color:var(--accent);background:var(--accent-dim);">👥</div><div class="admin-stat-val" style="color:var(--accent);">{{ analytics().totalUsers }}</div><div class="admin-stat-label">Total Users</div></div>
        <div class="admin-stat-card"><div class="admin-stat-icon" style="color:var(--green);background:var(--green-dim);">📁</div><div class="admin-stat-val" style="color:var(--green);">{{ analytics().totalProjects }}</div><div class="admin-stat-label">Projects</div></div>
        <div class="admin-stat-card"><div class="admin-stat-icon" style="color:var(--orange);background:var(--orange-dim);">⚡</div><div class="admin-stat-val" style="color:var(--orange);">{{ analytics().activeSessions }}</div><div class="admin-stat-label">Active Sessions</div></div>
        <div class="admin-stat-card"><div class="admin-stat-icon" style="color:#a78bfa;background:var(--purple-dim);">▶️</div><div class="admin-stat-val" style="color:#a78bfa;">{{ analytics().totalExecutions }}</div><div class="admin-stat-label">Executions</div></div>
      </div>
    }

    <!-- Users -->
    <div class="admin-panel">
      <div class="admin-panel-header">
        <span>User Management</span>
        <span class="badge badge-accent">{{ users().length }} users</span>
      </div>
      <table class="admin-table">
        <thead><tr><th>User</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>
          @for (u of users(); track u.userId) {
            <tr>
              <td>
                <div class="flex items-center gap-2">
                  <div class="avatar avatar-sm">{{ (u.fullName||u.username||'U')[0].toUpperCase() }}</div>
                  <div><div style="font-weight:600;font-size:12px;">{{ u.fullName }}</div><div class="mono text-muted" style="font-size:10px;">&#64;{{ u.username }}</div></div>
                </div>
              </td>
              <td><span class="badge {{ u.role==='ADMIN' ? 'badge-purple' : 'badge-accent' }}" style="font-size:10px;">{{ u.role }}</span></td>
              <td><span class="flex items-center gap-1"><span class="dot {{ u.active ? 'dot-green' : 'dot-red' }}"></span><span style="font-size:11px;">{{ u.active ? 'Active' : 'Suspended' }}</span></span></td>
              <td>
                <div class="flex gap-1">
                  <button class="btn btn-ghost btn-sm" style="padding:3px 8px;font-size:10px;" (click)="toggleUser(u)">{{ u.active ? 'Suspend' : 'Activate' }}</button>
                  <button class="btn btn-danger btn-sm" style="padding:3px 8px;font-size:10px;" (click)="deleteUser(u.userId)">Delete</button>
                </div>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <!-- Sessions -->
    <div class="admin-panel" style="margin-top:16px;">
      <div class="admin-panel-header">
        <span>Active Sessions</span>
        <span class="badge badge-green">{{ sessions().length }} live</span>
      </div>
      @for (s of sessions(); track s.sessionId) {
        <div class="session-admin-row">
          <span class="dot dot-green"></span>
          <span class="mono" style="font-size:12px;font-weight:600;">{{ s.sessionId }}</span>
          <span class="text-muted" style="font-size:11px;">{{ s.language }}</span>
          <button class="btn btn-danger btn-sm" style="padding:3px 8px;font-size:10px;margin-left:auto;" (click)="terminateSession(s.sessionId)">Terminate</button>
        </div>
      }
      @if (sessions().length === 0) {
        <div style="padding:20px;text-align:center;color:var(--text-muted);font-size:13px;">No active sessions</div>
      }
    </div>
  </div>
</div>
  `,
  styles: [`
    .admin-stats { display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:24px; }
    .admin-stat-card { background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);padding:18px;text-align:center; }
    .admin-stat-icon { width:40px;height:40px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:18px;margin:0 auto 8px; }
    .admin-stat-val { font-size:24px;font-weight:700;font-family:var(--font-mono); }
    .admin-stat-label { font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:.08em;margin-top:4px; }
    .admin-panel { background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);overflow:hidden; }
    .admin-panel-header { display:flex;align-items:center;justify-content:space-between;padding:12px 16px;border-bottom:1px solid var(--border);font-size:13px;font-weight:600; }
    .admin-table { width:100%;border-collapse:collapse; }
    .admin-table th { padding:10px 16px;text-align:left;font-size:11px;font-weight:600;color:var(--text-muted);text-transform:uppercase;letter-spacing:.06em;border-bottom:1px solid var(--border);background:var(--bg-base); }
    .admin-table td { padding:10px 16px;border-bottom:1px solid rgba(37,48,69,0.4); }
    .admin-table tr:hover td { background:var(--bg-elevated); }
    .session-admin-row { display:flex;align-items:center;gap:12px;padding:12px 16px;border-bottom:1px solid rgba(37,48,69,0.4);font-size:12px; }
    @media(max-width:768px) { .admin-stats { grid-template-columns:repeat(2,1fr); } }
  `]
})
export class AdminComponent implements OnInit {
  adminService = inject(AdminService);
  analytics = signal<any>(null);
  users = signal<any[]>([]);
  sessions = signal<any[]>([]);

  ngOnInit() {
    this.adminService.getDashboard().subscribe(data => this.analytics.set(data));
    this.adminService.getAllUsers().subscribe(u => this.users.set(u));
    this.adminService.getActiveSessions().subscribe(s => this.sessions.set(s));
  }

  toggleUser(u: any) {
    const obs = u.active
      ? this.adminService.suspendUser(u.userId)
      : this.adminService.reactivateUser(u.userId);
    obs.subscribe(() => this.users.update(us => us.map(x => x.userId === u.userId ? { ...x, active: !x.active } : x)));
  }

  deleteUser(userId: string) {
    if (!confirm('Permanently delete this user?')) return;
    this.adminService.deleteUser(userId).subscribe(() =>
      this.users.update(us => us.filter(x => x.userId !== userId))
    );
  }

  terminateSession(sessionId: string) {
    this.adminService.terminateSession(sessionId).subscribe(() =>
      this.sessions.update(ss => ss.filter(s => s.sessionId !== sessionId))
    );
  }
}
