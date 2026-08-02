/**
 * Kyro E-Commerce Load Testing Configuration
 * Defines base target URLs, thresholds, and performance SLAs across services.
 */

export const CONFIG = {
  // Primary gateway endpoint for all microservices
  BASE_URL: __ENV.BASE_URL || 'http://localhost:8080',
  
  // AI FastAPI service endpoint (either via Gateway or direct)
  AI_SERVICE_URL: __ENV.AI_SERVICE_URL || 'http://localhost:8080/api/v1/ai',

  // System Performance SLA Thresholds
  THRESHOLDS: {
    // 95% of all HTTP requests must complete within 500ms
    HTTP_REQ_DURATION_P95: 'p(95)<500',
    // 99% of all HTTP requests must complete within 1500ms
    HTTP_REQ_DURATION_P99: 'p(99)<1500',
    // Global HTTP error rate must be under 1%
    HTTP_REQ_FAILED_RATE: 'rate<0.01',
    // Gateway Auth processing SLA
    AUTH_DURATION_P95: 'p(95)<400',
    // Redis Cart operations SLA
    CART_DURATION_P95: 'p(95)<200',
    // DB Order Checkout SLA
    ORDER_DURATION_P95: 'p(95)<800',
  },

  // Default headers
  HEADERS: {
    JSON: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
  },
};
