Markdown
<div align="center">

# 🔒 Kemini Messaging API
### *Encrypted, real-time, in transit*

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-Reactive-DC382D?style=for-the-badge&logo=redis)](https://redis.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database-47A248?style=for-the-badge&logo=mongodb)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

<p align="center">
  <strong>Kemini</strong> is a high-performance, enterprise-grade public WebSocket API designed for zero-trust, end-to-end encrypted real-time messaging. Built on a reactive, resilient architecture, Kemini guarantees that every frame is securely sealed with cryptographic precision before it ever leaves the network socket.
</p>

[Features](#-key-architectural-features) • [Tech Stack](#-enterprise-tech-stack) • [Quickstart](#-quickstart--local-testing) • [Architecture](#%EF%B8%8F-system-architecture--infrastructure) • [API Documentation](#-api-documentation)

---

## 🚀 Key Architectural Features

| Feature | Description | Core Mechanism |
| :--- | :--- | :--- |
| **End-to-End Encryption** | Messages are sealed prior to transport. Plaintext never touches server logs. | `BouncyCastle (bcprov-jdk18on)` |
| **Reactive WebSockets** | Bi-directional streaming infrastructure handling highly concurrent persistent frames. | `Spring WebFlux` & `Reactive Redis` |
| **Distributed Rate Limiting** | Dynamic bucket token refills running at the socket layer to block malicious volumetric flows. | `Bucket4j` + `Redis` Integration |
| **Fault Isolation** | Active circuit breakers, retries, and rate structures preventing cascading thread failures. | `Resilience4j` |
| **Background Orchestration** | Transparent, non-blocking distributed task processing and guaranteed mail dispatches. | `JobRunr` |
| **Zero-Downtime Migrations** | Code-first database evolution safely maintaining distributed Mongo data states. | `Mongock` |

---

## 🛠️ Enterprise Tech Stack

Kemini utilizes a heavily optimized, asynchronous runtime environment structured via the following production dependencies:

```xml
- Spring Boot Starter Web / WebFlux / WebSocket
- Spring Security Messaging (Secure WebSocket STOMP frames)
- Spring Boot Starter AMQP (RabbitMQ message broker routing)

- BouncyCastle Crypto Provider (Advanced Cipher suites)
- JJWT (JSON Web Token generation, runtime parsing, and validation)

- Bucket4j (Core & Redis-backed distributed rate limiters)
- Resilience4j Spring Boot 3 Starter (Resilient Circuit Breakers)
- Spring Boot Starter Data Redis Reactive (Non-blocking cache & state management)

- Spring Boot Starter Data MongoDB (Document persistence)
- Mongock (Automated MongoDB data migrations)
- JobRunr Spring Boot 3 Starter (Distributed background processing engine)
- Cloudinary HTTP5 (High-performance secure binary attachment uploads)
⏱️ Quickstart & Local Testing
The public test socket infrastructure exposes a default pipeline boundary locally on port 8989.

1. Set Up Environment
Create a .env file in your root environment path powered by spring-dotenv:

Code snippet
PORT=8989
MONGO_URI=mongodb://localhost:27017/kemini
REDIS_HOST=localhost
REDIS_PORT=6379
CLOUDINARY_URL=cloudinary://api_key:api_secret@cloud_name
2. Establish Connection Engine
Point your WebSocket pipeline directly to the local broker stream handler:

JavaScript
// connect.js
const socket = new WebSocket("ws://localhost:8989");

socket.onopen = () => {
  console.log("Connection securely opened with Kemini Engine.");
  socket.send(JSON.stringify({
    type: "message",
    body: "hello, kemini" // Automatically encrypted in-transit
  }));
};

socket.onmessage = (event) => {
  console.log("Secure frame received:", event.data);
};
⚙️ System Architecture & Infrastructure
                                     +------------------------------------------+
                                     |           Kemini Gateway Engine          |
                                     |                (Port 8989)               |
                                     +------------------------------------------+
                                                          |
                                      +-------------------+-------------------+
                                      |                                       |
                                      v                                       v
                        +----------------------------+          +---------------------------+
                        |  Reactive WebSocket Stack  |          |   Spring Security Layer   |
                        |   (Spring WebFlux/AMQP)    |          |  (BouncyCastle / JJWT)    |
                        +----------------------------+          +---------------------------+
                                      |                                       |
          +---------------------------+---------------------------+           |
          |                           |                           |           |
          v                           v                           v           v
+-------------------+       +-------------------+       +---------------------------+
| Bucket4j + Redis  |       |   Resilience4j    |       |      JobRunr Workers      |
| (Rate Limiting)   |       | (Circuit Breaker) |       |  (Background Tasks/Mail)  |
+-------------------+       +-------------------+       +---------------------------+
          |                                                           |
          +-------------------------------+---------------------------+
                                          |
                                          v
                               +---------------------+
                               |   MongoDB Storage   |
                               |  (Mongock Managed)  |
                               +---------------------+
Cryptographic Foundation
Utilizing BouncyCastle, frames transit across the WebSocket payload network encrypted. The system completely sanitizes logs against accidental data visibility by structural reflection processing (jackson-databind) mapping custom jsr310 temporal objects directly inside isolated encryption models.

Rate Limiting & High Availability
Distributed thread access policies are monitored seamlessly by combining Bucket4j with a reactive Redis topology. Users are shielded against downstream service isolation collapses via Resilience4j failure tracking metrics.

Asynchronous Operations
Time-consuming workloads such as transactional confirmation dispatches (spring-boot-starter-mail) and asset uploads via Cloudinary are automatically decoupled from the active WebSocket frame engine using JobRunr distributed background workers.

📖 API Documentation
Kemini features fully compliant Open API generation components rendering runtime updates on target application ports.

Local Workspace View: http://localhost:8989/swagger-ui/index.html?urls.primaryName=public

Interactive Live Sandbox Platform: Kemini Swagger Dashboard
</div>