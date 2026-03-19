# HFT E-Rates Platform

A modular, event-driven trading sandbox that demonstrates how premium treasury rates can be streamed, priced, executed, and recorded end-to-end. The system is composed of Spring Boot microservices, a React/Vite client, and a Kafka/Postgres backbone orchestrated with Docker Compose.

## Table of contents
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Run with Docker Compose](#run-with-docker-compose)
- [Local development workflows](#local-development-workflows)
- [Testing](#testing)
- [Kafka topics](#kafka-topics)
- [Troubleshooting](#troubleshooting)

## Architecture
```
┌──────────────┐     RAW RATES      ┌──────────────────┐      PRICED RATES      ┌─────────────────────┐
│ Market Data  │ ─────────────────▶ │  Pricing Engine  │ ─────────────────────▶ │ WebSocket Gateway   │ ──▶ React UI
└──────────────┘                    └──────────────────┘                         │ (STOMP over /ws)    │
                                                                                 └─────────────────────┘
                                                                                           │
                                                                                           ▼
                                                                                Trade Requests / Responses
                                                                                           │
                                                                                           ▼
                   ┌──────────────────────┐   LEDGER REQUESTS   ┌─────────────────────┐
                   │ Execution Service    │ ───────────────────▶ │ Ledger Service      │ ──▶ Postgres ledger
                   └──────────────────────┘                      └─────────────────────┘
```

| Component | Purpose | Tech | Default Port |
| --- | --- | --- | --- |
| `common-lib` | Shared DTOs (`RateTick`, `TradeRequest`, `TradeResponse`) | Java 21 | n/a |
| `pricing-engine` | Applies client spreads to raw ticks and republishes priced rates | Spring Boot, Kafka | n/a |
| `websocket-gateway` | Bridges Kafka topics to WebSocket/STOMP channels and accepts trade requests | Spring Boot, SockJS/STOMP | 8081 |
| `execution-service` | Screens trade requests (limits, validation) and forwards accepted orders to the ledger | Spring Boot, Kafka | n/a |
| `ledger-service` | Persists confirmed trades into Postgres and publishes final responses | Spring Boot, Kafka, Postgres | n/a |
| `client-ui` | React dashboard showing live rates, blotter, and trade entry | React 18, Vite, STOMP | 5173 (dev) / 80 (container) |
| Infra | Kafka, Zookeeper, Postgres, Redis (optional caching), Docker Compose | Containers | multiple |

## Tech stack
- **Backend:** Java 21, Spring Boot 3/4, Spring Kafka, Spring WebSocket, Spring Data JPA
- **Frontend:** React 18, Vite, STOMP.js, Lucide icons
- **Data & messaging:** Apache Kafka, Zookeeper, PostgreSQL 15, Redis 7
- **Tooling:** Maven, Node.js ≥ 18, Docker & Docker Compose

## Getting started
1. **Clone the repo**
   ```bash
   git clone https://github.com/Iamsujithd/HFT_E-rates_platform.git
   cd HFT_E-rates_platform
   ```
2. **Install prerequisites**
   - JDK 21
   - Maven 3.9+
   - Node.js 18+ (for the `client-ui`)
   - Docker Desktop (for the full stack / infra services)
3. **Build shared artifacts**
   ```bash
   mvn clean install
   ```
   This compiles every Spring Boot module and installs `common-lib` locally so individual services can run in isolation.

## Run with Docker Compose
The easiest way to see the entire platform:
```bash
docker compose up --build
```
This brings up Zookeeper, Kafka, Postgres, Redis, all Spring Boot services, and the client UI served by Nginx on port `80`. Once the containers are healthy:
- Frontend: <http://localhost>
- WebSocket gateway (health): <http://localhost:8081/actuator/health>
- Kafka broker: `localhost:9092`
- Postgres: `localhost:5432` (user `hft_user`, db `hft_ledger`)

Stop everything with `docker compose down` (add `-v` to drop volumes).

## Local development workflows
### Backend services
Run any service with Spring Boot’s Maven plugin (Kafka/Postgres must already be available—use Docker or local installs):
```bash
# Terminal 1 – shared lib & dependencies
mvn -pl common-lib clean install

# Terminal 2 – pricing engine
mvn -pl pricing-engine spring-boot:run

# Terminal 3 – websocket gateway
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 mvn -pl websocket-gateway spring-boot:run
# (similar for execution-service and ledger-service)
```
Each service exposes Actuator health at `/actuator/health`. Override Kafka or database settings via standard Spring `application.properties` or environment variables (see `docker-compose.yml` for examples).

### Frontend (React + Vite)
```bash
cd client-ui
npm install
npm run dev -- --port 5173
```
The app expects the gateway at `http://localhost:8081/ws`; adjust `GATEWAY_WS_URL` in `src/App.jsx` if you proxy or change ports.

## Testing
- **Backend:** `mvn test` (runs unit tests for all Spring Boot modules). You can scope to a module with `mvn -pl pricing-engine test`.
- **Frontend:** `npm run build` ensures the bundle compiles cleanly (no dedicated test suite yet).

## Kafka topics
| Topic | Who writes | Who reads | Intent |
| --- | --- | --- | --- |
| `raw-rates` | Market data simulator (inside pricing-engine) | Pricing engine | Ingest vendor ticks |
| `priced-rates` | Pricing engine | WebSocket gateway → UI | Client-friendly bid/ask stream |
| `trade-requests` | WebSocket gateway (from `/app/trade`) | Execution service | Validate incoming orders |
| `ledger-requests` | Execution service | Ledger service | Persist accepted trades |
| `trade-responses` | Execution & ledger services | WebSocket gateway → UI | Notify clients of ACCEPTED/REJECTED |

## Troubleshooting
- **`gh repo create …` fails (exit 127):** Install GitHub CLI via `brew install gh` or create the repo via the GitHub UI and add the remote manually.
- **Kafka consumer can’t connect:** Confirm `SPRING_KAFKA_BOOTSTRAP_SERVERS` matches your broker (`localhost:9092` outside Docker, `kafka:29092` inside Compose).
- **Ledger service can’t reach Postgres:** Check the `SPRING_DATASOURCE_*` env vars; inside Compose it targets `postgres:5432`, locally use `jdbc:postgresql://localhost:5432/hft_ledger`.
- **Web UI stuck on “Waiting for streaming data…”** Make sure the pricing engine is running and publishing to Kafka, and that the gateway is connected to the same broker.

Happy trading!
