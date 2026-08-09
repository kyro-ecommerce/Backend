/**
 * Scenario 01: Smoke Test
 * Purpose: Quick 30-second verification across all microservices to check overall system readiness.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { login, getAuthHeaders } from '../utils/auth-helper.js';

export const options = {
  vus: 2,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  group('01. Gateway & Auth Service Health', function () {
    const authSession = login();
    check(authSession, {
      'Auth login successful': (s) => s !== null && s.token.length > 0,
    });
  });

  group('02. Catalog Service Health', function () {
    const res = http.get(`${CONFIG.BASE_URL}/api/v1/products`, {
      headers: CONFIG.HEADERS.JSON,
    });
    check(res, {
      'Catalog products return 200': (r) => r.status === 200,
    });
  });

  group('03. Cart Service (Redis) Health', function () {
    const authSession = login();
    if (authSession) {
      const headers = getAuthHeaders(authSession.token);
      const res = http.get(`${CONFIG.BASE_URL}/api/v1/carts`, { headers });
      check(res, {
        'Cart retrieval returns 200': (r) => r.status === 200,
      });
    }
  });

  group('04. Category & Reviews Health', function () {
    const res = http.get(`${CONFIG.BASE_URL}/api/v1/categories/`, {
      headers: CONFIG.HEADERS.JSON,
    });
    check(res, {
      'Category listing returns 200': (r) => r.status === 200,
    });
  });

  sleep(1);
}
