# LeagueHQ Frontend

React 18 + TypeScript + Vite frontend for LeagueHQ.

## Tech Stack
- React 18
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Axios

## Setup
See main [README.md](../README.md) for full setup instructions.

## Running Locally

```bash
# From frontend/ directory
npm install
npm run dev
```

Frontend will be available at: `http://localhost:5173`

## API Integration

The Vite dev server proxies `/api` requests to the backend at `http://localhost:8080`.

Example:
```typescript
// This will proxy to http://localhost:8080/api/auth/login
axios.post('/api/auth/login', { email, password })
```

## Project Structure

```
src/
├── components/     # Reusable UI components
├── pages/          # Page components (routes)
├── services/       # API client (axios)
├── hooks/          # Custom React hooks
├── types/          # TypeScript type definitions
├── utils/          # Utility functions
├── App.tsx         # Main app component
└── main.tsx        # Entry point
```

## Available Scripts

- `npm run dev` - Start dev server (port 5173)
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Environment Variables

Create `.env` file:
```
VITE_API_BASE_URL=http://localhost:8080
```

## Deployment

Deployed to Vercel. Build settings:
- Build command: `npm run build`
- Output directory: `dist`
- Root directory: `frontend`
