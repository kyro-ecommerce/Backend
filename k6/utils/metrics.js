/**
 * Custom k6 Metrics for Microservice Level Granularity
 */
import { Trend, Rate, Counter } from 'k6/metrics';

// Latency Trends per Microservice
export const metrics = {
  // Auth Service
  authDuration: new Trend('kyro_auth_req_duration', true),
  authSuccessRate: new Rate('kyro_auth_success_rate'),

  // Catalog Service
  catalogDuration: new Trend('kyro_catalog_req_duration', true),
  catalogSuccessRate: new Rate('kyro_catalog_success_rate'),

  // Cart Service (Redis)
  cartDuration: new Trend('kyro_cart_req_duration', true),
  cartSuccessRate: new Rate('kyro_cart_success_rate'),

  // Order Service (Postgres)
  orderDuration: new Trend('kyro_order_req_duration', true),
  orderSuccessRate: new Rate('kyro_order_success_rate'),

  // Payment Service (VNPay integration)
  paymentDuration: new Trend('kyro_payment_req_duration', true),
  paymentSuccessRate: new Rate('kyro_payment_success_rate'),

  // AI Service (FastAPI + pgvector)
  aiDuration: new Trend('kyro_ai_req_duration', true),
  aiSuccessRate: new Rate('kyro_ai_success_rate'),

  // Global Error Counters
  http4xxErrors: new Counter('kyro_http_4xx_count'),
  http5xxErrors: new Counter('kyro_http_5xx_count'),
};
