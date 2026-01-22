# AGENTS.md - Frontend Specification (Sinsay Returns PoC)

## 1. System Overview
**Role:** Client-side interface for customers to upload return requests.
**Core Responsibility:** Capture high-quality images and description, manage upload state, and display AI verdict.
**Constraint Checklist:**
- [x] React 18 + Vite
- [x] TypeScript only
- [x] Tailwind CSS for Styling
- [x] NO User Authentication (Public Kiosk/PoC Mode)
- [x] Visual Consistency with Sinsay Brand (Minimalist, B&W, Clean)

## 2. Technical Architecture

### Stack
- **Build Tool:** Vite
- **Framework:** React 18
- **Language:** TypeScript 5.x
- **Styling:** Tailwind CSS 3.x
- **HTTP Client:** Native `fetch` (Zero external dependencies if possible, or Axios)
- **Icons:** `lucide-react` (Lightweight)

### Project Structure
```
src/
├── assets/             # Static Assets
├── components/         # Reusable UI Components
│   ├── ui/             # Atomic Design Primitives (Button, Card)
│   ├── UploadZone.tsx  # Drag & Drop Image Handler
│   └── ResultCard.tsx  # Display AI Verdict
├── services/           # API Integration
│   └── api.ts          # Backend Communication
├── App.tsx             # Main Layout
└── main.tsx            # Entry Point
```

## 3. Implementation Guidelines

### 3.1. File Handling (Multimodal)
**Component:** `UploadZone.tsx`
**Logic:**
- Accept `image/jpeg`, `image/png`.
- Validate file size (< 10MB) client-side before upload.
- Preview image locally using `URL.createObjectURL()` before sending.
- **FormData Construction:**
  ```typescript
  const formData = new FormData();
  formData.append('file', fileObject);
  formData.append('description', descriptionText);
  // Send via POST
  ```
*Critical: Ensure Content-Type is NOT manually set to 'multipart/form-data'. Let the browser set the boundary.*

### 3.2. API Integration
**Endpoint:** `POST /api/returns/analyze`
**Proxy:** Configure Vite `server.proxy` to route `/api` to `http://localhost:8080` to avoid CORS in dev.
**Response Handling:**
- Loading State: Show spinner/skeleton processing state (~5-10s latency expected for GPT-4o Vision).
- Error State: Graceful handling of server errors or validation failures.
- Success State: Render verdict.

### 3.3. UI/UX Standards
**Theme:**
- Primary: Black (`#000000`)
- Secondary: White (`#FFFFFF`)
- Accent: Grey (`#F5F5F5`)
- Fonts: Sans-serif (Inter or similar system font).
- **Responsive:** Mobile-first design (Critical for customer photo uploads).

### 3.4. Type Safety
**Interface `AnalysisResponse`:**
```typescript
interface AnalysisResponse {
  verdict: 'ACCEPTED' | 'REJECTED' | 'MANUAL_REVIEW';
  confidence: number;
  reasoning: string;
  suggestedAction: string;
}
```
*Note: Must exact-match Backend DTO.*

## 4. Development Standards
- **Component Design:** Functional components with Hooks.
- **State Management:** Local state (`useState`) sufficient for PoC. Context API if complexity grows.
- **Code Quality:** ESLint + Prettier enabled.
- **Testing:** Basic unit tests with Vitest if time permits, priority on manual E2E flow.

## 5. Setup Instructions
1. **Requirements:** Node.js v18+.
2. **Install:** `npm install`
3. **Run:** `npm run dev`
4. **Environment:** Create `.env` if API base URL customization is needed (default to relative path via proxy).
