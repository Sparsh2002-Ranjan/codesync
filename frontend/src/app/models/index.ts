export interface User {
  userId: string;
  username: string;
  email: string;
  fullName: string;
  role: 'DEVELOPER' | 'ADMIN';
  avatarUrl?: string;
  provider: 'LOCAL' | 'GITHUB' | 'GOOGLE';
  isActive: boolean;
  createdAt: string;
  bio?: string;
}

export interface Project {
  projectId: string;
  ownerId: string;
  ownerUsername: string;
  name: string;
  description: string;
  language: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  isArchived: boolean;
  createdAt: string;
  updatedAt: string;
  starCount: number;
  forkCount: number;
  memberCount?: number;
}

export interface CodeFile {
  fileId: string;
  projectId: string;
  name: string;
  path: string;
  language: string;
  content: string;
  size: number;
  createdById: string;
  lastEditedBy: string;
  createdAt: string;
  updatedAt: string;
  isDeleted: boolean;
  isFolder?: boolean;
  children?: CodeFile[];
}

export interface CollabSession {
  sessionId: string;
  projectId: string;
  fileId: string;
  ownerId: string;
  status: 'ACTIVE' | 'ENDED';
  language: string;
  createdAt: string;
  endedAt?: string;
  maxParticipants: number;
  isPasswordProtected: boolean;
  participants?: Participant[];
}

export interface Participant {
  participantId: string;
  sessionId: string;
  userId: string;
  username: string;
  avatarUrl?: string;
  role: 'HOST' | 'EDITOR' | 'VIEWER';
  joinedAt: string;
  cursorLine: number;
  cursorCol: number;
  color: string;
  isActive: boolean;
}

export interface Snapshot {
  snapshotId: string;
  projectId: string;
  fileId: string;
  authorId: string;
  authorUsername: string;
  message: string;
  content: string;
  hash: string;
  parentSnapshotId?: string;
  branch: string;
  tag?: string;
  createdAt: string;
}


export interface Comment {
  commentId: string;
  projectId: string;
  fileId: string;
  authorId: string;
  authorUsername: string;
  content: string;
  lineNumber: number;
  columnNumber: number;
  parentCommentId?: string;
  resolved: boolean;
  snapshotId: string;
  createdAt: string;
  updatedAt: string;
  replies?: Comment[];
}

export interface Notification {
  notificationId: string;
  recipientId: string;
  actorId: string;
  actorUsername: string;
  type: 'SESSION_INVITE' | 'JOIN_REQUEST' | 'JOIN_ACCEPTED' | 'COMMENT' | 'MENTION' | 'SNAPSHOT' | 'FORK';
  title: string;
  message: string;
  relatedId: string;
  relatedType: string;
  deepLinkUrl?: string;
  isRead: boolean;
  createdAt: string;
}

export interface CursorUpdate {
  userId: string;
  username: string;
  color: string;
  line: number;
  col: number;
}

export interface EditDelta {
  userId: string;
  sessionId: string;
  fileId: string;
  content: string;
  timestamp: number;
}

export interface SupportedLanguage {
  id: string;
  name: string;
  version: string;
  extension: string;
  sandboxImage: string;
  isEnabled: boolean;
}

export const CURSOR_COLORS = [
  '#ff6b9d', '#00d4ff', '#00ffa3', '#ffd60a',
  '#ff6b35', '#a78bfa', '#f97316', '#ec4899'
];

export const SUPPORTED_LANGUAGES: SupportedLanguage[] = [
  { id: 'python', name: 'Python', version: '3.11', extension: '.py', sandboxImage: 'python:3.11-slim', isEnabled: true },
  { id: 'java', name: 'Java', version: '21', extension: '.java', sandboxImage: 'openjdk:21-slim', isEnabled: true },
  { id: 'javascript', name: 'JavaScript', version: 'Node 20', extension: '.js', sandboxImage: 'node:20-slim', isEnabled: true },
  { id: 'typescript', name: 'TypeScript', version: '5.x', extension: '.ts', sandboxImage: 'node:20-slim', isEnabled: true },
  { id: 'go', name: 'Go', version: '1.21', extension: '.go', sandboxImage: 'golang:1.21-slim', isEnabled: true },
  { id: 'rust', name: 'Rust', version: '1.75', extension: '.rs', sandboxImage: 'rust:1.75-slim', isEnabled: true },
  { id: 'cpp', name: 'C++', version: 'C++17', extension: '.cpp', sandboxImage: 'gcc:13-slim', isEnabled: true },
  { id: 'kotlin', name: 'Kotlin', version: '1.9', extension: '.kt', sandboxImage: 'openjdk:21-slim', isEnabled: true },
];
