import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { Project } from '../../models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  auth = inject(AuthService);
  projectService = inject(ProjectService);

  topProjects = signal<Project[]>([]);

  stats = [
    { label: 'Active Sessions', value: '1,247', icon: '⚡', color: 'var(--accent)' },
    { label: 'Projects Hosted', value: '38,912', icon: '📁', color: 'var(--green)' },
    { label: 'Code Executions', value: '2.4M', icon: '▶️', color: 'var(--orange)' },
    { label: 'Developers', value: '14,832', icon: '👩‍💻', color: '#a78bfa' },
  ];

  features = [
    { icon: '⚡', title: 'Real-Time Co-Editing', desc: 'Multiple developers editing the same file simultaneously with OT/CRDT conflict resolution and live cursor presence.' },
    { icon: '🔐', title: 'Sandboxed Execution', desc: 'Run Python, Java, Go, Rust, JS and 9 more languages in isolated Docker containers with resource limits.' },
    { icon: '📸', title: 'Version Snapshots', desc: 'Git-inspired history with SHA-256 integrity, Myers diff algorithm, and non-destructive restore.' },
    { icon: '💬', title: 'Inline Code Review', desc: 'Threaded comments anchored to specific lines, with resolve workflow and @mention notifications.' },
    { icon: '🌿', title: 'Branching & Tags', desc: 'Named branches for parallel development. Tag snapshots with semantic version labels like v1.0.0.' },
    { icon: '🔔', title: 'Smart Notifications', desc: 'Real-time WebSocket badge updates for session invites, comment replies, @mentions, and new snapshots.' },
  ];

  ngOnInit() {
    this.projectService.getPublicProjects().subscribe({
      next: (projects) => this.topProjects.set(projects.slice(0, 3)),
      error: () => {}
    });
  }

  getLangClass(lang: string): string { return `lang-${lang.toLowerCase()}`; }
}
