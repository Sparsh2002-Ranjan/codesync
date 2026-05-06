import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Comment } from '../models';

@Injectable({ providedIn: 'root' })
export class CommentService {
  private api = `${environment.apiUrl}/comments`;

  constructor(private http: HttpClient) {}

  addComment(comment: Partial<Comment>): Observable<Comment> {
    return this.http.post<Comment>(this.api, comment);
  }

  getByFile(fileId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.api}/file/${fileId}`);
  }

  getByProject(projectId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.api}/project/${projectId}`);
  }

  getReplies(commentId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.api}/${commentId}/replies`);
  }

  getByLine(fileId: string, lineNumber: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.api}/file/${fileId}/line/${lineNumber}`);
  }

  updateComment(commentId: string, content: string): Observable<Comment> {
    return this.http.put<Comment>(`${this.api}/${commentId}`, { content });
  }

  deleteComment(commentId: string): Observable<any> {
    return this.http.delete(`${this.api}/${commentId}`);
  }

  resolveComment(commentId: string): Observable<Comment> {
    return this.http.put<Comment>(`${this.api}/${commentId}/resolve`, {});
  }

  unresolveComment(commentId: string): Observable<Comment> {
    return this.http.put<Comment>(`${this.api}/${commentId}/unresolve`, {});
  }

  getCount(fileId: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.api}/file/${fileId}/count`);
  }
}
