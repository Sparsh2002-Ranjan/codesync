import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// ── Types ─────────────────────────────────────────────────────────────────
export interface ExecuteRequest {
  language: string;
  code: string;
  stdin?: string;
  projectId?: string;
  fileId?: string;
}

export interface ExecuteResponse {
  jobId: string;
  status: string;
  message: string;
}

export interface JobResult {
  jobId: string;
  status: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'TIMEOUT' | 'CANCELLED';
  stdout?: string;
  stderr?: string;
  exitCode?: number;
  executionTimeMs?: number;
  language: string;
  createdAt: string;
  finishedAt?: string;
}

export interface OutputChunk {
  jobId: string;
  type: 'stdout' | 'stderr' | 'status' | 'done';
  data: string;
  exitCode?: number;
  executionTimeMs?: number;
}

// ── Service ───────────────────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class ExecutionService {
  private http = inject(HttpClient);
  private execUrl = `${environment.executionUrl || 'http://localhost:8085'}/api/v1/execution`;

  // Reactive state
  isRunning   = signal(false);
  outputLines = signal<{ type: string; text: string }[]>([]);
  jobStatus   = signal<string>('');
  currentJobId = signal<string>('');
  exitCode    = signal<number | null>(null);
  execTimeMs  = signal<number | null>(null);

  private stompClient: Client | null = null;
  private subscription: StompSubscription | null = null;

  // ── Submit execution ────────────────────────────────────────────────────
  run(req: ExecuteRequest): Observable<ExecuteResponse> {
    return this.http.post<ExecuteResponse>(`${this.execUrl}/run`, req);
  }

  // ── Poll result ─────────────────────────────────────────────────────────
  getJob(jobId: string): Observable<JobResult> {
    return this.http.get<JobResult>(`${this.execUrl}/jobs/${jobId}`);
  }

  // ── History ─────────────────────────────────────────────────────────────
  myJobs(): Observable<JobResult[]> {
    return this.http.get<JobResult[]>(`${this.execUrl}/jobs/me`);
  }

  // ── Full run flow: submit → connect WS → stream output ─────────────────
  execute(req: ExecuteRequest): void {
    // Reset state
    this.isRunning.set(true);
    this.outputLines.set([]);
    this.jobStatus.set('QUEUED');
    this.exitCode.set(null);
    this.execTimeMs.set(null);

    this.run(req).subscribe({
      next: (resp) => {
        this.currentJobId.set(resp.jobId);
        this.subscribeToJob(resp.jobId);
      },
      error: (err) => {
        this.isRunning.set(false);
        this.jobStatus.set('FAILED');
        this.appendLine('stderr', '❌ Failed to submit job: ' + (err.error?.message || err.message));
      }
    });
  }

  // ── WebSocket subscription ───────────────────────────────────────────────
  private subscribeToJob(jobId: string): void {
    const wsUrl = `${environment.executionWsUrl || 'http://localhost:8085'}/ws/execution`;

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      reconnectDelay: 0,
      onConnect: () => {
        this.subscription = this.stompClient!.subscribe(
          `/topic/execution/${jobId}`,
          (msg: IMessage) => this.handleChunk(JSON.parse(msg.body) as OutputChunk)
        );
      },
      onStompError: (frame) => {
        // Fall back to HTTP polling if WS fails
        this.pollUntilDone(jobId);
      }
    });
    this.stompClient.activate();
  }

  private handleChunk(chunk: OutputChunk): void {
    switch (chunk.type) {
      case 'stdout':
        this.appendLine('stdout', chunk.data);
        break;
      case 'stderr':
        this.appendLine('stderr', chunk.data);
        break;
      case 'status':
        this.jobStatus.set(chunk.data);
        break;
      case 'done':
        this.jobStatus.set(chunk.data);
        if (chunk.exitCode !== undefined && chunk.exitCode !== null)
          this.exitCode.set(chunk.exitCode);
        if (chunk.executionTimeMs)
          this.execTimeMs.set(chunk.executionTimeMs);
        this.isRunning.set(false);
        this.disconnectWs();
        break;
    }
  }

  // ── HTTP polling fallback ───────────────────────────────────────────────
  private pollUntilDone(jobId: string, attempts = 0): void {
    if (attempts > 60) { this.isRunning.set(false); return; }
    setTimeout(() => {
      this.getJob(jobId).subscribe({
        next: (job) => {
          this.jobStatus.set(job.status);
          if (['COMPLETED', 'FAILED', 'TIMEOUT', 'CANCELLED'].includes(job.status)) {
            if (job.stdout) job.stdout.split('\n').forEach(l => this.appendLine('stdout', l));
            if (job.stderr) job.stderr.split('\n').forEach(l => this.appendLine('stderr', l));
            this.exitCode.set(job.exitCode ?? null);
            this.execTimeMs.set(job.executionTimeMs ?? null);
            this.isRunning.set(false);
          } else {
            this.pollUntilDone(jobId, attempts + 1);
          }
        },
        error: () => this.pollUntilDone(jobId, attempts + 1)
      });
    }, 1000);
  }

  private appendLine(type: string, text: string): void {
    this.outputLines.update(lines => [...lines, { type, text }]);
  }

  private disconnectWs(): void {
    this.subscription?.unsubscribe();
    this.stompClient?.deactivate();
    this.stompClient = null;
    this.subscription = null;
  }

  clearOutput(): void {
    this.outputLines.set([]);
    this.jobStatus.set('');
    this.exitCode.set(null);
    this.execTimeMs.set(null);
  }
}
