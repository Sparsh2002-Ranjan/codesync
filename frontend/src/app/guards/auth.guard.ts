import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// AUTH GUARD — currently commented out in routes for demo
// To enable: import this and add canActivate: [authGuard] to protected routes

export const authGuard = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn) return true;
  router.navigate(['/login']);
  return false;
};

export const adminGuard = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAdmin) return true;
  router.navigate(['/dashboard']);
  return false;
};
