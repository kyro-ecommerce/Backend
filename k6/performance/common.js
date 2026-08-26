import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import { Trend, Rate, Counter } from 'k6/metrics';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
export const BASE_USER_ID = Number(__ENV.BASE_USER_ID || 100000);
export const PAYMENT_ORDER_ID = 9000000;

export const technicalSuccess = new Rate('technical_success_rate');
export const observationSuccess = new Rate('rabbitmq_observation_success_rate');
export const syncPathLatency = new Trend('synchronous_path_latency', true);
export const propagationLatency = new Trend('rabbitmq_propagation_latency', true);
export const acceptedRequests = new Counter('accepted_requests');
export const observedUpdates = new Counter('observed_updates');

export function auth(userId) {
  const now = Math.floor(Date.now() / 1000);
  const header = b64({ alg: 'HS256', typ: 'JWT' });
  const payload = b64({ sub: `perf-${userId}@kyro.test`, id: userId, roles: ['CUSTOMER'], iat: now, exp: now + 3600 });
  const key = encoding.b64decode(required('JWT_SECRET'), 'std');
  const signature = encoding.b64encode(crypto.hmac('sha256', key, `${header}.${payload}`, 'binary'), 'rawurl');
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${header}.${payload}.${signature}` };
}

export function body(response) {
  try { return JSON.parse(response.body) || {}; } catch (_) { return {}; }
}

export function userId(iteration) {
  return BASE_USER_ID + iteration;
}

export function thresholds() {
  return {
    technical_success_rate: ['rate>=0.99'],
    dropped_iterations: ['count==0'],
  };
}

function b64(value) {
  return encoding.b64encode(JSON.stringify(value), 'rawurl');
}

export function required(name) {
  if (!__ENV[name]) throw new Error(`Set ${name}`);
  return __ENV[name];
}
