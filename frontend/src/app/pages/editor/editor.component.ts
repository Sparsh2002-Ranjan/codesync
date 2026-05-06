import {
  Component, inject, signal, OnDestroy, OnInit, AfterViewInit,
  HostListener, ViewChild, ElementRef, effect, NgZone
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { CollabService } from '../../services/collab.service';
import { VersionService } from '../../services/version.service';
import { CommentService } from '../../services/comment.service';
import { ExecutionService } from '../../services/execution.service';
import { ExecutionPanelComponent } from '../../components/execution-panel/execution-panel.component';
import { CodeFile, Snapshot, Comment } from '../../models';

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ExecutionPanelComponent],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.scss'
})
export class EditorComponent implements OnInit, AfterViewInit, OnDestroy {
  auth   = inject(AuthService);
  ps     = inject(ProjectService);
  collab = inject(CollabService);
  vs     = inject(VersionService);
  cs     = inject(CommentService);
  ex     = inject(ExecutionService);
  route  = inject(ActivatedRoute);
  router = inject(Router);
  zone   = inject(NgZone);

  @ViewChild('codeArea') codeAreaRef!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('execPanel') execPanelRef!: ExecutionPanelComponent;

  showOutput = signal(false);

  project: any = null;
  files       = signal<CodeFile[]>([]);
  activeFile  = signal<CodeFile | null>(null);
  fileContent = signal('');

  rightPanel = signal<'history' | 'comments'>('history');
  leftPanel  = signal<'files' | 'search' | 'collab'>('files');
  isInSession       = signal(false);
  pendingJoinApproval = signal(false); // waiting for host to accept share-link join
  sessionLink       = signal('');
  expandedFolders   = signal<Set<string>>(new Set());
  linkCopied        = signal(false);
  loadingFiles      = signal(true);
  saving            = signal(false);
  saved             = signal(false);
  unsavedChanges    = signal(false);

  showSnapshotModal  = signal(false);
  showNewFileModal   = signal(false);
  showNewFolderModal = signal(false);
  showCollabModal    = signal(false);
  inviteSent         = signal(false);
  inviteSentTo       = signal('');

  // Rename / delete file
  showRenameModal   = signal(false);
  showDeleteFileConfirm = signal(false);
  fileToAction      = signal<CodeFile | null>(null);
  renameValue       = '';

  newFileName = ''; newFolderName = ''; newSnapshotMsg = '';
  newComment  = ''; searchQuery   = ''; inviteUsername = '';
  searchResults: { line: number; text: string }[] = [];
  userSearchResults: any[] = [];

  snapshots = signal<Snapshot[]>([]);
  comments  = signal<Comment[]>([]);

  private saveTimer: any;
  private isApplyingRemote = false;
  private viewReady = false;

  private starterCode: Record<string, { filename: string; content: string }> = {
    python:     { filename: 'main.py',    content: '# Python\n\ndef main():\n    print("Hello from Python!")\n\nif __name__ == "__main__":\n    main()\n' },
    java:       { filename: 'Main.java',  content: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello from Java!");\n    }\n}\n' },
    javascript: { filename: 'index.js',   content: '// JavaScript\nconsole.log("Hello from JavaScript!");\n' },
    typescript: { filename: 'index.ts',   content: '// TypeScript\nconst greet = (name: string) => `Hello, ${name}!`;\nconsole.log(greet("World"));\n' },
    go:         { filename: 'main.go',    content: 'package main\nimport "fmt"\nfunc main() {\n    fmt.Println("Hello from Go!")\n}\n' },
    rust:       { filename: 'main.rs',    content: 'fn main() {\n    println!("Hello from Rust!");\n}\n' },
    cpp:        { filename: 'main.cpp',   content: '#include <iostream>\nusing namespace std;\nint main() {\n    cout << "Hello from C++!" << endl;\n    return 0;\n}\n' },
    kotlin:     { filename: 'Main.kt',    content: 'fun main() {\n    println("Hello from Kotlin!")\n}\n' },
  };

  constructor() {
    // Watch remoteContent — fires whenever another user sends an edit via WebSocket
    effect(() => {
      const remote = this.collab.remoteContent();
      if (!remote) return;

      const file = this.activeFile();
      if (!file || remote.fileId !== file.fileId) return;

      this.isApplyingRemote = true;

      // 1. Update the signal so line numbers re-render
      this.fileContent.set(remote.content);
      file.content = remote.content;

      // 2. Force update the textarea DOM directly (bypasses ngModel stale state)
      this.zone.run(() => {
        const ta = this.codeAreaRef?.nativeElement;
        if (ta) {
          const sel = ta.selectionStart;
          ta.value = remote.content;
          ta.setSelectionRange(
            Math.min(sel, remote.content.length),
            Math.min(sel, remote.content.length)
          );
        }
        this.isApplyingRemote = false;
      });
    });
  }

  @HostListener('window:keydown', ['$event'])
  onKeyDown(e: KeyboardEvent) {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      this.saveFile();
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      this.runCode();
    }
  }

