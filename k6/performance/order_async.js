import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';
import {
  BASE_URL, BASE_USER_ID, auth, body, checkoutSuccess, consumed, observationSuccess, propagationLatency, produced,
  syncPathLatency, technicalSuccess, thresholds, userId,
} from './common.js';

export const options = {
  scenarios: {
    load: {
      executor: __ENV.EXECUTOR || 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 10),
      timeUnit: '1s',
      duration: __ENV.DURATION || '30s',
      preAllocatedVUs: Number(__ENV.PRE_VUS || 200),
      maxVUs: Number(__ENV.MAX_VUS || 1000),
    },
  },
  thresholds: thresholds(),
};

export default function () {
  const id = userId(exec.scenario.iterationInTest);
  const headers = auth(id);
  const started = Date.now();
  const response = http.post(`${BASE_URL}/orders`, JSON.stringify({
    addressId: id,
    paymentMethod: 'COD',
    cartItemIds: [id],
    cartVersion: 0,
    expectedTotalDiscountedPrice: price(id),
  }), { headers, tags: { path: 'order_producer' } });
  syncPathLatency.add(Date.now() - started);
  const orderId = body(response).orders?.[0]?.id;
  const accepted = check(response, { 'order accepted': (r) => r.status === 201 && !!orderId });
  technicalSuccess.add(accepted);
  checkoutSuccess.add(accepted);
  if (!accepted) return;
  produced.add(1);

  if (exec.scenario.iterationInTest % 10 !== 0) return;
  const waitStarted = Date.now();
  while (Date.now() - waitStarted < 10000) {
    const order = body(http.get(`${BASE_URL}/orders/${orderId}`, { headers, tags: { path: 'observer' } }));
    if (order.orderStatus === 'CONFIRMED' || order.orderStatus === 'CANCELLED') {
      propagationLatency.add(Date.now() - waitStarted);
      consumed.add(1);
      observationSuccess.add(true);
      return;
    }
    sleep(0.1);
  }
  observationSuccess.add(false);
}

function price(id) {
  return [32890600, 20001300, 18051400, 18911400, 60191400, 30020900, 3495080, 1295190, 496290, 7461700][(id - BASE_USER_ID) % 10];
}
