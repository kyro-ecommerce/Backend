/**
 * Kyro E-Commerce Master Load Test Suite
 * Executing multi-scenario traffic distribution representing real user behavior.
 */
import browseScenario from './scenarios/02_browse_catalog.js';
import cartScenario from './scenarios/03_cart_operations.js';
import checkoutScenario from './scenarios/04_checkout_order.js';
import edgeCasesScenario from './scenarios/07_edge_cases.js';
import { CONFIG } from './config/environments.js';

export const options = {
  scenarios: {
    // 70% traffic: Browsing & AI Search
    browse_journey: {
      executor: 'ramping-vus',
      startVUs: 5,
      stages: [
        { duration: '30s', target: 35 },
        { duration: '3m',  target: 70 },
        { duration: '30s', target: 0 },
      ],
      exec: 'runBrowse',
    },
    // 20% traffic: Cart Operations
    cart_journey: {
      executor: 'ramping-vus',
      startVUs: 2,
      stages: [
        { duration: '30s', target: 10 },
        { duration: '3m',  target: 20 },
        { duration: '30s', target: 0 },
      ],
      exec: 'runCart',
    },
    // 10% traffic: Checkout & Payments
    checkout_journey: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '3m',  target: 10 },
        { duration: '30s', target: 0 },
      ],
      exec: 'runCheckout',
    },
    // Edge case resilience checks
    edge_cases: {
      executor: 'constant-vus',
      vus: 2,
      duration: '3m',
      exec: 'runEdgeCases',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: [CONFIG.THRESHOLDS.HTTP_REQ_DURATION_P95],
  },
};

export function runBrowse() {
  browseScenario();
}

export function runCart() {
  cartScenario();
}

export function runCheckout() {
  checkoutScenario();
}

export function runEdgeCases() {
  edgeCasesScenario();
}

/**
 * Custom handleSummary output formatter (Generates JSON summary report)
 */
export function handleSummary(data) {
  return {
    'k6-summary.json': JSON.stringify(data, null, 2),
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
  };
}

function textSummary(data, options) {
  const p95 = data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'].toFixed(2) : 'N/A';
  const p99 = data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(99)'].toFixed(2) : 'N/A';
  const reqs = data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0;
  const rps = data.metrics.http_reqs ? data.metrics.http_reqs.values.rate.toFixed(2) : 0;
  const failRate = data.metrics.http_req_failed ? (data.metrics.http_req_failed.values.rate * 100).toFixed(2) : 0;

  return `
================================================================================
                    🛒 KYRO BACKEND K6 LOAD TEST SUMMARY 🛒
================================================================================
  - Total HTTP Requests Executed : ${reqs}
  - Requests Per Second (RPS)    : ${rps} req/s
  - p95 Response Time           : ${p95} ms
  - p99 Response Time           : ${p99} ms
  - HTTP Failure Rate            : ${failRate}%
================================================================================
  Detailed metric breakdown saved to: k6-summary.json
================================================================================
`;
}