  ngOnInit() {
    const qParams        = this.route.snapshot.queryParams;
    const projectId      = qParams['projectId'] || this.route.snapshot.params['id'];
    const sessionIdParam = qParams['sessionId'];

    const loadProject = (id: string) => {
      this.ps.getById(id).subscribe({
        next: (p) => { this.project = p; this.loadFiles(p.projectId, sessionIdParam); },
        error: () => this.loadingFiles.set(false)
      });
    };

    if (projectId) {
      loadProject(projectId);
    } else {
      this.ps.getByOwner(this.auth.currentUser()?.userId || '').subscribe({
        next: (projects) => {
          if (projects.length) loadProject(projects[0].projectId);
          else this.loadingFiles.set(false);
        },
        error: () => this.loadingFiles.set(false)
      });
    }

    // Connect for invite notifications even before joining a session
    const userId = this.auth.currentUser()?.userId;
    if (userId) this.collab.connectNotifications(userId);
  }

  ngAfterViewInit() {
    this.viewReady = true;
  }

  ngOnDestroy() {
    clearTimeout(this.saveTimer);
    if (this.isInSession()) {
      const sid = this.collab.sessionId();
      if (sid) this.collab.leaveSession(sid, this.auth.currentUser()?.userId || '');
    }
  }

  loadFiles(projectId: string, autoJoinSessionId?: string) {
    this.loadingFiles.set(true);
    this.ps.getFileTree(projectId).subscribe({
      next: (files) => {
        const active = files.filter(f => !f.isDeleted);
        if (active.length === 0) {
          const lang    = this.project?.language || 'javascript';
          const starter = this.starterCode[lang] || this.starterCode['javascript'];
          this.ps.createFile({
            projectId, name: starter.filename, path: starter.filename,
            language: lang, content: starter.content,
            createdById: this.auth.currentUser()?.userId || '',
            lastEditedBy: this.auth.currentUser()?.userId || '', isDeleted: false
          }).subscribe({ next: (f) => {
            this.files.set([f]); this.selectFile(f);
            this.loadingFiles.set(false);
            this.autoJoinIfNeeded(autoJoinSessionId);
          }});
        } else {
          this.files.set(active);
          const first = active.find(f => !f.isFolder);
          if (first) this.selectFile(first);
          this.loadingFiles.set(false);
          this.autoJoinIfNeeded(autoJoinSessionId);
        }
      },
      error: () => this.loadingFiles.set(false)
    });
  }

  private autoJoinIfNeeded(sessionId?: string) {
    if (!sessionId) return;
    const userId   = this.auth.currentUser()?.userId || '';
    const username = this.auth.currentUser()?.username || '';
    const isHost   = this.project?.ownerId === userId;

    this.sessionLink.set(
      `${window.location.origin}/editor?projectId=${this.project?.projectId}&sessionId=${sessionId}`
    );
    this.leftPanel.set('collab');

    if (isHost) {
      // Host always joins directly
      this.collab.joinSession(sessionId, userId, username);
      this.isInSession.set(true);
    } else {
      // Non-host arrived via share link: send a join request to the host
      this.collab.requestJoinApi(sessionId, userId, username).subscribe({
        next: () => {
          this.pendingJoinApproval.set(true);
          // Connect WebSocket just for notifications while waiting
          this.collab.connectNotifications(userId);
          // Listen for join_accepted event
          const sub = this.collab.messages$.subscribe(msg => {
            if (msg.type === 'join_accepted' && msg.payload.sessionId === sessionId) {
              this.pendingJoinApproval.set(false);
              this.collab.joinSession(sessionId, userId, username);
              this.isInSession.set(true);
              sub.unsubscribe();
            }
          });
        },
        error: (err) => console.error('Failed to send join request:', err)
      });
    }
  }

