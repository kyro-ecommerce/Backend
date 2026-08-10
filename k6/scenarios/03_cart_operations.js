/**
 * Scenario 03: Cart Operations Load Test
 * Purpose: Tests high-throughput PostgreSQL cart operations with Redis cache (Add, View, Update, Clear).
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { login, getAuthHeaders } from '../utils/auth-helper.js';
import { PRODUCT_SIZES, getRandomElement } from '../data/test-data.js';
import { metrics } from '../utils/metrics.js';

export const options = {
  stages: [
    { duration: '30s', target: 15 },
    { duration: '2m', target: 40 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    kyro_cart_req_duration: [CONFIG.THRESHOLDS.CART_DURATION_P95],
    kyro_cart_success_rate: ['rate>0.99'],
  },
};

export default function () {
  const authSession = login();
  if (!authSession) {
    sleep(1);
    return;
  }

  const headers = getAuthHeaders(authSession.token);
  const productId = Math.floor(Math.random() * 10) + 1;
  const size = getRandomElement(PRODUCT_SIZES);

  group('Cart - Add Item', function () {
    const url = `${CONFIG.BASE_URL}/api/v1/carts/items`;
    const payload = JSON.stringify({
      productId: productId,
      size: size,
      quantity: 2,
    });

    const start = Date.now();
    const res = http.post(url, payload, { headers });
    metrics.cartDuration.add(Date.now() - start);

    const ok = check(res, {
      'add item to cart status 200': (r) => r.status === 200,
      'cart contains items': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body && body.items && body.items.length > 0;
        } catch (e) {
          return false;
        }
      },
    });
    metrics.cartSuccessRate.add(ok);
  });

  sleep(0.5);

  group('Cart - Get Cart Details', function () {
    const url = `${CONFIG.BASE_URL}/api/v1/carts`;

    const start = Date.now();
    const res = http.get(url, { headers });
    metrics.cartDuration.add(Date.now() - start);

    const ok = check(res, {
      'get cart status 200': (r) => r.status === 200,
    });
    metrics.cartSuccessRate.add(ok);
  });

  sleep(0.5);

  group('Cart - Update Item Quantity', function () {
    const cart = http.get(`${CONFIG.BASE_URL}/api/v1/carts`, { headers });
    const itemId = JSON.parse(cart.body).items.find((item) => item.productId === productId && item.size === size)?.id;
    const url = `${CONFIG.BASE_URL}/api/v1/carts/items/${itemId}?quantity=5`;

    const start = Date.now();
    const res = http.put(url, null, { headers });
    metrics.cartDuration.add(Date.now() - start);

    const ok = check(res, {
      'update cart item status 200': (r) => r.status === 200,
    });
    metrics.cartSuccessRate.add(ok);
  });

  sleep(0.5);

  group('Cart - Clear Cart', function () {
    const url = `${CONFIG.BASE_URL}/api/v1/carts/items`;

    const start = Date.now();
    const res = http.del(url, null, { headers });
    metrics.cartDuration.add(Date.now() - start);

    const ok = check(res, {
      'clear cart status 200': (r) => r.status === 200,
    });
    metrics.cartSuccessRate.add(ok);
  });

  sleep(1);
}
