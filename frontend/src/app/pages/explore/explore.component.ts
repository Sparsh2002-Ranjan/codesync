import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProjectService } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';
import { Project, SUPPORTED_LANGUAGES } from '../../models';

@Component({
  selector: 'app-explore',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './explore.component.html',
  styleUrl: './explore.component.scss'
})
export class ExploreComponent implements OnInit {
  ps = inject(ProjectService);
  auth = inject(AuthService);

  allProjects = signal<Project[]>([]);
  loading = signal(true);
  query = signal('');
  selectedLang = signal('');
  sort = signal<'stars'|'forks'|'updated'>('stars');
  languages = SUPPORTED_LANGUAGES;

  ngOnInit() {
    const userId = this.auth.currentUser()?.userId;
    if (userId) this.ps.loadStarredIds(userId);
    this.ps.getPublicProjects().subscribe({
      next: (p) => { this.allProjects.set(p); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  get projects() {
    let list = this.allProjects();
    if (this.query()) {
      const q = this.query().toLowerCase();
      list = list.filter(p => p.name.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q) || p.language.includes(q));
    }
    if (this.selectedLang()) list = list.filter(p => p.language === this.selectedLang());
    if (this.sort() === 'stars') list = [...list].sort((a,b) => b.starCount - a.starCount);
    else if (this.sort() === 'forks') list = [...list].sort((a,b) => b.forkCount - a.forkCount);
    return list;
  }

  toggleStar(projectId: string) {
    const userId = this.auth.currentUser()?.userId;
    if (!userId) return;
    this.ps.toggleStar(projectId, userId).subscribe();
  }

  forkProject(project: any) {
    const userId = this.auth.currentUser()?.userId;
    if (!userId) { alert('Please log in to fork projects.'); return; }
    if (!confirm(`Fork "${project.name}"? A copy will be created in your account.`)) return;
    this.ps.forkProject(project.projectId, userId).subscribe({
      next: (forked) => {
        // Increment forkCount in local list for immediate feedback
        this.allProjects.update(list =>
          list.map(p => p.projectId === project.projectId ? { ...p, forkCount: p.forkCount + 1 } : p)
        );
        alert(`Forked successfully as "${forked.name}"! Check your dashboard.`);
      },
      error: (err) => alert(err.error?.message || 'Failed to fork project')
    });
  }

  isStarred(id: string) { return this.ps.isStarred(id); }
  getLangClass(lang: string) { return 'lang-' + lang.toLowerCase(); }
  formatDate(d: string) { return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }); }
}
