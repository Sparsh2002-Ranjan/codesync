import { Component, inject, signal, Input, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExecutionService } from '../../services/execution.service';

@Component({
  selector: 'app-execution-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="exec-panel" [class.exec-panel--expanded]="isExpanded()">

      <!-- ── Panel header ───────────────────────────────────────────── -->
      <div class="exec-header">
        <div class="exec-header-left">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
          <span class="exec-title">OUTPUT</span>

          @if (ex.isRunning()) {
            <span class="exec-badge exec-badge--running">
              <span class="exec-pulse"></span>
              Running
            </span>
          } @else if (ex.jobStatus() === 'COMPLETED') {
            <span class="exec-badge exec-badge--ok">
              ✓ Exit 0 · {{ formatTime(ex.execTimeMs()) }}
            </span>
          } @else if (ex.jobStatus() === 'FAILED') {
            <span class="exec-badge exec-badge--err">
              ✗ Exit {{ ex.exitCode() ?? 1 }} · {{ formatTime(ex.execTimeMs()) }}
            </span>
          } @else if (ex.jobStatus() === 'TIMEOUT') {
            <span class="exec-badge exec-badge--timeout">⏱ Timeout</span>
          }
        </div>

        <div class="exec-header-right">
          @if (showStdin()) {
            <button class="exec-icon-btn" (click)="showStdin.set(false)" title="Hide stdin">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 3H7a4 4 0 0 0-4 4v10a4 4 0 0 0 4 4h10a4 4 0 0 0 4-4V7a4 4 0 0 0-4-4z"/>
                <line x1="9" y1="12" x2="15" y2="12"/>
              </svg>
            </button>
          } @else {
            <button class="exec-icon-btn" (click)="showStdin.set(true)" title="Provide stdin input">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 3H7a4 4 0 0 0-4 4v10a4 4 0 0 0 4 4h10a4 4 0 0 0 4-4V7a4 4 0 0 0-4-4z"/>
                <line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
              </svg>
            </button>
          }
          <button class="exec-icon-btn" (click)="ex.clearOutput()" title="Clear output">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/>
            </svg>
          </button>
          <button class="exec-icon-btn" (click)="isExpanded.set(!isExpanded())" title="Toggle panel size">
            @if (isExpanded()) {
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="18 15 12 9 6 15"/>
              </svg>
            } @else {
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            }
          </button>
        </div>
      </div>

      <!-- ── stdin input (optional) ────────────────────────────────── -->
      @if (showStdin()) {
        <div class="exec-stdin">
          <span class="exec-stdin-label">stdin:</span>
          <textarea class="exec-stdin-area" [(ngModel)]="stdinValue"
            placeholder="Enter program input here..."
            rows="3"></textarea>
        </div>
      }

      <!-- ── Output terminal ───────────────────────────────────────── -->
      <div class="exec-output" #outputEl>
        @if (ex.outputLines().length === 0 && !ex.isRunning()) {
          <div class="exec-empty">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity=".3">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
            <p>Press <kbd>Run</kbd> to execute your code</p>
          </div>
        }

        @for (line of ex.outputLines(); track $index) {
          <div class="exec-line" [class.exec-line--stderr]="line.type === 'stderr'">
            <span class="exec-line-prefix">{{ line.type === 'stderr' ? '!' : '›' }}</span>
            <span class="exec-line-text">{{ line.text }}</span>
          </div>
        }

        @if (ex.isRunning()) {
          <div class="exec-line exec-line--running">
            <span class="exec-cursor">█</span>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .exec-panel {
      display: flex;
      flex-direction: column;
      height: 220px;
      background: #0d0f12;
      border-top: 1px solid rgba(255,255,255,.08);
      transition: height .25s cubic-bezier(.4,0,.2,1);
    }
    .exec-panel--expanded { height: 420px; }

    /* Header */
    .exec-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 12px;
      height: 34px;
      background: #0a0c0f;
      border-bottom: 1px solid rgba(255,255,255,.06);
      flex-shrink: 0;
    }
    .exec-header-left, .exec-header-right { display: flex; align-items: center; gap: 8px; }
    .exec-title { font-size: 10px; font-weight: 700; letter-spacing: .1em; color: #6b7280; }

    /* Badges */
    .exec-badge {
      display: inline-flex; align-items: center; gap: 5px;
      font-size: 10px; font-weight: 600; padding: 2px 8px;
      border-radius: 10px;
    }
    .exec-badge--running { background: rgba(59,130,246,.15); color: #60a5fa; }
    .exec-badge--ok      { background: rgba(34,197,94,.12);  color: #4ade80; }
    .exec-badge--err     { background: rgba(239,68,68,.12);  color: #f87171; }
    .exec-badge--timeout { background: rgba(234,179,8,.12);  color: #facc15; }
    .exec-pulse {
      width: 7px; height: 7px; border-radius: 50%;
      background: #60a5fa;
      animation: pulse 1.2s ease-in-out infinite;
    }
    @keyframes pulse { 0%,100% { opacity:1; transform:scale(1); } 50% { opacity:.5; transform:scale(.7); } }

    /* Icon buttons */
    .exec-icon-btn {
      background: none; border: none; cursor: pointer;
      color: #4b5563; padding: 4px;
      border-radius: 4px; display: flex; align-items: center;
      transition: color .15s, background .15s;
    }
    .exec-icon-btn:hover { color: #d1d5db; background: rgba(255,255,255,.06); }

    /* stdin */
    .exec-stdin { display: flex; align-items: flex-start; gap: 8px; padding: 8px 12px; border-bottom: 1px solid rgba(255,255,255,.06); flex-shrink: 0; }
    .exec-stdin-label { font-size: 10px; font-weight: 600; color: #6b7280; padding-top: 4px; white-space: nowrap; }
    .exec-stdin-area {
      flex: 1; background: rgba(255,255,255,.04); border: 1px solid rgba(255,255,255,.08);
      border-radius: 4px; color: #e2e8f0; font-family: 'JetBrains Mono', 'Fira Code', monospace;
      font-size: 11px; padding: 6px 8px; resize: none; outline: none;
    }
    .exec-stdin-area:focus { border-color: rgba(99,102,241,.5); }

    /* Output */
    .exec-output {
      flex: 1; overflow-y: auto; padding: 8px 0;
      font-family: 'JetBrains Mono', 'Fira Code', 'Courier New', monospace;
      font-size: 12px; line-height: 1.6;
    }
    .exec-output::-webkit-scrollbar { width: 5px; }
    .exec-output::-webkit-scrollbar-track { background: transparent; }
    .exec-output::-webkit-scrollbar-thumb { background: rgba(255,255,255,.1); border-radius: 3px; }

    .exec-empty {
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      height: 100%; gap: 10px; color: #374151;
    }
    .exec-empty p { font-size: 12px; color: #4b5563; }
    .exec-empty kbd {
      background: rgba(255,255,255,.07); border: 1px solid rgba(255,255,255,.12);
      border-radius: 4px; padding: 1px 6px; font-size: 11px; color: #9ca3af;
    }

    .exec-line {
      display: flex; gap: 10px; padding: 0 14px;
      color: #d1d5db;
    }
    .exec-line--stderr { color: #f87171; }
    .exec-line--running { color: #60a5fa; padding: 2px 14px; }
    .exec-line-prefix { color: #374151; user-select: none; flex-shrink: 0; }
    .exec-line--stderr .exec-line-prefix { color: #7f1d1d; }
    .exec-line-text { white-space: pre-wrap; word-break: break-all; }

    .exec-cursor {
      display: inline-block;
      animation: blink .8s step-end infinite;
    }
    @keyframes blink { 0%,100% { opacity:1; } 50% { opacity:0; } }
  `]
})
export class ExecutionPanelComponent {
  ex = inject(ExecutionService);

  isExpanded = signal(false);
  showStdin  = signal(false);
  stdinValue = '';

  @Input() language = '';
  @Input() code     = '';
  @Input() projectId?: string;
  @Input() fileId?: string;

  run(): void {
    this.ex.execute({
      language: this.language,
      code:     this.code,
      stdin:    this.stdinValue || undefined,
      projectId: this.projectId,
      fileId:   this.fileId
    });
  }

  formatTime(ms: number | null): string {
    if (ms == null) return '';
    if (ms < 1000) return ms + 'ms';
    return (ms / 1000).toFixed(2) + 's';
  }

  get stdin(): string { return this.stdinValue; }
}
