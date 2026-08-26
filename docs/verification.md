# Backend Verification Report

## Result

The Java backend passed its source, Docker, and measured performance gates on 2026-08-25 (Asia/Ho_Chi_Minh). These figures describe one local development machine; they are not production capacity claims.

The verified working tree is based on commit `06092cab295f`. Replace this provenance line with the final commit SHA after the reviewed changes and evidence are committed.

## Environment

| Component | Value |
| --- | --- |
| Host | MacBook Pro, Apple M4 (10 cores), 24 GB RAM |
| Docker allocation | 4 vCPU, 8 GB RAM |
| Java used for verification | Microsoft OpenJDK 21.0.8 LTS |
| Maven | 3.9.9 via Maven Wrapper |
| Docker Engine | 28.4.0 |
| Docker Compose | 2.39.2-desktop.1 |
| k6 | 1.7.1, darwin/arm64 |

## Source and Docker verification

The following checks passed:

```bash
./mvnw spotless:check
./mvnw clean verify
docker compose config --quiet
task clean
task run
task status
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/api/v1/categories
curl -fsS http://localhost:8080/catalog-service/v3/api-docs
```

`clean verify` ran 68 tests with zero failures or errors. The fresh Compose environment reported twelve healthy containers plus Dozzle running. Gateway health, the public Catalog API, and proxied OpenAPI returned successfully. The implemented categories path does not accept a trailing slash.

## Method

Each scenario used isolated SQL fixture users and locally signed JWTs. A run passed only with at least 99% technical success, p95 at or below 2 seconds, zero dropped iterations, queue backlog drained to zero, and the expected database final state.

The Feign capacity runs measured an already-warm service, which is the normal steady-state load-test scope. Docker was stabilized first and checkout traffic preconditioned the JVM, connection pools, and database working set. That conditioning traffic is excluded from the tables. The fixture was reset to the same logical state before every measured 60-second run. Three measured runs then requested 250 iterations per second on the same warm stack.

Fresh-boot probes with only the default 10-second warm-up, or a 50 req/s 30-second warm-up, were highly variable and are not capacity evidence. A diagnostic `address(user_id)` index reduced one Auth lookup from a 5.147 ms sequential scan to a 0.045 ms index scan, but it was dropped before all three published runs. The published results therefore use the repository schema without a temporary index.

The Feign scenario creates VNPAY orders so SMTP delivery is outside the measured path. The RabbitMQ scenario sends locally generated VNPay callbacks signed with HMAC-SHA512. It polls 10% of callbacks for propagation latency and verifies the final database state for all accepted callbacks after each run.

## Feign order path

Path: Gateway → Order → Cart/Auth, followed by asynchronous stock reservation.

| Run | Accepted | Actual throughput | Sync p95 | Success | Dropped | Peak backlog | After drain | DB result |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | 15,001 | 249.909 req/s | 539 ms | 100% | 0 | 16,958 | 0 | 15,001 orders reserved |
| 2 | 15,001 | 249.945 req/s | 466 ms | 100% | 0 | 9,910 | 0 | 15,001 orders reserved |
| 3 | 15,001 | 249.927 req/s | 427 ms | 100% | 0 | 4,348 | 0 | 15,001 orders reserved |

Conservative warm-capacity headline: **249.909 req/s, 539 ms p95, 100% success, zero dropped iterations, and zero backlog after drain**.

Evidence: [runs CSV](../k6/results/feign/runs.csv), [run 1](../k6/results/feign/feign-run-1.json), [run 2](../k6/results/feign/feign-run-2.json), [run 3](../k6/results/feign/feign-run-3.json), and the corresponding [run 1 DB verification](../k6/results/feign/feign-run-1-verify.csv), [run 2 DB verification](../k6/results/feign/feign-run-2-verify.csv), and [run 3 DB verification](../k6/results/feign/feign-run-3-verify.csv).

### Capacity boundary

An incremental warm-stack sweep was run after the verified 250 req/s baseline. These probes are boundary evidence, not headline measurements:

