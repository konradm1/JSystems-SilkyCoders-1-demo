# Sinsay Returns PoC (Monorepo)

A stateless AI system for analyzing customer photos and complaints for Sinsay returns.

## Structure
- **backend/**: Spring Boot 3.4+ application (Java 21). Stateless, In-memory RAG.
- **frontend/**: React 18 + Vite application. Strict FormData handling.

## Specific Docs
- [Backend Specification](backend/AGENTS.md)
- [Frontend Specification](frontend/AGENTS.md)

## Quick Start
### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```
