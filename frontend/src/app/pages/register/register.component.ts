import { Component, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
<div class="auth-page">
  <div class="auth-bg"><div class="grid-pattern"></div></div>
  <div class="auth-container">
    <a routerLink="/" class="auth-brand">
      <div class="logo-icon-sm">
        <svg viewBox="0 0 32 32" fill="none">
          <path d="M4 8h8l4 4-4 4H4V8z" fill="var(--accent)" opacity="0.9"/>
          <path d="M28 24h-8l-4-4 4-4h8v8z" fill="var(--green)" opacity="0.9"/>
        </svg>
      </div>
      <span>Code<span class="text-accent">Sync</span></span>
    </a>
    <div class="auth-card fade-in">
      <h2 class="auth-title">Create your account</h2>
      <p class="auth-subtitle text-secondary">Join the CodeSync platform</p>

      <form (ngSubmit)="onSubmit()" class="auth-form">
        @if (error()) {
          <div class="auth-error">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ error() }}
          </div>
        }
        <div class="form-row">
          <div class="form-group">
            <label>Full Name</label>
            <input class="input" type="text" placeholder="Jane Smith" [(ngModel)]="fullName" name="fullName" required>
          </div>
          <div class="form-group">
            <label>Username</label>
            <input class="input" type="text" placeholder="jane_dev" [(ngModel)]="username" name="username" required>
          </div>
        </div>
        <div class="form-group">
          <label>Email Address</label>
          <input class="input" type="email" placeholder="jane@example.com" [(ngModel)]="email" name="email" required>
        </div>
        <div class="form-group">
          <label>Password</label>
          <input class="input" type="password" placeholder="Min. 6 characters" [(ngModel)]="password" name="password" required>
        </div>
        <button type="submit" class="btn btn-primary w-full" style="justify-content:center;height:44px;" [disabled]="loading()">
          @if (loading()) { <span class="spinner"></span> } @else { Create Account }
        </button>
      </form>
      <p class="auth-switch">Already have an account? <a routerLink="/login">Sign in</a></p>
    </div>
  </div>
</div>
  `,
  styleUrl: '../login/login.component.scss'
})
export class RegisterComponent {
  auth = inject(AuthService);
  router = inject(Router);

  fullName = ''; username = ''; email = ''; password = '';
  error = signal('');
  loading = signal(false);

  onSubmit() {
    if (!this.username || !this.email || !this.password) {
      this.error.set('Please fill all required fields.');
      return;
    }
    this.loading.set(true);
    this.auth.register({ fullName: this.fullName, username: this.username, email: this.email, password: this.password })
      .subscribe({
        next: () => { this.loading.set(false); this.router.navigate(['/dashboard']); },
        error: (err) => { this.loading.set(false); this.error.set(err.error?.message || 'Registration failed'); }
      });
  }
}
