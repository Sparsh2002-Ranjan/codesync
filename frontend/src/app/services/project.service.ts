import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Project, CodeFile } from '../models';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private api = `${environment.apiUrl}/projects`;
  private filesApi = `${environment.apiUrl}/files`;

  // Local signal cache for starred projects
  private _starredIds = signal<Set<string>>(new Set());

  constructor(private http: HttpClient) {}

  // ── Projects ─────────────────────────────────────────────────
  createProject(project: Partial<Project>): Observable<Project> {
    return this.http.post<Project>(this.api, project);
  }

  getById(projectId: string): Observable<Project> {
    return this.http.get<Project>(`${this.api}/${projectId}`);
  }

  getPublicProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/public`);
  }

  getByOwner(ownerId: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/owner/${ownerId}`);
  }

  getByMember(userId: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/member/${userId}`);
  }

  searchProjects(query: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/search?query=${query}`);
  }

  getByLanguage(language: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/language/${language}`);
  }

  updateProject(projectId: string, updates: Partial<Project>): Observable<Project> {
    return this.http.put<Project>(`${this.api}/${projectId}`, updates);
  }

  deleteProject(projectId: string): Observable<any> {
    return this.http.delete(`${this.api}/${projectId}`);
  }

  archiveProject(projectId: string): Observable<any> {
    return this.http.put(`${this.api}/${projectId}/archive`, {});
  }

  forkProject(projectId: string, newOwnerId: string): Observable<Project> {
    return this.http.post<Project>(`${this.api}/${projectId}/fork?newOwnerId=${newOwnerId}`, {});
  }

  // ── Stars ─────────────────────────────────────────────────────
  getStarredProjects(userId: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/starred/${userId}`);
  }

  loadStarredIds(userId: string): void {
    this.getStarredProjects(userId).subscribe({
      next: (projects) => {
        const ids = new Set(projects.map(p => p.projectId));
        this._starredIds.set(ids);
      }
    });
  }

  starProject(projectId: string, userId: string): Observable<any> {
    return this.http.post(`${this.api}/${projectId}/star?userId=${userId}`, {}).pipe(
      tap(() => this._starredIds.update(s => { const n = new Set(s); n.add(projectId); return n; }))
    );
  }

  unstarProject(projectId: string, userId: string): Observable<any> {
    return this.http.delete(`${this.api}/${projectId}/star?userId=${userId}`).pipe(
      tap(() => this._starredIds.update(s => { const n = new Set(s); n.delete(projectId); return n; }))
    );
  }

  isStarred(projectId: string): boolean {
    return this._starredIds().has(projectId);
  }

  toggleStar(projectId: string, userId: string): Observable<any> {
    return this.isStarred(projectId)
      ? this.unstarProject(projectId, userId)
      : this.starProject(projectId, userId);
  }

  // ── Members ───────────────────────────────────────────────────
  getMembers(projectId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/${projectId}/members`);
  }

  addMember(projectId: string, userId: string, role = 'EDITOR'): Observable<any> {
    return this.http.post(`${this.api}/${projectId}/members?userId=${userId}&role=${role}`, {});
  }

  removeMember(projectId: string, userId: string): Observable<any> {
    return this.http.delete(`${this.api}/${projectId}/members/${userId}`);
  }

  // ── Files ─────────────────────────────────────────────────────
  getFileTree(projectId: string): Observable<CodeFile[]> {
    return this.http.get<CodeFile[]>(`${this.filesApi}/project/${projectId}/tree`);
  }

  getFileById(fileId: string): Observable<CodeFile> {
    return this.http.get<CodeFile>(`${this.filesApi}/${fileId}`);
  }

  createFile(file: Partial<CodeFile>): Observable<CodeFile> {
    return this.http.post<CodeFile>(this.filesApi, file);
  }

  createFolder(projectId: string, name: string, path: string, createdById: string): Observable<CodeFile> {
    return this.http.post<CodeFile>(
      `${this.filesApi}/folder?projectId=${projectId}&name=${name}&path=${path}&createdById=${createdById}`, {}
    );
  }

  updateFileContent(fileId: string, content: string, editorUserId: string): Observable<CodeFile> {
    return this.http.put<CodeFile>(
      `${this.filesApi}/${fileId}/content?editorUserId=${editorUserId}`,
      { content }
    );
  }

  renameFile(fileId: string, newName: string): Observable<CodeFile> {
    return this.http.put<CodeFile>(`${this.filesApi}/${fileId}/rename?newName=${newName}`, {});
  }

  deleteFile(fileId: string): Observable<any> {
    return this.http.delete(`${this.filesApi}/${fileId}`);
  }

  restoreFile(fileId: string): Observable<any> {
    return this.http.post(`${this.filesApi}/${fileId}/restore`, {});
  }

  searchInProject(projectId: string, query: string): Observable<CodeFile[]> {
    return this.http.get<CodeFile[]>(`${this.filesApi}/project/${projectId}/search?query=${query}`);
  }
}
