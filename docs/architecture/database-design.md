# Database Design & Storage Schema

Kyro uses one PostgreSQL 16 instance with four isolated databases. Each service owns its own
schema and accesses another service's data only through its API or events; there are no
cross-database foreign keys or joins.

| Service | Storage | Owns |
|---|---|---|
| Auth | PostgreSQL `kyro_auth` | `role`, `users`, `address` |
| Catalog | PostgreSQL `kyro_catalog` | `category`, `product`, `image`, `sizes`, `review` |
| Cart | Redis 7 | `cart:{userId}` with 30-day TTL |
| Order | PostgreSQL `kyro_order` | `order_address`, `orders`, `order_item` |
| Payment | PostgreSQL `kyro_payment` | `payment_details`, VNPay transaction data |

## Ownership rules

- Foreign keys are used only inside one database, such as `product.category_id` and
  `order_item.order_id`.
- `review.user_id`, `orders.user_id`, `order_item.product_id`, and `payment_details.order_id`
  are external references. The owning service validates or retrieves them through Feign APIs.
- Cart data is stored only by Cart Service in Redis; Order Service reads it through Cart Service
  during checkout.
- Payment Service owns VNPay transaction details. Order Service keeps only payment method and
  payment status needed for its own order lifecycle.
- Cloudinary holds binary image files. Catalog's `image` table holds the product relationship,
  URL, filename, and content type.

## Migration

Flyway creates the PostgreSQL schemas from `V1__init.sql`; Hibernate runs with `ddl-auto: validate`.
For the development reset, recreate the local Docker volume so the revised initial schema is applied.
