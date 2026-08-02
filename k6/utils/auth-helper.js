/**
 * Authentication & Session Helper for k6
 */
import http from 'k6/http';
import { check } from 'k6';
import { CONFIG } from '../config/environments.js';
import { SEED_USERS, generateAddressPayload } from '../data/test-data.js';
import { metrics } from './metrics.js';

/**
 * Authenticates against /api/v1/auth/login and returns JWT access token & user object.
 */
export function login(email = SEED_USERS[0].email, password = SEED_USERS[0].password) {
  const url = `${CONFIG.BASE_URL}/api/v1/auth/login`;
  const payload = JSON.stringify({ email, password });
  const params = { headers: CONFIG.HEADERS.JSON };

  const start = Date.now();
  const res = http.post(url, payload, params);
  const duration = Date.now() - start;

  metrics.authDuration.add(duration);

  const success = check(res, {
    'login status is 200': (r) => r.status === 200,
    'login returned accessToken': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body && !!body.accessToken;
      } catch (e) {
        return false;
      }
    },
  });

  metrics.authSuccessRate.add(success);

  if (!success) {
    if (res.status >= 400 && res.status < 500) metrics.http4xxErrors.add(1);
    if (res.status >= 500) metrics.http5xxErrors.add(1);
    return null;
  }

  const responseData = JSON.parse(res.body);
  return {
    token: responseData.accessToken,
    user: responseData.user,
  };
}

/**
 * Creates authenticated HTTP headers with JWT Bearer Token.
 */
export function getAuthHeaders(token) {
  return {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

/**
 * Ensures user has at least one address created and returns addressId.
 */
export function getOrCreateAddress(token) {
  const headers = getAuthHeaders(token);
  
  // 1. Get existing addresses
  const getUrl = `${CONFIG.BASE_URL}/api/v1/users/addresses`;
  const getRes = http.get(getUrl, { headers });

  if (getRes.status === 200) {
    try {
      const addresses = JSON.parse(getRes.body);
      if (Array.isArray(addresses) && addresses.length > 0) {
        return addresses[0].id;
      }
    } catch (e) {}
  }

  // 2. If no address found, create a new address
  const createUrl = `${CONFIG.BASE_URL}/api/v1/users/addresses`;
  const addressPayload = JSON.stringify(generateAddressPayload());
  const createRes = http.post(createUrl, addressPayload, { headers });

  if (createRes.status === 200 || createRes.status === 201) {
    try {
      const newAddress = JSON.parse(createRes.body);
      return newAddress.id;
    } catch (e) {}
  }

  return 1; // Fallback default ID if seed data exists
}
