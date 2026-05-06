import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { Project, SUPPORTED_LANGUAGES } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  projectService = inject(ProjectService);
  route = inject(ActivatedRoute);

  myProjects = signal<Project[]>([]);
  starredProjects = signal<Project[]>([]);
  loading = signal(true);

  showNewProjectModal = signal(false);
  activeTab = signal<'projects' | 'starred'>('projects');
  searchQuery = signal('');

  newProject = { name: '', description: '', language: 'javascript', visibility: 'PRIVATE' as 'PUBLIC' | 'PRIVATE' };
  languages = SUPPORTED_LANGUAGES;

  get user() { return this.auth.currentUser(); }

  get filteredProjects() {
    const source = this.activeTab() === 'starred' ? this.starredProjects() : this.myProjects();
    const q = this.searchQuery().toLowerCase();
    if (!q) return source;
    return source.filter(p => p.name.toLowerCase().includes(q) || p.language.includes(q));
  }

  ngOnInit() {
    this.loadProjects();
    // Open new project modal if ?new=1 in URL
    this.route.queryParams.subscribe(p => { if (p['new']) this.showNewProjectModal.set(true); });
  }

  loadProjects() {
    this.loading.set(true);
    const userId = this.user?.userId || '';
    this.projectService.loadStarredIds(userId);
    this.projectService.getByOwner(userId).subscribe({
      next: (projects) => { this.myProjects.set(projects); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  createProject() {
    if (!this.newProject.name) return;
    const project = {
      ...this.newProject,
      ownerId: this.user?.userId,
    };
    this.projectService.createProject(project).subscribe({
      next: (created) => {
        this.myProjects.update(p => [created, ...p]);
        this.showNewProjectModal.set(false);
        this.newProject = { name: '', description: '', language: 'javascript', visibility: 'PRIVATE' };
      },
      error: (err) => alert(err.error?.message || 'Failed to create project')
    });
  }

  toggleStar(projectId: string, e: Event) {
    e.preventDefault(); e.stopPropagation();
    const userId = this.user?.userId || '';
    this.projectService.toggleStar(projectId, userId).subscribe({
      next: () => {
        // Refresh starred list if on starred tab
        if (this.activeTab() === 'starred') this.loadStarredProjects();
      }
    });
  }

  loadStarredProjects() {
    const userId = this.user?.userId || '';
    this.projectService.getStarredProjects(userId).subscribe({
      next: (projects) => this.starredProjects.set(projects)
    });
  }

  switchTab(tab: 'projects' | 'starred') {
    this.activeTab.set(tab);
    if (tab === 'starred') this.loadStarredProjects();
  }

  forkProject(projectId: string, e: Event) {
    e.preventDefault(); e.stopPropagation();
    const userId = this.user?.userId || '';
    if (!confirm('Fork this project? A copy will be created in your account.')) return;
    this.projectService.forkProject(projectId, userId).subscribe({
      next: (forked) => {
        this.myProjects.update(p => [forked, ...p]);
        alert(`Project forked successfully as "${forked.name}"!`);
      },
      error: (err) => alert(err.error?.message || 'Failed to fork project')
    });
  }

  deleteProject(projectId: string, projectName: string, e: Event) {
    e.preventDefault(); e.stopPropagation();
    if (!confirm(`Delete project "${projectName}"? This cannot be undone.`)) return;
    this.projectService.deleteProject(projectId).subscribe({
      next: () => this.myProjects.update(p => p.filter(x => x.projectId !== projectId)),
      error: (err) => alert(err.error?.message || 'Failed to delete project')
    });
  }

  isStarred(id: string) { return this.projectService.isStarred(id); }
  getLangClass(lang: string) { return `lang-${lang.toLowerCase()}`; }
  formatDate(d: string) { return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }); }

  recentActivity = [
    { icon: '📸', text: 'Create a snapshot after your first commit', project: '', time: 'tip', color: 'var(--accent)' },
    { icon: '⚡', text: 'Start a live session to collaborate in real time', project: '', time: 'tip', color: 'var(--green)' },
    { icon: '▶️', text: 'Run your code directly from the editor', project: '', time: 'tip', color: 'var(--orange)' },
  ];
}