| Probe | Requested | Actual throughput | Sync p95 | Success | Dropped | After drain | Gate |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 275 initial | 275 req/s | 274.895 req/s | 384 ms | 100% | 0 | 0 | Pass |
| 275 repeat | 275 req/s | 251.871 req/s | 4.38 s | 100% | 486 | 0 | Fail |
| 276 | 276 req/s | 246.402 req/s | 5.24 s | 100% | 665 | 0 | Fail |

The initial 275 req/s probe was not repeatable, so it is not presented as stable capacity. The system shows a sharp saturation cliff rather than a gradual error-rate increase: accepted requests remain technically correct and queues eventually drain, while latency and dropped iterations breach the gates. The defensible result remains **250 req/s verified across three measured runs**, with instability demonstrated at 275 req/s and failure at 276 req/s.

Boundary evidence: [runs CSV](../k6/results/feign-boundary/runs.csv), [275 pass](../k6/results/feign-boundary/feign-275-probe-pass.json), [275 repeat failure](../k6/results/feign-boundary/feign-275-repeat-fail.json), and [276 failure](../k6/results/feign-boundary/feign-276-fail.json). Each probe also has a corresponding DB verification CSV.

## RabbitMQ payment propagation

Path: signed VNPay callback → Payment → RabbitMQ → Order payment projection.

| Run | Accepted | Actual throughput | Propagation p95 | Technical success | Observation success | Dropped | Peak backlog | After drain | DB result |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | 3,001 | 50.005 req/s | 112 ms | 100% | 100% | 0 | 1 | 0 | 3,001 completed |
| 2 | 3,000 | 49.997 req/s | 16.05 ms | 100% | 100% | 0 | 1 | 0 | 3,000 completed |
| 3 | 3,000 | 49.997 req/s | 20.75 ms | 100% | 100% | 0 | 1 | 0 | 3,000 completed |

Conservative headline: **49.997 req/s, 112 ms p95, 100% technical and observation success, zero dropped iterations, and zero backlog after drain**.

Evidence: [runs CSV](../k6/results/rabbitmq/runs.csv), [run 1](../k6/results/rabbitmq/rabbitmq-run-1.json), [run 2](../k6/results/rabbitmq/rabbitmq-run-2.json), [run 3](../k6/results/rabbitmq/rabbitmq-run-3.json), and the corresponding [run 1 DB verification](../k6/results/rabbitmq/rabbitmq-run-1-verify.csv), [run 2 DB verification](../k6/results/rabbitmq/rabbitmq-run-2-verify.csv), and [run 3 DB verification](../k6/results/rabbitmq/rabbitmq-run-3-verify.csv).

## Payment failure check

The auxiliary failure-path run accepted 100 signed callbacks at 9.998 req/s with 100% technical and observation success, 118.95 ms propagation p95, zero dropped iterations, and zero backlog after drain. Database verification found exactly 100 `FAILED` Payment records and 100 matching failed Order payment projections.

Evidence: [summary JSON](../k6/results/payment-failure/payment-failure.json), [runs CSV](../k6/results/payment-failure/runs.csv), and [DB verification](../k6/results/payment-failure/payment-failure-verify.csv).

## Limitations

- Results are local, single-replica measurements with Docker limited to 4 vCPU and 8 GB RAM.
- The 250 req/s Feign result is warm steady-state capacity, not cold-start capacity. A short warm-up after a fresh Docker boot did not reproduce it reliably.
- Boundary probes are intentionally single-run diagnostics. The 275 req/s probe passed once and failed on repeat, so it is evidence of instability rather than a capacity claim.
- The runs are 60 seconds long and do not establish long-duration capacity or production SLOs.
- VNPay traffic is synthetic and correctly signed, but does not include the real VNPay network.
- External AI and SMTP delivery are outside the benchmark scope.
- Feign latency is the complete synchronous order path, not an isolated Feign client timer.
- RabbitMQ latency polling samples 10% of callbacks; post-run database verification covers every accepted callback.
- The Auth address ownership lookup has no `address(user_id)` index. A migration for that index is a measured optimization opportunity, but schema changes were outside this verification scope.
