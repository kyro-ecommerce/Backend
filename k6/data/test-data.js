/**
 * Test Data Generator & Mocks for Kyro k6 Load Tests
 */

// Preset test accounts (matches database seed or dynamic generation)
export const SEED_USERS = [
  { email: 'customer@kyro.com', password: 'Password123!' },
  { email: 'admin@kyro.com', password: 'Password123!' },
];

export const PRODUCT_SIZES = ['S', 'M', 'L', 'XL', 'XXL'];

export const CATEGORIES = {
  TOP: ['clothing', 'electronics', 'footwear', 'accessories'],
  SECOND: ['mens_tshirts', 'womens_dresses', 'jackets', 'sneakers'],
};

export const SEARCH_KEYWORDS = [
  'shirt',
  'tshirt',
  'dress',
  'shoes',
  'jacket',
  'black',
  'cotton',
  'denim',
];

export const AI_CHAT_PROMPTS = [
  'Gợi ý cho tôi áo sơ mi nam phù hợp đi dự tiệc',
  'Tìm sản phẩm áo phông màu đen chất liệu cotton tốt',
  'Tôi muốn mua quà tặng cho nữ dưới 500k',
];

/**
 * Generate unique random email per VU / Iteration
 */
export function generateRandomUser() {
  const timestamp = Date.now();
  const randomStr = Math.random().toString(36).substring(2, 8);
  return {
    email: `k6_user_${timestamp}_${randomStr}@example.com`,
    password: 'Password123!',
    firstName: `K6User_${randomStr}`,
    lastName: 'Tester',
  };
}

/**
 * Generate mock shipping address payload
 */
export function generateAddressPayload() {
  return {
    streetAddress: '123 Nguyen Hue Street',
    city: 'Ho Chi Minh',
    state: 'District 1',
    zipCode: '700000',
    mobile: '0987654321',
  };
}

/**
 * Pick random element from array
 */
export function getRandomElement(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

/**
 * Edge Case Payloads for Resilience Testing
 */
export const EDGE_CASE_DATA = {
  INVALID_TOKENS: [
    'Bearer invalid_token_xyz_123',
    'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.invalid_signature',
    'Bearer ',
    'MalformedTokenFormat',
  ],
  INVALID_EMAILS: [
    'not-an-email',
    'missingdomain@.com',
    'user@domain..com',
  ],
  MALFORMED_JSON_STRINGS: [
    '{"email": "test@example.com", "password": }',
    '{"productId": 1, "size": "M"',
  ],
  OUT_OF_BOUND_IDS: [
    9999999,
    -1,
    0,
  ],
  INVALID_QUANTITIES: [
    -5,
    0,
    999999,
  ],
};
