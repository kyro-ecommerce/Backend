import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';
import {
  BASE_URL, BASE_USER_ID, auth, body, checkoutSuccess, consumed, observationSuccess, propagationLatency,
  produced, query, technicalSuccess, thresholds, userId,
} from './common.js';

const spike = __ENV.MODE === 'spike';
export const options = {
  scenarios: {
    load: spike ? {
      executor: 'per-vu-iterations', vus: Number(__ENV.SPIKE_VUS || 1000), iterations: 1, maxDuration: '2m',
    } : {
      executor: 'constant-arrival-rate', rate: Number(__ENV.RATE || 10), timeUnit: '1s',
      duration: __ENV.DURATION || '30s', preAllocatedVUs: Number(__ENV.PRE_VUS || 200), maxVUs: 1000,
    },
  },
  thresholds: { ...thresholds(), checkout_success_rate: ['rate>=0.95'] },
};

export default function () {
  const offset = spike ? exec.vu.idInTest - 1 : exec.scenario.iterationInTest;
  const id = userId(offset);
  const headers = auth(id);
  const distributed = __ENV.SKU_MODE !== 'contention';
  const sku = distributed ? offset % 10 : 0;
  const response = http.post(`${BASE_URL}/orders`, JSON.stringify({
    addressId: id, paymentMethod: 'VNPAY', cartItemIds: [id], cartVersion: 0,
    expectedTotalDiscountedPrice: [32890600, 20001300, 18051400, 18911400, 60191400, 30020900, 3495080, 1295190, 496290, 7461700][sku],
  }), { headers, tags: { path: 'order' } });
  const orderId = body(response).orders?.[0]?.id;
  if (!check(response, { 'order accepted': (r) => r.status === 201 && !!orderId })) return failed();

  const payment = http.post(`${BASE_URL}/orders/${orderId}/payments`, null, { headers, tags: { path: 'payment' } });
  const paymentUrl = body(payment).paymentUrl;
  const txnRef = query(paymentUrl, 'vnp_TxnRef');
  if (!check(payment, { 'payment URL created': (r) => r.status === 200 && !!txnRef })) return failed();

  const callback = http.get(`${BASE_URL}/payment-providers/vnpay/callback?vnp_TxnRef=${encodeURIComponent(txnRef)}&vnp_ResponseCode=00&vnp_TransactionNo=k6-${orderId}`, { tags: { path: 'callback' } });
  const accepted = check(callback, { 'callback accepted': (r) => r.status === 200 && body(r).success === true });
  technicalSuccess.add(accepted);
  if (!accepted) return checkoutSuccess.add(false);
  produced.add(2);

  const started = Date.now();
  while (Date.now() - started < 10000) {
    const order = body(http.get(`${BASE_URL}/orders/${orderId}`, { headers, tags: { path: 'observer' } }));
    if (order.paymentStatus === 'COMPLETED' && order.orderStatus === 'CONFIRMED') {
      propagationLatency.add(Date.now() - started);
      consumed.add(2);
      observationSuccess.add(true);
      checkoutSuccess.add(true);
      return;
    }
    sleep(0.1);
  }
  observationSuccess.add(false);
  checkoutSuccess.add(false);
}

function failed() {
  technicalSuccess.add(false);
  observationSuccess.add(false);
  checkoutSuccess.add(false);
}
