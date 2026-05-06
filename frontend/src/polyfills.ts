// Polyfill for sockjs-client: it references Node.js 'global' which doesn't exist in browsers.
// This must run before any sockjs/stomp imports.
(window as any).global = window;
