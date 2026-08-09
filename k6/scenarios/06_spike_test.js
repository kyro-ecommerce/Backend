/**
 * Scenario 06: Flash Sale Spike Test
 * Purpose: Simulates sudden, massive traffic bursts (e.g., Flash Sale / Black Friday drop) to test recovery.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { PRODUCT_SIZES, getRandomElement } from '../data/test-data.js';

export const options = {
  stages: [
    { duration: '10s', target: 250 }, // Explosive spike 0 -> 250 VUs in 10 seconds!
    { duration: '1m',  target: 250 }, // Sustained flash sale pressure
    { duration: '10s', target: 0 },   // Instant drop
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'],    // Up to 10% allowed under instant spike
  },
};

export default function () {
  // Flash sale spike: Users rushing to buy Product #1
  const productId = 1;
  const detailUrl = `${CONFIG.BASE_URL}/api/v1/products/${productId}`;
  
  const res = http.get(detailUrl, { headers: CONFIG.HEADERS.JSON });

  check(res, {
    'spike product fetch 200': (r) => r.status === 200,
  });

  sleep(0.2);
}
