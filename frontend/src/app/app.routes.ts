import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { EditorComponent } from './pages/editor/editor.component';
import { ExploreComponent } from './pages/explore/explore.component';
import {
  ProfileComponent,
  NotificationsComponent,
  ProjectDetailComponent,
  AdminComponent
} from './pages/other-pages';

// import { authGuard, adminGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '',             component: HomeComponent },
  { path: 'login',        component: LoginComponent },
  { path: 'register',     component: RegisterComponent },
  { path: 'explore',      component: ExploreComponent },
  { path: 'dashboard',    component: DashboardComponent },   // canActivate: [authGuard]
  { path: 'editor',       component: EditorComponent },      // canActivate: [authGuard]
  { path: 'project/:id',  component: ProjectDetailComponent },
  { path: 'profile',      component: ProfileComponent },
  { path: 'notifications',component: NotificationsComponent },
  { path: 'admin',        component: AdminComponent },       // canActivate: [adminGuard]
  { path: '**',           redirectTo: '' }
];
