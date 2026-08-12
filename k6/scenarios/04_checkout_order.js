/**
 * Scenario 04: Full E-Commerce Checkout & Payment Flow
 * Purpose: Write-heavy transactional workflow test across Auth, Cart, Order, and Payment microservices.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { CONFIG } from '../config/environments.js';
import { login, getAuthHeaders, getOrCreateAddress } from '../utils/auth-helper.js';
import { PRODUCT_SIZES, getRandomElement } from '../data/test-data.js';
import { metrics } from '../utils/metrics.js';

export const options = {
  stages: [
    { duration: '30s', target: 5 },   // Ramp-up
    { duration: '2m', target: 20 },   // Sustained checkout load
    { duration: '30s', target: 0 },   // Ramp-down
  ],
  thresholds: {
    kyro_order_req_duration: [CONFIG.THRESHOLDS.ORDER_DURATION_P95],
    kyro_order_success_rate: ['rate>0.95'],
    kyro_payment_success_rate: ['rate>0.95'],
  },
};

export default function () {
  let token = null;
  let addressId = 1;
  let cart;
  let createdOrderId = null;
  let paymentTxnRef = null;

  group('01. Authenticate User', function () {
    const session = login();
    if (session) {
      token = session.token;
    }
  });

  if (!token) {
    sleep(1);
    return;
  }

  const headers = getAuthHeaders(token);

  group('02. Fetch/Create Shipping Address', function () {
    addressId = getOrCreateAddress(token);
    check(addressId, {
      'valid addressId available': (id) => id !== null && id !== undefined,
    });
  });

  group('03. Populate Cart with Items', function () {
    const addUrl = `${CONFIG.BASE_URL}/api/v1/carts/items`;
    const payload = JSON.stringify({
      productId: Math.floor(Math.random() * 5) + 1,
      size: getRandomElement(PRODUCT_SIZES),
      quantity: 1,
    });

    const res = http.post(addUrl, payload, { headers });
    if (res.status === 200) {
      cart = JSON.parse(res.body);
    }
    check(res, {
      'item added to cart': (r) => r.status === 200,
    });
  });

  sleep(0.5);

  group('04. Place Order (Checkout)', function () {
    const orderUrl = `${CONFIG.BASE_URL}/api/v1/orders`;
    const item = cart && cart.items && cart.items[0];
    if (!item) {
      return;
    }
    
    const start = Date.now();
    const res = http.post(orderUrl, JSON.stringify({
      addressId,
      paymentMethod: 'VNPAY',
      cartItemIds: [item.id],
      cartVersion: cart.version,
      expectedTotalDiscountedPrice: (item.discountedPrice || item.price) * item.quantity,
    }), { headers });
    metrics.orderDuration.add(Date.now() - start);

    const ok = check(res, {
      'order creation returns 201 Created': (r) => r.status === 201 || r.status === 200,
      'returns order object': (r) => {
        try {
          const body = JSON.parse(r.body);
          if (body && body.orders && body.orders.length > 0) {
            createdOrderId = body.orders[0].id;
            return true;
          }
          return false;
        } catch (e) {
          return false;
        }
      },
    });
    metrics.orderSuccessRate.add(ok);
  });

  if (!createdOrderId) {
    sleep(1);
    return;
  }

  sleep(0.5);

  group('05. Create VNPay Payment URL', function () {
    const payUrl = `${CONFIG.BASE_URL}/api/v1/orders/${createdOrderId}/payments`;
    
    const start = Date.now();
    const res = http.post(payUrl, null, { headers });
    metrics.paymentDuration.add(Date.now() - start);

    const ok = check(res, {
      'payment URL creation returns 200': (r) => r.status === 200,
      'contains paymentUrl field': (r) => {
        try {
          const body = JSON.parse(r.body);
          const match = body?.paymentUrl?.match(/[?&]vnp_TxnRef=([^&]+)/);
          paymentTxnRef = match ? decodeURIComponent(match[1]) : null;
          return body?.success === true && paymentTxnRef !== null;
        } catch (e) {
          return false;
        }
      },
    });
    metrics.paymentSuccessRate.add(ok);
  });

  if (!paymentTxnRef) {
    sleep(1);
    return;
  }

  sleep(0.5);

  group('06. Simulate VNPay Callback Webhook (IPN)', function () {
    const callbackUrl = `${CONFIG.BASE_URL}/api/v1/payment-providers/vnpay/callback?vnp_TxnRef=${encodeURIComponent(paymentTxnRef)}&vnp_ResponseCode=00&vnp_Amount=50000000&vnp_TransactionNo=14000000`;

    const start = Date.now();
    const res = http.get(callbackUrl, { headers: CONFIG.HEADERS.JSON });
    metrics.paymentDuration.add(Date.now() - start);

    const ok = check(res, {
      'VNPay callback processed successfully': (r) => r.status === 200,
      'callback response indicates success': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body && body.success === true;
        } catch (e) {
          return false;
        }
      },
    });
    metrics.paymentSuccessRate.add(ok);
  });

  sleep(0.5);

  group('07. Query Updated Order Status', function () {
    const getOrderUrl = `${CONFIG.BASE_URL}/api/v1/orders/${createdOrderId}`;

    const start = Date.now();
    const res = http.get(getOrderUrl, { headers });
    metrics.orderDuration.add(Date.now() - start);

    const ok = check(res, {
      'order details retrieved': (r) => r.status === 200,
    });
    metrics.orderSuccessRate.add(ok);
  });

  sleep(1);
}
