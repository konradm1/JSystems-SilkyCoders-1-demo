# AGENTS.md - Backend Specification (Sinsay Returns PoC)

## 1. System Overview
**Role:** Stateless Backend API for processing Visual + Text complaints.
**Core Responsibility:** Orchestrate Multimodal AI analysis (OpenAI GPT-4o) using in-memory RAG.
**Constraint Checklist:**
- [x] NO Database (In-memory only)
- [x] NO Docker
- [x] Java 21 + Spring Boot 3.4+
- [x] Spring AI 1.0.1+
- [x] Stateless Architecture

## 2. Technical Architecture

### Stack
- **Framework:** Spring Boot 3.4.x
- **AI Integration:** Spring AI (OpenAI Provider)
- **Vector Store:** `SimpleVectorStore` (In-memory)
- **Embedding Model:** `OpenAiEmbeddingModel`
- **Chat Model:** `OpenAiChatModel` (GPT-4o)
- **Build Tool:** Maven

### Project Structure
```
src/main/java/com/sinsay/returns
├── config/             # AI Client & VectorStore Config
├── controller/         # REST Endpoints (Multipart)
├── dto/                # Request/Response Records (Strict Typing)
├── service/            # Business Logic & RAG Orchestration
├── service/rag/        # Vector Store Initialization & Retrieval
├── util/               # Image Processing / MIME Validation
└── ReturnsApplication.java
```

### Data Flow
1. **POST /api/returns/analyze** receives `MultipartFile image` + `String description`.
2. **Validation:** Check MIME type (image/jpeg, image/png) & file size.
3. **ETL (Startup):** Load `policies.json` -> Tokenize -> Embed -> Store in `SimpleVectorStore`.
4. **Retrieval:** Query `SimpleVectorStore` using `description` for relevant policy chunks.
5. **Generation:** Construct `UserMessage`:
   - Text: System prompt + Retrieved Context + User Description.
   - Media: Image byte stream (In-memory).
6. **Response:** Stream structured JSON verdict to Frontend via SSE.

## 3. Implementation Guidelines

### 3.1. RAG & Vector Store (Ephemeral)
**File:** `src/main/resources/data/policies.json`
**Loader:** Use `JsonReader` or manual `ObjectMapper` to parse policies, then convert to `Document`.
**Initialization:**
```java
@Bean
public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
    SimpleVectorStore store = new SimpleVectorStore(embeddingModel);
    // Load policies.json from classpath
    // Convert to List<Document>
    // store.add(documents);
    return store;
}
```
**Policy Source:** `https://www.sinsay.com/pl/pl/help-regulamin-zwrotow-i-wymian-towarow-w-sklepach-sinsay`
*Note: Ensure policies.json contains extracted text from this URL.*

### 3.2. Multimodal AI Service
**Model:** Use `gpt-4o` (or compatible multimodal model).
**Code Pattern:**
```java
var userMessage = new UserMessage(
    "Analyze this return request against the following policy: " + policyContext + "\nUser Description: " + description,
    List.of(new Media(MimeTypeUtils.parseMimeType(file.getContentType()), new InputStreamResource(file.getInputStream())))
);
Flux<String> response = chatClient.prompt(userMessage).stream().content();
```
*Critical: Do NOT save image to disk. Use `InputStreamResource` or `byte[]`.*

### 3.3. API Contract (DTOs)
**Request:** `multipart/form-data`
- `file`: Binary (Image)
- `description`: Text

**Response:** `text/event-stream`
Stream of JSON characters constructing:
```json
{
  "verdict": "ACCEPTED | REJECTED | MANUAL_REVIEW",
  "confidence": 0.95,
  "reasoning": "The item shows visible wear marks which violates policy section...",
  "suggestedAction": "Inform customer about rejection due to usage."
}
```

## 4. Development Standards
- **Error Handling:** Global Exception Handler for `MaxUploadSizeExceededException`, `UnsupportedMediaTypeStatusException`.
- **Validation:** Strict verification of input content.
- **Logging:** Log flow steps but NEVER log PII or full User Descriptions.
- **Testing:**
  - `mvn clean test` for unit tests.
  - Mock `OpenAiChatModel` in tests to avoid API costs during build.

## 5. Setup Instructions
1. **Requirements:** Java 21, Maven.
2. **Environment:** Set `SPRING_AI_OPENAI_API_KEY`.
3. **Run:** `./mvnw spring-boot:run`
4. **Data:** Ensure `src/main/resources/data/policies.json` is populated manually from the Sinsay URL before start.
