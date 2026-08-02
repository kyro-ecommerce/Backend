# 🤖 AI Service Documentation (Python FastAPI & pgvector)

> **Service Name**: `ai-service`  
> **Port**: `8000`  
> **Framework**: Python 3.11, FastAPI, SQLAlchemy, Alembic, Uvicorn  
> **Database**: PostgreSQL 16 + `pgvector` extension  
> **AI Provider**: Google Gemini API (`models/embedding-001`)  
> **Messaging**: `pika` / `aio-pika` RabbitMQ Consumer (`ai.product.sync.queue`)

---

## 📌 1. Chức Năng Chính

**AI Service** là dịch vụ thông minh cung cấp khả năng tìm kiếm ngữ nghĩa (**Semantic Search**) và gợi ý sản phẩm (**Vector Recommendation System**):

1. **Đồng Bộ Dữ Liệu Sản Phẩm Bất Đồng Bộ (Async Catalog Sync)**:
   - Lắng nghe sự kiện `product.created`, `product.updated`, `product.deleted` từ RabbitMQ `catalog.exchange`.
   - Tự động cập nhật chỉ mục vector mà không làm chậm `catalog-service`.
2. **Sinh Vector Embedding Với Gemini AI**:
   - Nhận thông tin sản phẩm (Tên + Mô tả + Danh mục), gọi Google Gemini API (`models/embedding-001`) để chuyển đổi văn bản thành **Vector 768 chiều**.
3. **Lưu Trữ & Tìm Kiếm Tối Ưu Với `pgvector`**:
   - Lưu trữ vector trong bảng `product_embeddings` tại database PostgreSQL.
   - Tìm kiếm khoảng cách Cosine Similarity (`<=>`) trực tiếp bằng SQL query:
     ```sql
     SELECT product_id, title, 1 - (embedding <=> :query_vector) AS similarity
     FROM product_embeddings
     ORDER BY similarity DESC
     LIMIT 10;
     ```
4. **API Gợi Ý Sản Phẩm Tương Tự (Product Recommendations)**:
   - Nhận một `productId` hoặc một từ khóa tìm kiếm bằng ngôn ngữ tự nhiên (ví dụ *"áo sơ mi đi biển mùa hè"*), trả về danh sách các sản phẩm liên quan nhất.

---

## 📡 2. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/docs` | Giao diện Swagger Open API tương tác | Public |
| `POST` | `/api/v1/ai/recommend` | Gợi ý các sản phẩm tương tự dựa trên Product ID | Public |
| `POST` | `/api/v1/ai/semantic-search` | Tìm kiếm sản phẩm bằng câu nói / ngôn ngữ tự nhiên | Public |
| `POST` | `/api/v1/ai/sync-product` | Trigger sinh lại vector cho 1 sản phẩm thủ công | Admin |

---

## 🐍 3. Cấu Trúc Mã Nguồn Python (`ai-service/app`)

```text
ai-service/
├── app/
│   ├── main.py              # FastAPI Application Entrypoint
│   ├── config.py            # Environment Variables
│   ├── database.py          # SQLAlchemy Session & pgvector Engine
│   ├── models.py            # SQLAlchemy Model ProductEmbedding
│   ├── schemas.py           # Pydantic Schemas
│   ├── services/
│   │   ├── gemini_service.py# Call Gemini Embedding API
│   │   └── ai_service.py    # Vector Cosine Similarity Search
│   ├── consumers/
│   │   └── catalog_sync.py  # RabbitMQ Event Listener
│   └── api/
│       └── routes.py        # REST Endpoints
├── alembic/                 # Database Migrations
├── Dockerfile
└── requirements.txt
```
