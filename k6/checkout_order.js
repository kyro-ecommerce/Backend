import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const email = __ENV.K6_USER_EMAIL;
const password = __ENV.K6_USER_PASSWORD;
const feignPathLatency = new Trend('feign_path_latency', true);
const feignPathSuccess = new Rate('feign_path_success');
const rabbitmqPaymentToOrderLatency = new Trend('rabbitmq_payment_to_order_latency', true);
const rabbitmqPaymentToOrderSuccess = new Rate('rabbitmq_payment_to_order_success');
const checkoutSuccess = new Rate('checkout_success');

if (!email || !password) throw new Error('Set K6_USER_EMAIL and K6_USER_PASSWORD.');

export const options = {
  vus: 1,
  iterations: Number(__ENV.K6_ITERATIONS || 10),
  thresholds: {
    http_req_failed: ['rate<0.05'],
    checkout_success: ['rate>0.95'],
  },
};

export default function () {
  const login = http.post(`${baseUrl}/auth/login`, JSON.stringify({ email, password }), json());
  if (!check(login, { 'login 200': (r) => r.status === 200 })) return fail();
  const token = body(login).accessToken;
  if (!token) return fail();
  const headers = json(token);

  const addresses = http.get(`${baseUrl}/users/me/addresses`, { headers });
  const addressId = body(addresses)[0]?.id;
  if (!check(addresses, { 'test user has an address': (r) => r.status === 200 && !!addressId })) return fail();

  http.del(`${baseUrl}/carts/items`, null, { headers });
  const cartResponse = http.post(
    `${baseUrl}/carts/items`,
    JSON.stringify({ productId: 1, size: '256GB', quantity: 1 }),
    { headers }
  );
  const cart = body(cartResponse);
  const item = cart.items?.[0];
  if (!check(cartResponse, { 'cart item added': (r) => r.status === 200 && !!item })) return fail();

  const orderStarted = Date.now();
  const orderResponse = http.post(
    `${baseUrl}/orders`,
    JSON.stringify({
      addressId,
      paymentMethod: 'VNPAY',
      cartItemIds: [item.id],
      cartVersion: cart.version,
      expectedTotalDiscountedPrice: (item.discountedPrice || item.price) * item.quantity,
    }),
    { headers }
  );
  feignPathLatency.add(Date.now() - orderStarted, { path: 'order' });
  const orderId = body(orderResponse).orders?.[0]?.id;
  const orderOk = check(orderResponse, { 'order created': (r) => (r.status === 200 || r.status === 201) && !!orderId });
  feignPathSuccess.add(orderOk);
  if (!orderOk) return fail();

  const paymentStarted = Date.now();
  const paymentResponse = http.post(`${baseUrl}/orders/${orderId}/payments`, null, { headers });
  feignPathLatency.add(Date.now() - paymentStarted, { path: 'payment' });
  const paymentUrl = body(paymentResponse).paymentUrl;
  const txnRef = query(paymentUrl, 'vnp_TxnRef');
  const amount = query(paymentUrl, 'vnp_Amount');
  const paymentOk = check(paymentResponse, { 'VNPay URL created': (r) => r.status === 200 && !!txnRef });
  feignPathSuccess.add(paymentOk);
  if (!paymentOk) return fail();

  // Synthetic callback: tests this application's callback/event path, not VNPay itself.
  const callback = http.get(`${baseUrl}/payment-providers/vnpay/callback?vnp_TxnRef=${encodeURIComponent(txnRef)}&vnp_Amount=${amount}&vnp_ResponseCode=00&vnp_TransactionNo=k6-${Date.now()}`);
  if (!check(callback, { 'payment callback accepted': (r) => r.status === 200 && body(r).success === true })) return fail();

  const started = Date.now();
  while (Date.now() - started < 10000) {
    const order = http.get(`${baseUrl}/orders/${orderId}`, { headers });
    const value = body(order);
    if (value.paymentStatus === 'COMPLETED' && value.orderStatus === 'CONFIRMED') {
      rabbitmqPaymentToOrderLatency.add(Date.now() - started);
      rabbitmqPaymentToOrderSuccess.add(true);
      checkoutSuccess.add(true);
      return;
    }
    sleep(0.2);
  }
  rabbitmqPaymentToOrderSuccess.add(false);
  checkoutSuccess.add(false);
}

function json(token) {
  return { headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) } };
}

function body(response) {
  try { return JSON.parse(response.body); } catch (_) { return {}; }
}

function query(url, key) {
  const match = String(url).match(new RegExp(`[?&]${key}=([^&]+)`));
  return match ? decodeURIComponent(match[1]) : null;
}

function fail() {
  checkoutSuccess.add(false);
  sleep(0.2);
}