  selectFile(file: CodeFile) {
    if (file.isFolder) { this.toggleFolder(file.name); return; }
    if (this.unsavedChanges() && this.activeFile()) this.saveFile();
    this.activeFile.set(file);
    this.fileContent.set(file.content || '');
    this.unsavedChanges.set(false);
    this.saved.set(false);
    this.cs.getByFile(file.fileId).subscribe(c => this.comments.set(c));
    this.vs.getFileHistory(file.fileId).subscribe(s => this.snapshots.set(s));
  }

  onContentChange(content: string) {
    if (this.isApplyingRemote) return;

    // Block edits if a session is active and the user is not a participant
    // (e.g. someone who opened the page but wasn't accepted yet)
    if (this.isInSession() && !this.isSessionMember) return;

    this.fileContent.set(content);
    this.unsavedChanges.set(true);
    this.saved.set(false);
    const file = this.activeFile();
    if (file) file.content = content;

    clearTimeout(this.saveTimer);
    this.saveTimer = setTimeout(() => this.saveFile(), 3000);

    // Send edit to all collaborators via WebSocket only if in session
    if (this.isInSession() && this.collab.sessionId()) {
      this.collab.sendEdit(
        this.collab.sessionId()!,
        this.auth.currentUser()?.userId || '',
        file?.fileId || '',
        content
      );
    }
  }

  // ── Save ─────────────────────────────────────────────────────
  saveFile() {
    const file = this.activeFile();
    if (!file || !this.unsavedChanges()) return;
    this.saving.set(true);
    this.ps.updateFileContent(file.fileId, this.fileContent(), this.auth.currentUser()?.userId || '')
      .subscribe({
        next: () => {
          this.saving.set(false); this.saved.set(true); this.unsavedChanges.set(false);
          this.files.update(fs => fs.map(f => f.fileId === file.fileId ? { ...f, content: this.fileContent() } : f));
          setTimeout(() => this.saved.set(false), 2000);
        },
        error: (err) => { this.saving.set(false); console.error('Save failed:', err); }
      });
  }

  // ── File tree ─────────────────────────────────────────────────
  getTreeFiles(): CodeFile[] { return this.files().filter(f => !f.isDeleted); }

  toggleFolder(name: string) {
    this.expandedFolders.update(s => { const n = new Set(s); n.has(name) ? n.delete(name) : n.add(name); return n; });
  }

  createNewFile() {
    if (!this.newFileName.trim() || !this.project) return;
    const name = this.newFileName.trim();
    const ext  = name.includes('.') ? '.' + name.split('.').pop()! : '';
    const langMap: Record<string, string> = {
      '.py':'python','.java':'java','.js':'javascript','.ts':'typescript',
      '.go':'go','.rs':'rust','.cpp':'cpp','.c':'cpp','.kt':'kotlin',
      '.rb':'ruby','.php':'php','.r':'r','.md':'markdown','.txt':'text'
    };
    const lang    = langMap[ext] || 'text';
    const starter = this.starterCode[lang];
    this.ps.createFile({
      projectId: this.project.projectId, name, path: name, language: lang,
      content: starter ? starter.content : `// ${name}\n`,
      createdById: this.auth.currentUser()?.userId || '',
      lastEditedBy: this.auth.currentUser()?.userId || '', isDeleted: false
    }).subscribe({
      next: (f) => { this.files.update(fs => [...fs, f]); this.selectFile(f); this.newFileName = ''; this.showNewFileModal.set(false); },
      error: (e) => alert(e.error?.message || 'Failed to create file')
    });
  }

  createNewFolder() {
    if (!this.newFolderName.trim() || !this.project) return;
    this.ps.createFolder(
      this.project.projectId, this.newFolderName.trim(),
      this.newFolderName.trim(), this.auth.currentUser()?.userId || ''
    ).subscribe({ next: (f) => { this.files.update(fs => [...fs, f]); this.newFolderName = ''; this.showNewFolderModal.set(false); } });
  }

  // ── File rename / delete ──────────────────────────────────────
  openRename(file: CodeFile, e: Event) {
    e.stopPropagation();
    this.fileToAction.set(file);
    this.renameValue = file.name;
    this.showRenameModal.set(true);
  }

  confirmRename() {
    const file = this.fileToAction();
    if (!file || !this.renameValue.trim()) return;
    this.ps.renameFile(file.fileId, this.renameValue.trim()).subscribe({
      next: (updated) => {
        this.files.update(fs => fs.map(f => f.fileId === updated.fileId ? updated : f));
        if (this.activeFile()?.fileId === updated.fileId) this.activeFile.set(updated);
        this.showRenameModal.set(false);
        this.fileToAction.set(null);
      },
      error: (e) => alert(e.error?.message || 'Rename failed')
    });
  }

