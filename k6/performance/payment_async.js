import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';
import {
  BASE_URL, PAYMENT_ORDER_ID, auth, body, checkoutSuccess, consumed, observationSuccess, propagationLatency,
  produced, technicalSuccess, thresholds, userId,
} from './common.js';

export const options = {
  scenarios: {
    load: {
      executor: 'constant-arrival-rate',
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
  const offset = exec.scenario.iterationInTest;
  const orderId = PAYMENT_ORDER_ID + offset;
  const responseCode = __ENV.RESPONSE_CODE || '00';
  const expectedStatus = __ENV.EXPECTED_PAYMENT_STATUS || (responseCode === '00' ? 'COMPLETED' : 'FAILED');
  const headers = auth(userId(offset));
  const response = http.get(
    `${BASE_URL}/payment-providers/vnpay/callback?vnp_TxnRef=perf-${orderId}&vnp_ResponseCode=${responseCode}&vnp_Amount=10000&vnp_TransactionNo=k6-${orderId}`,
    { tags: { path: 'payment_producer' } }
  );
  const accepted = check(response, { 'callback accepted': (r) => r.status === 200 && body(r).success === (responseCode === '00') });
  technicalSuccess.add(accepted);
  checkoutSuccess.add(accepted);
  if (!accepted) return;
  produced.add(1);

  if (offset % 10 !== 0) return;
  const started = Date.now();
  while (Date.now() - started < 10000) {
    const order = body(http.get(`${BASE_URL}/orders/${orderId}`, { headers, tags: { path: 'observer' } }));
    if (order.paymentStatus === expectedStatus) {
      propagationLatency.add(Date.now() - started);
      consumed.add(1);
      observationSuccess.add(true);
      return;
    }
    sleep(0.1);
  }
  observationSuccess.add(false);
}
