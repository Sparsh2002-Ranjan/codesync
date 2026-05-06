import { Component, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  auth = inject(AuthService);
  router = inject(Router);

  email = signal('admin@codesync.io');
  password = signal('admin123');
  error = signal('');
  loading = signal(false);

  onSubmit() {
    this.error.set('');
    this.loading.set(true);

    this.auth.login(this.email(), this.password()).subscribe({
      next: () => {
        this.loading.set(false);
        // Redirect admin to admin panel, others to dashboard
        if (this.auth.isAdmin) this.router.navigate(['/admin']);
        else this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Invalid email or password');
      }
    });
  }

  // Quick demo shortcuts
  fillAdmin() { this.email.set('admin@codesync.io'); this.password.set('admin123'); }
  fillDev()   { this.email.set('dev@codesync.io');   this.password.set('dev123');   }
}
