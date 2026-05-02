# 🧵 Fabric AI — Intelligent Fabric Waste Prediction & Management System

A full-stack web application built for garment manufacturing companies that uses machine learning to predict fabric waste percentage during cutting processes, provides intelligent decision support through a RAG-based chat interface, live dashboards, sustainability tracking, and continuous model monitoring.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [ML Model](#ml-model)
- [Running Tests](#running-tests)

---

## Overview

Fabric waste during garment cutting is one of the largest controllable costs in garment manufacturing. **Fabric AI** provides production teams with a data-driven tool to predict waste before cutting begins, monitor model performance over time, and access an AI assistant trained on domain-specific knowledge — all through a unified web interface.

---

## Features

| Feature | Description |
|---|---|
| 🤖 **Waste Prediction** | Predicts fabric waste % using a trained scikit-learn ML model |
| 💬 **RAG Chat Interface** | AI chat assistant powered by Google Gemini + ChromaDB vector store |
| 📊 **Live Dashboard** | Real-time metrics, KPIs, and prediction history |
| 📈 **Model Monitoring** | R², MAE, residual analysis, and rolling window drift detection |
| 🔄 **Model Retraining** | Manual retraining trigger via API; model only replaced if performance improves |
| 🌿 **Sustainability Metrics** | Tracks fabric saved, CO₂ impact, and waste trends over time |
| 📄 **Report Generation** | Exportable reports and CSV data export |
| 📁 **Historical Data Management** | Full prediction history with actual result logging |

---

## Tech Stack

### Frontend
- **Thymeleaf** — Server-side HTML templating
- **Vanilla JavaScript** — Client-side interactivity
- **Static HTML pages** — Supplementary views

### Backend — Web Layer
- **Spring Boot (Java)** — REST controllers, Thymeleaf rendering, business logic
- **Spring Data JPA / Hibernate** — ORM and database access
- **PostgreSQL** — Primary relational database

### Backend — ML/AI Layer
- **FastAPI (Python)** — ML inference server, retraining endpoint, RAG endpoint
- **scikit-learn** — ML pipeline (trained model stored as `.joblib`)
- **Google Gemini API** (`gemini-2.5-flash`) — LLM for RAG chat responses
- **ChromaDB** — Vector store for RAG document embeddings
- **Gemini Embeddings** (`gemini-embedding-001`) — Document and query embedding model

### Configuration & Tooling
- **Pydantic + python-dotenv** — FastAPI settings management
- **NumPy / Pandas** — Metrics computation for model monitoring
- **joblib** — ML model serialisation

---

## Architecture

The system uses a **dual-service backend architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser / User                        │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│          Spring Boot — Port 8080                             │
│  • Thymeleaf UI rendering                                    │
│  • REST controllers                                          │
│  • JPA / PostgreSQL access                                   │
│  • Calls FastAPI via HTTP for predictions, RAG, retraining   │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP (localhost:8000)
                      ▼
┌─────────────────────────────────────────────────────────────┐
│          FastAPI — Port 8000                                 │
│  • ML inference (scikit-learn .joblib model)                 │
│  • Model retraining endpoint                                 │
│  • RAG chat endpoint (Gemini + ChromaDB)                     │
│  • CORS enabled for port 8080                                │
└─────────────────────────────────────────────────────────────┘
                      │
                      ▼
┌──────────────┐   ┌──────────────────┐
│  PostgreSQL  │   │    ChromaDB       │
│  (main DB)   │   │  (vector store)   │
└──────────────┘   └──────────────────┘
```

---



## Getting Started

### Prerequisites

- Java 17+
- Python 3.10+
- PostgreSQL 14+
- Node.js (optional, for frontend tooling)
- A Google Gemini API key

### 1. Clone the repository

```bash
git clone https://github.com/Dineth-Wickremasinghe/Fabric_Waste_Prediction.git
cd fabric-ai
```

### 2. Set up PostgreSQL

Create a database and update the Spring Boot `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fabric_ai
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Start the FastAPI service

```bash
cd fastapi
python -m venv venv
venv\Scripts\activate        # Windows
source venv/bin/activate     # macOS/Linux

pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

### 4. Start the Spring Boot service

```bash
cd springboot
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`.

---

## Environment Variables

Create a `.env` file in the `fastapi/` directory:

```env
GEMINI_API_KEY=your_gemini_api_key_here
COLLECTION_NAME=cutting_room_kb
EMBEDDING_MODEL=models/gemini-embedding-001
GEMINI_MODEL=gemini-2.5-flash
CHUNK_SIZE=1000
CHUNK_OVERLAP=200
```

---

## API Endpoints

### FastAPI (Port 8000)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/predict` | Run ML inference and return predicted waste % |
| `POST` | `/retrain` | Retrain the model on new data; replaces model only if R² improves |
| `POST` | `/chat` | RAG chat endpoint — queries ChromaDB and generates a Gemini response |

### Spring Boot (Port 8080)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Home / prediction form |
| `GET` | `/dashboard` | Prediction history and monitoring dashboard |
| `POST` | `/predictions/{id}/actual` | Submit actual result for a prediction |
| `GET` | `/sustainability` | Sustainability metrics view |
| `GET` | `/reports` | Report generation page |

---

## ML Model

The model is a **scikit-learn pipeline** serialised as a `.joblib` file. It was trained on approximately 1,500 historical cutting records.

**Input features:**
- Pattern complexity
- Operator experience (years)
- Fabric pattern (target-encoded)
- Cutting method (manual: 0/1)
- Fabric type (target-encoded)
- Marker loss percentage

**Target:** Fabric waste percentage (`prediction_result`)

**Baseline performance:** R² ≈ 0.80

Target encoding mappings for categorical features are stored in `target_encoding_mappings.json`.

### Model Monitoring

The dashboard computes the following metrics on predictions that have an `actual_result` logged:

- **R²** and **MAE** — Overall model performance
- **Residual analysis** — Distribution of errors to detect systematic bias
- **Rolling window drift** — Tracks mean error over a sliding window to detect concept drift over time

---

## Running Tests

### Spring Boot (Java)

```bash
cd springboot
./mvnw test
```

Tests are located in `src/test/java/` and cover:

- `PredictionServiceTest` — Service layer unit tests
- `PredictionControllerTest` — Controller layer tests (MockMvc)
- `PredictionRepositoryTest` — Repository tests (H2 in-memory DB via `@DataJpaTest`)

### FastAPI (Python)

```bash
cd fastapi
pytest
```

---

## Production Deployment

For a local production setup, both services can be registered as Windows Services using [NSSM](https://nssm.cc/) or containerised using Docker Compose:

```yaml
version: '3.8'
services:
  fastapi:
    build: ./fastapi
    ports:
      - "8000:8000"
    env_file:
      - ./fastapi/.env
    restart: always

  springboot:
    build: ./springboot
    ports:
      - "8080:8080"
    depends_on:
      - fastapi
    restart: always
```

---

## License

This project was developed as part of an academic software engineering module. All rights reserved.