  openDeleteFile(file: CodeFile, e: Event) {
    e.stopPropagation();
    this.fileToAction.set(file);
    this.showDeleteFileConfirm.set(true);
  }

  confirmDeleteFile() {
    const file = this.fileToAction();
    if (!file) return;
    this.ps.deleteFile(file.fileId).subscribe({
      next: () => {
        this.files.update(fs => fs.filter(f => f.fileId !== file.fileId));
        if (this.activeFile()?.fileId === file.fileId) this.activeFile.set(null);
        this.showDeleteFileConfirm.set(false);
        this.fileToAction.set(null);
      },
      error: (e) => alert(e.error?.message || 'Delete failed')
    });
  }

  // ── Delete project ────────────────────────────────────────────
  deleteProject() {
    if (!this.project) return;
    if (!confirm(`Delete project "${this.project.name}"? This cannot be undone.`)) return;
    this.ps.deleteProject(this.project.projectId).subscribe({
      next: () => { window.location.href = '/dashboard'; },
      error: (e) => alert(e.error?.message || 'Failed to delete project')
    });
  }
  createSnapshot() {
    if (!this.newSnapshotMsg.trim() || !this.activeFile()) return;
    this.saveFile();
    this.vs.createSnapshot({
      projectId: this.project?.projectId, fileId: this.activeFile()!.fileId,
      authorId: this.auth.currentUser()?.userId || '',
      message: this.newSnapshotMsg, content: this.fileContent(), branch: 'main'
    }).subscribe({
      next: (s) => { this.snapshots.update(ss => [s, ...ss]); this.newSnapshotMsg = ''; this.showSnapshotModal.set(false); this.rightPanel.set('history'); },
      error: (e) => alert(e.error?.message || 'Snapshot failed')
    });
  }

  restoreSnapshot(snap: Snapshot) {
    this.vs.restoreSnapshot(snap.snapshotId).subscribe({
      next: (s) => { this.fileContent.set(s.content); const f = this.activeFile(); if (f) f.content = s.content; this.unsavedChanges.set(true); this.snapshots.update(ss => [s, ...ss]); }
    });
  }

  // ── Comments ─────────────────────────────────────────────────
  addComment() {
    if (!this.newComment.trim() || !this.activeFile()) return;
    // Derive the current line number from the textarea cursor position
    const ta = this.codeAreaRef?.nativeElement;
    const lineNumber = ta
      ? (this.fileContent().slice(0, ta.selectionStart).split('\n').length)
      : 1;
    this.cs.addComment({
      projectId: this.project?.projectId, fileId: this.activeFile()!.fileId,
      authorId: this.auth.currentUser()?.userId || '',
      authorUsername: this.auth.currentUser()?.username || '',
      content: this.newComment, lineNumber, columnNumber: 1, resolved: false
    }).subscribe({ next: (c) => { this.comments.update(cs => [c, ...cs]); this.newComment = ''; } });
  }

  resolveComment(id: string) {
    const c = this.comments().find(x => x.commentId === id);
    if (!c) return;
    const obs = c.resolved ? this.cs.unresolveComment(id) : this.cs.resolveComment(id);
    obs.subscribe({ next: (u) => this.comments.update(cs => cs.map(x => x.commentId === id ? u : x)) });
  }

  // ── Session ──────────────────────────────────────────────────
  startSession() {
    if (!this.activeFile() || !this.project) return;
    const userId   = this.auth.currentUser()?.userId || '';
    const username = this.auth.currentUser()?.username || '';
    this.collab.createSession(
      this.project.projectId, this.activeFile()!.fileId, userId, this.project.language
    ).subscribe({
      next: (session: any) => {
        const sessionId = session.sessionId;
        this.sessionLink.set(
          `${window.location.origin}/editor?projectId=${this.project.projectId}&sessionId=${sessionId}`
        );
        this.collab.joinSession(sessionId, userId, username);
        this.isInSession.set(true);
        this.leftPanel.set('collab');
      },
      error: (e) => alert(e.error?.message || 'Failed to start session')
    });
  }

  leaveSession() {
    const sid = this.collab.sessionId();
    if (sid) this.collab.leaveSession(sid, this.auth.currentUser()?.userId || '');
    this.isInSession.set(false);
  }

