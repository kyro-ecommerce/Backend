CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_created_id ON product (created_at DESC, id DESC);
CREATE INDEX idx_variant_active_price ON product_variant (product_id, active, price);
CREATE INDEX idx_review_product ON review (product_id);
