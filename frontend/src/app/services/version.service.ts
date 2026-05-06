import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Snapshot } from '../models';

@Injectable({ providedIn: 'root' })
export class VersionService {
  private api = `${environment.apiUrl}/versions`;

  constructor(private http: HttpClient) {}

  createSnapshot(snapshot: Partial<Snapshot>): Observable<Snapshot> {
    return this.http.post<Snapshot>(`${this.api}/snapshots`, snapshot);
  }

  getFileHistory(fileId: string): Observable<Snapshot[]> {
    return this.http.get<Snapshot[]>(`${this.api}/history/${fileId}`);
  }

  getByFile(fileId: string): Observable<Snapshot[]> {
    return this.http.get<Snapshot[]>(`${this.api}/snapshots/file/${fileId}`);
  }

  getLatest(fileId: string, branch = 'main'): Observable<Snapshot> {
    return this.http.get<Snapshot>(`${this.api}/snapshots/file/${fileId}/latest?branch=${branch}`);
  }

  diffSnapshots(s1: string, s2: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/snapshots/diff?s1=${s1}&s2=${s2}`);
  }

  restoreSnapshot(snapshotId: string): Observable<Snapshot> {
    return this.http.post<Snapshot>(`${this.api}/snapshots/${snapshotId}/restore`, {});
  }

  tagSnapshot(snapshotId: string, tag: string): Observable<Snapshot> {
    return this.http.put<Snapshot>(`${this.api}/snapshots/${snapshotId}/tag?tag=${tag}`, {});
  }

  createBranch(projectId: string, name: string, createdById: string): Observable<any> {
    return this.http.post(`${this.api}/branches?projectId=${projectId}&name=${name}&createdById=${createdById}`, {});
  }

  getBranches(projectId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/branches/${projectId}`);
  }
}
