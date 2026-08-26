import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';
import {
  BASE_URL, BASE_USER_ID, acceptedRequests, auth, body, syncPathLatency, technicalSuccess, thresholds, userId,
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
  thresholds: {
    ...thresholds(),
    synchronous_path_latency: ['p(95)<=2000'],
  },
};

export default function () {
  const id = userId(exec.scenario.iterationInTest);
  const headers = auth(id);
  const started = Date.now();
  const response = http.post(`${BASE_URL}/orders`, JSON.stringify({
    addressId: id,
    paymentMethod: 'VNPAY',
    cartItemIds: [id],
    cartVersion: 0,
    expectedTotalDiscountedPrice: price(id),
  }), { headers, tags: { path: 'order_producer' } });
  syncPathLatency.add(Date.now() - started);
  const orderId = body(response).orders?.[0]?.id;
  const accepted = check(response, { 'order accepted': (r) => r.status === 201 && !!orderId });
  technicalSuccess.add(accepted);
  if (!accepted) return;
  acceptedRequests.add(1);
}

function price(id) {
  return [32890600, 20001300, 18051400, 18911400, 60191400, 30020900, 3495080, 1295190, 496290, 7461700][(id - BASE_USER_ID) % 10];
}
