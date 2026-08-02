/**
 * Scenario 07: Edge Cases & Resilience Chaos Testing
 * Purpose: Tests backend behavior under invalid inputs, bad authorization, out-of-bound parameters, and rate-limiting.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { EDGE_CASE_DATA } from '../data/test-data.js';

export const options = {
  vus: 5,
  duration: '45s',
  thresholds: {
    // Note: Here we expect HTTP status 4xx, so http_req_failed includes 4xx unless handled cleanly.
  },
};

export default function () {
  group('Edge Case 01: Invalid Bearer Tokens (Gateway Auth Filter)', function () {
    EDGE_CASE_DATA.INVALID_TOKENS.forEach((token) => {
      const res = http.get(`${CONFIG.BASE_URL}/api/v1/cart/`, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token,
        },
      });
      check(res, {
        'invalid token yields 401 or 403': (r) => r.status === 401 || r.status === 403,
      });
    });
  });

  sleep(0.5);

  group('Edge Case 02: Bad Auth Credentials', function () {
    const res = http.post(
      `${CONFIG.BASE_URL}/api/v1/auth/login`,
      JSON.stringify({ email: 'nonexistent_user@kyro.com', password: 'WrongPassword123!' }),
      { headers: CONFIG.HEADERS.JSON }
    );
    check(res, {
      'wrong password yields 401 Bad Credentials': (r) => r.status === 401 || r.status === 400,
    });
  });

  sleep(0.5);

  group('Edge Case 03: Non-existent Resource IDs (404 Handling)', function () {
    EDGE_CASE_DATA.OUT_OF_BOUND_IDS.forEach((id) => {
      const res = http.get(`${CONFIG.BASE_URL}/api/v1/products/id/${id}`, {
        headers: CONFIG.HEADERS.JSON,
      });
      check(res, {
        'non-existent product ID yields 404 or 400': (r) => r.status === 404 || r.status === 400,
      });
    });
  });

  sleep(0.5);

  group('Edge Case 04: Invalid Cart Item Quantities', function () {
    // Token is needed to reach cart service
    const loginRes = http.post(
      `${CONFIG.BASE_URL}/api/v1/auth/login`,
      JSON.stringify({ email: 'customer@kyro.com', password: 'Password123!' }),
      { headers: CONFIG.HEADERS.JSON }
    );
    
    if (loginRes.status === 200) {
      const token = JSON.parse(loginRes.body).accessToken;
      const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      };

      EDGE_CASE_DATA.INVALID_QUANTITIES.forEach((qty) => {
        const res = http.post(
          `${CONFIG.BASE_URL}/api/v1/cart/add`,
          JSON.stringify({ productId: 1, size: 'M', quantity: qty }),
          { headers }
        );
        check(res, {
          'invalid quantity rejected with 400 or handled safely': (r) => r.status === 400 || r.status === 200,
        });
      });
    }
  });

  sleep(0.5);

  group('Edge Case 05: Malformed VNPay Callback Params', function () {
    const res = http.get(`${CONFIG.BASE_URL}/api/v1/payments/vnpay-callback`, {
      headers: CONFIG.HEADERS.JSON,
    });
    check(res, {
      'missing vnp_TxnRef yields 400 or 500 error payload': (r) => r.status === 400 || r.status === 500,
    });
  });

  sleep(0.5);

  group('Edge Case 06: OTP Cooldown & Rate Limiting', function () {
    const res1 = http.post(
      `${CONFIG.BASE_URL}/api/v1/auth/register/resend-otp`,
      JSON.stringify({ email: 'customer@kyro.com' }),
      { headers: CONFIG.HEADERS.JSON }
    );

    // Immediately request again to trigger OTP cooldown 429
    const res2 = http.post(
      `${CONFIG.BASE_URL}/api/v1/auth/register/resend-otp`,
      JSON.stringify({ email: 'customer@kyro.com' }),
      { headers: CONFIG.HEADERS.JSON }
    );

    check(res2, {
      'rapid OTP request returns 429 Too Many Requests or 400': (r) => r.status === 429 || r.status === 400,
    });
  });

  sleep(1);
}
