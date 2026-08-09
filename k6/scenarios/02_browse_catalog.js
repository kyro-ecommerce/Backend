/**
 * Scenario 02: Browse Catalog & Search Load Test
 * Purpose: Simulates heavy browsing traffic (Read-Heavy workload) across catalog & AI search.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { SEARCH_KEYWORDS, CATEGORIES, getRandomElement } from '../data/test-data.js';
import { metrics } from '../utils/metrics.js';

export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp-up
    { duration: '2m', target: 50 },   // Sustained peak load
    { duration: '30s', target: 0 },   // Ramp-down
  ],
  thresholds: {
    kyro_catalog_req_duration: [CONFIG.THRESHOLDS.HTTP_REQ_DURATION_P95],
    kyro_catalog_success_rate: ['rate>0.99'],
  },
};

export default function () {
  group('Catalog - Product List & Filtering', function () {
    const category = getRandomElement(CATEGORIES.TOP);
    const keyword = getRandomElement(SEARCH_KEYWORDS);
    const filterUrl = `${CONFIG.BASE_URL}/api/v1/products?topLevelCategory=${category}&minPrice=100&maxPrice=1000000&sort=price_low&keyword=${keyword}`;

    const start = Date.now();
    const res = http.get(filterUrl, { headers: CONFIG.HEADERS.JSON });
    metrics.catalogDuration.add(Date.now() - start);

    const ok = check(res, {
      'filter products status 200': (r) => r.status === 200,
      'response is array': (r) => {
        try {
          return Array.isArray(JSON.parse(r.body));
        } catch (e) {
          return false;
        }
      },
    });
    metrics.catalogSuccessRate.add(ok);
  });

  sleep(1);

  group('Catalog - View Product Details', function () {
    const productId = Math.floor(Math.random() * 10) + 1; // ID 1..10
    const detailUrl = `${CONFIG.BASE_URL}/api/v1/products/${productId}`;

    const start = Date.now();
    const res = http.get(detailUrl, { headers: CONFIG.HEADERS.JSON });
    metrics.catalogDuration.add(Date.now() - start);

    const ok = check(res, {
      'get product detail status 200': (r) => r.status === 200,
      'product ID matches': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body && body.id === productId;
        } catch (e) {
          return false;
        }
      },
    });
    metrics.catalogSuccessRate.add(ok);
  });

  sleep(1);

  group('Catalog - Product Reviews', function () {
    const productId = Math.floor(Math.random() * 5) + 1;
    const reviewUrl = `${CONFIG.BASE_URL}/api/v1/reviews/product/${productId}`;

    const start = Date.now();
    const res = http.get(reviewUrl, { headers: CONFIG.HEADERS.JSON });
    metrics.catalogDuration.add(Date.now() - start);

    const ok = check(res, {
      'get reviews status 200': (r) => r.status === 200,
    });
    metrics.catalogSuccessRate.add(ok);
  });

  sleep(1);

  group('AI Service - Semantic Search', function () {
    const keyword = getRandomElement(SEARCH_KEYWORDS);
    const aiSearchUrl = `${CONFIG.AI_SERVICE_URL}/search?q=${encodeURIComponent(keyword)}`;

    const start = Date.now();
    const res = http.get(aiSearchUrl, { headers: CONFIG.HEADERS.JSON });
    metrics.aiDuration.add(Date.now() - start);

    const ok = check(res, {
      'AI search returns status 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    metrics.aiSuccessRate.add(ok);
  });

  sleep(1.5);
}
