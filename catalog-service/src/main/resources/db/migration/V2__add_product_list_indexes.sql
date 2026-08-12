CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_created_id ON product (created_at DESC, id DESC);
CREATE INDEX idx_product_discounted_price_id ON product (discounted_price, id);
