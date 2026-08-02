/**
 * Scenario 05: System Stress Test
 * Purpose: Determines system breaking point, maximum requests per second (RPS), and connection limits.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { SEARCH_KEYWORDS, getRandomElement } from '../data/test-data.js';
import { metrics } from '../utils/metrics.js';

export const options = {
  stages: [
    { duration: '1m', target: 50 },    // Warm up
    { duration: '2m', target: 150 },   // Medium stress
    { duration: '2m', target: 300 },   // High stress
    { duration: '2m', target: 300 },   // Peak sustain
    { duration: '1m', target: 0 },     // Cool down
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],     // Under stress, allow up to 5% failure max
    http_req_duration: ['p(95)<2000'],  // p95 latency under 2s under heavy load
  },
};

export default function () {
  // Mixed workload under stress
  const rand = Math.random();

  if (rand < 0.6) {
    // 60% Read Catalog
    const category = getRandomElement(['clothing', 'footwear']);
    const res = http.get(`${CONFIG.BASE_URL}/api/v1/products/?topLevelCategory=${category}`, {
      headers: CONFIG.HEADERS.JSON,
    });
    check(res, { 'stress product list 200': (r) => r.status === 200 });
  } else if (rand < 0.85) {
    // 25% Product Details & AI Search
    const keyword = getRandomElement(SEARCH_KEYWORDS);
    const res = http.get(`${CONFIG.AI_SERVICE_URL}/search?q=${encodeURIComponent(keyword)}`, {
      headers: CONFIG.HEADERS.JSON,
    });
    check(res, { 'stress AI search OK': (r) => r.status === 200 || r.status === 404 });
  } else {
    // 15% Auth attempts
    const res = http.post(`${CONFIG.BASE_URL}/api/v1/auth/login`, 
      JSON.stringify({ email: 'customer@kyro.com', password: 'Password123!' }), 
      { headers: CONFIG.HEADERS.JSON }
    );
    check(res, { 'stress login status 200': (r) => r.status === 200 });
  }

  sleep(0.5);
}