  copySessionLink() {
    navigator.clipboard.writeText(this.sessionLink()).then(() => {
      this.linkCopied.set(true);
      setTimeout(() => this.linkCopied.set(false), 2500);
    });
  }

  // ── Invite ───────────────────────────────────────────────────
  searchUsers() {
    const q = this.inviteUsername.trim();
    if (!q) { this.userSearchResults = []; return; }
    this.auth.searchUsers(q).subscribe({
      next: (users) => {
        const myId = this.auth.currentUser()?.userId;
        this.userSearchResults = users.filter(u => u.userId !== myId);
      },
      error: () => this.userSearchResults = []
    });
  }

  inviteUser(user: any) {
    const sid = this.collab.sessionId();
    if (!sid) {
      alert('You need to start a session first!\nClick "Start Session" button above, then invite.');
      return;
    }
    const me = this.auth.currentUser();
    this.collab.sendInvite(
      user.userId,
      me?.userId || '',
      me?.username || '',
      sid,
      this.project?.projectId,
      this.project?.name
    ).subscribe({
      next: () => {
        this.inviteSent.set(true);
        this.inviteSentTo.set(user.username);
        this.userSearchResults = [];
        this.inviteUsername = '';
        setTimeout(() => { this.inviteSent.set(false); this.inviteSentTo.set(''); }, 4000);
      },
      error: (e) => alert(e.error?.message || `Invite failed: ${e.status} ${e.statusText}`)
    });
  }

  acceptInvite(invite: { sessionId: string; projectId: string }) {
    this.collab.dismissInvite(invite.sessionId);
    this.router.navigate(['/editor'], {
      queryParams: { projectId: invite.projectId, sessionId: invite.sessionId }
    });
  }

  dismissInvite(sessionId: string) { this.collab.dismissInvite(sessionId); }

  // ── Search in project ────────────────────────────────────────
  doSearch() {
    if (!this.searchQuery.trim() || !this.project) return;
    this.ps.searchInProject(this.project.projectId, this.searchQuery).subscribe(files => {
      this.searchResults = files.map(f => ({ line: 1, text: f.name + ': ' + (f.content || '').split('\n')[0] }));
    });
  }

  // ── Helpers ──────────────────────────────────────────────────
  hasCommentOnLine(line: number): boolean { return this.comments().some(c => c.lineNumber === line && !c.resolved); }
  get unresolvedComments(): number { return this.comments().filter(c => !c.resolved).length; }
  get pendingInvites() { return this.collab.pendingInvites(); }

  /** True only if the current user is the project owner */
  get isHost(): boolean { return this.project?.ownerId === this.auth.currentUser()?.userId; }

  /** True if the user is in the active session (host or accepted member) */
  get isSessionMember(): boolean { return this.isInSession() && this.participants.some(p => p.userId === this.auth.currentUser()?.userId); }

  // ── Run code execution ───────────────────────────────────────────────────
  runCode(): void {
    const file = this.activeFile();
    if (!file) return;
    this.showOutput.set(true);
    this.ex.execute({
      language: this.project?.language || file.language || 'python',
      code:     this.fileContent(),
      stdin:    this.execPanelRef?.stdin || undefined,
      projectId: this.project?.projectId,
      fileId:   file.fileId
    });
  }

  getFileIcon(name: string): string {
    if (name.endsWith('.py'))  return '🐍'; if (name.endsWith('.java')) return '☕';
    if (name.endsWith('.ts'))  return '📘'; if (name.endsWith('.js'))   return '📜';
    if (name.endsWith('.go'))  return '🐹'; if (name.endsWith('.rs'))   return '🦀';
    if (name.endsWith('.cpp') || name.endsWith('.c')) return '⚙️';
    if (name.endsWith('.kt'))  return '🎯'; if (name.endsWith('.md'))   return '📝';
    return '📄';
  }

  getLangClass(lang: string) { return 'lang-' + lang.toLowerCase(); }
  getCodeLines() { return this.fileContent().split('\n'); }
  getInitials(name: string) { return (name || 'U').split('_').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2); }
  timeAgo(d: string) { const m = Math.floor((Date.now() - new Date(d).getTime()) / 60000); return m < 60 ? m + 'm ' : Math.floor(m/60) + 'h '; }
  copyProjectLink() { navigator.clipboard.writeText(this.projectShareLink); }

  get participants()     { return this.collab.participants(); }
  get projectShareLink() { return `${window.location.origin}/project/${this.project?.projectId}`; }
  get projectMembers(): any[] { return []; }
}
