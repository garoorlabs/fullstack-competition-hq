# LeagueHQ - Frontend Coding Guidelines (MVP)

## Scope
React 18 + TypeScript + Tailwind CSS + Vite frontend for 6-7 week MVP.
Backend guidelines tracked separately.

## Principles
- Ship working UI slices weekly, aligned with backend progress
- Component-first thinking: build reusable pieces
- Keep state close to where it's used
- Mobile-first responsive design
- Accessible by default (semantic HTML, keyboard nav, ARIA when needed)

## Tech Stack
- **React 18** with TypeScript
- **Vite** for build tooling
- **React Router v6** for routing
- **Tailwind CSS** for styling
- **Axios** for API calls
- **React Hook Form** for form handling
- **Zod** for validation (mirrors backend DTOs)
- **date-fns** for date formatting
- **lucide-react** for icons

## Project Structure
```
src/
├─ components/
│  ├─ ui/              // Reusable primitives (Button, Input, Card)
│  ├─ common/          // Shared composite components (LoadingSpinner, EmptyState, DataError)
│  ├─ forms/           // Form components (LoginForm, CreateCompForm)
│  ├─ layout/          // Layout components (Header, Footer, Sidebar)
│  └─ features/        // Feature-specific components (CompetitionCard, TeamList)
├─ pages/              // Route pages (HomePage, DashboardPage, CompetitionPage)
├─ hooks/              // Custom hooks (useAuth, useApi, useCompetition)
├─ lib/                // Utilities and configurations
│  ├─ api.ts           // Axios instance, interceptors
│  ├─ auth.ts          // Auth helpers (getToken, setToken, clearToken)
│  ├─ errors.ts        // Error normalization helpers
│  └─ utils.ts         // Generic helpers (formatDate, formatCurrency)
├─ types/              // TypeScript types/interfaces
│  ├─ api.ts           // API request/response types
│  └─ models.ts        // Domain models (User, Competition, Team)
├─ context/            // React contexts (AuthContext)
└─ App.tsx             // Root component with router + ErrorBoundary
```

## Component Conventions

**Naming:**
- PascalCase for components: `CompetitionCard.tsx`
- camelCase for utilities: `formatDate.ts`
- Use descriptive names: `CreateCompetitionForm` not `CompForm`

**File Structure:**
```typescript
// CompetitionCard.tsx
import { Competition } from '@/types/models';

interface CompetitionCardProps {
  competition: Competition;
  onEdit?: (id: string) => void;
}

export function CompetitionCard({ competition, onEdit }: CompetitionCardProps) {
  // Component logic
  return (
    <div className="...">
      {/* JSX */}
    </div>
  );
}
```

**Component Types:**
- **UI Components** (`components/ui/`): Pure presentation, no business logic
- **Common Components** (`components/common/`): Shared composites like LoadingSpinner, EmptyState
- **Feature Components** (`components/features/`): Business logic, API calls
- **Page Components** (`pages/`): Route containers, compose features

**Props:**
- Define explicit interfaces for all props
- Destructure props in function signature
- Use optional props sparingly: `onEdit?` is okay, excessive optionals = bad design
- Avoid prop drilling: use context for deep trees (auth, theme)

## State Management

**Local State:**
- `useState` for simple component state
- Keep state as close to usage as possible
- Don't lift state until you need to share it

**Server State:**
- Don't cache in React state unless necessary
- Fetch on mount, show loading states
- Handle errors explicitly
- Simple pattern for MVP:
```typescript
const [data, setData] = useState<Competition[]>([]);
const [loading, setLoading] = useState(true);
const [error, setError] = useState<string | null>(null);

useEffect(() => {
  fetchCompetitions()
    .then(setData)
    .catch(err => setError(err.message))
    .finally(() => setLoading(false));
}, []);
```

**Post-MVP Optimization:**
- If you feel pain with stale cache or excessive refetching, consider React Query
- Don't add it now—only after launch if needed

**Global State:**
- `AuthContext` for user session (token, user info, login/logout)
- Avoid Redux/Zustand for MVP unless absolutely needed
- Context is sufficient for auth and light global state

**Form State:**
- Use React Hook Form for all forms
- Zod schemas for validation (start with login/signup + create competition)
- Mirror backend validation rules
```typescript
const schema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
});

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema)
});
```

## API Integration

**Axios Setup:**
```typescript
// lib/api.ts
import axios from 'axios';
import { getToken, clearToken } from './auth';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
});

// Request interceptor: attach JWT
api.interceptors.request.use(config => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle 401
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      clearToken();
      // Emit custom event for auth context to handle navigation
      window.dispatchEvent(new CustomEvent('auth:logout'));
    }
    return Promise.reject(error);
  }
);

export default api;
```

**Error Normalization:**
```typescript
// lib/errors.ts
import axios from 'axios';

export interface NormalizedError {
  message: string;
  status?: number;
}

export function normalizeError(error: unknown): NormalizedError {
  if (axios.isAxiosError(error)) {
    return {
      message: error.response?.data?.message || 'Something went wrong',
      status: error.response?.status
    };
  }
  
  if (error instanceof Error) {
    return { message: error.message };
  }
  
  return { message: 'An unexpected error occurred' };
}
```

**API Functions:**
```typescript
// lib/api/competitions.ts
import api from '@/lib/api';
import { Competition, CreateCompetitionRequest } from '@/types/api';

export const competitionApi = {
  getMyCompetitions: () => 
    api.get<Competition[]>('/competitions/my').then(res => res.data),
  
  getByToken: (token: string) => 
    api.get<Competition>(`/competitions/share/${token}`).then(res => res.data),
  
  create: (data: CreateCompetitionRequest) => 
    api.post<Competition>('/competitions', data).then(res => res.data),
  
  publish: (id: string) => 
    api.post(`/competitions/${id}/publish`).then(res => res.data)
};
```

**Custom Hooks:**
```typescript
// hooks/useCompetitions.ts
import { normalizeError } from '@/lib/errors';

export function useCompetitions() {
  const [competitions, setCompetitions] = useState<Competition[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    competitionApi.getMyCompetitions()
      .then(setCompetitions)
      .catch(err => setError(normalizeError(err).message))
      .finally(() => setLoading(false));
  }, []);

  return { competitions, loading, error };
}
```

## Routing

**React Router v6:**
```typescript
// App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { ErrorBoundary } from './components/ErrorBoundary';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ErrorBoundary>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            
            {/* Protected routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/competitions/new" element={<CreateCompetitionPage />} />
            </Route>
            
            {/* Public competition view via share token */}
            <Route path="/c/:token" element={<CompetitionPublicPage />} />
            
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </ErrorBoundary>
      </AuthProvider>
    </BrowserRouter>
  );
}
```

**IMPORTANT:** `AuthProvider` must be **inside** `BrowserRouter` because it uses `useNavigate`.

**Protected Routes:**
```typescript
// components/ProtectedRoute.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) return <LoadingSpinner />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  
  return <Outlet />;
}
```

**Error Boundary:**
```typescript
// components/ErrorBoundary.tsx
import React from 'react';

export class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Caught error:', error, errorInfo);
    // TODO: Log to error tracking service post-launch
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <h1 className="text-2xl font-bold mb-2">Something went wrong</h1>
            <button 
              onClick={() => window.location.href = '/'}
              className="text-blue-600 hover:underline"
            >
              Return to home
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
```

**Not Found Page:**
```typescript
// pages/NotFoundPage.tsx
import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-2">404</h1>
        <p className="text-gray-600 mb-4">Page not found</p>
        <Link to="/" className="text-blue-600 hover:underline">
          Go home
        </Link>
      </div>
    </div>
  );
}
```

## Utilities

**Date Formatting:**
```typescript
// lib/utils.ts
import { format, parseISO } from 'date-fns';

export function formatDate(dateString: string): string {
  return format(parseISO(dateString), 'MMM d, yyyy');
}

export function formatDateTime(dateString: string): string {
  return format(parseISO(dateString), 'MMM d, yyyy h:mm a');
}
```

**Currency Formatting:**
```typescript
// lib/utils.ts
export function formatCurrency(cents: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(cents / 100);
}

// Usage: formatCurrency(5000) => "$50.00"
```

## Styling with Tailwind

**Conventions:**
- Mobile-first: base styles for mobile, `md:` and `lg:` for larger screens
- Use Tailwind utility classes directly in JSX
- Extract repeated patterns to components, not CSS classes
- Use `cn()` helper for conditional classes (defined in lib/utils.ts)

**Usage Example:**
```typescript
<button className={cn(
  "px-4 py-2 rounded",
  isPrimary ? "bg-blue-600 text-white" : "bg-gray-200 text-gray-800",
  disabled && "opacity-50 cursor-not-allowed"
)}>
```

**Spacing & Sizing:**
- Use Tailwind scale: `p-4`, `mt-6`, `gap-3`
- Consistent spacing: 4px increments (4, 8, 12, 16, 24, 32...)
- Max widths: `max-w-7xl` for main content, `max-w-md` for forms

**Colors:**
- Semantic colors: `bg-blue-600` for primary, `bg-red-600` for danger
- Use `text-gray-600` for secondary text, `text-gray-900` for primary
- Consistent hover states: `hover:bg-blue-700`

**Responsive Design:**
```typescript
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {/* Mobile: 1 column, Tablet: 2 columns, Desktop: 3 columns */}
</div>
```

## Forms & Validation

**React Hook Form + Zod:**
```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters')
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginForm() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema)
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      await authApi.login(data);
      // Handle success
    } catch (error) {
      const normalized = normalizeError(error);
      // Show error toast or set form error
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="email" className="block text-sm font-medium">
          Email
        </label>
        <input
          {...register('email')}
          type="email"
          id="email"
          className="mt-1 block w-full rounded border-gray-300"
          aria-describedby={errors.email ? 'email-error' : undefined}
        />
        {errors.email && (
          <p id="email-error" className="mt-1 text-sm text-red-600" role="alert">
            {errors.email.message}
          </p>
        )}
      </div>
      
      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50"
      >
        {isSubmitting ? 'Logging in...' : 'Log In'}
      </button>
    </form>
  );
}
```

## Error Handling

**API Errors:**
```typescript
import { normalizeError } from '@/lib/errors';

try {
  await competitionApi.create(data);
} catch (error) {
  const { message } = normalizeError(error);
  setError(message);
  // Or show toast notification
}
```

**Error Display:**
- Toast notifications for transient errors (network issues)
- Inline validation errors below form fields with `role="alert"`
- Error boundaries for catastrophic failures
- Focus management: move focus to error summary for keyboard users

**Toast Utility (Simple Implementation):**
```typescript
// components/common/Toast.tsx
// Use Radix UI Toast or similar lightweight library
// Ensure announcements via aria-live="assertive" for screen readers
// Example structure:
<div role="region" aria-live="assertive" aria-atomic="true">
  {/* Toast content */}
</div>
```

## Authentication

**Auth Context:**
```typescript
// context/AuthContext.tsx
import { useNavigate } from 'react-router-dom';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const token = getToken();
    if (token) {
      authApi.getMe()
        .then(setUser)
        .catch(() => clearToken())
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  // Listen for logout events from interceptor
  useEffect(() => {
    const handleLogout = () => {
      clearToken();  // Ensure token is cleared
      setUser(null);
      navigate('/login');
    };
    
    window.addEventListener('auth:logout', handleLogout);
    return () => window.removeEventListener('auth:logout', handleLogout);
  }, [navigate]);

  const login = async (email: string, password: string) => {
    const { token, user } = await authApi.login({ email, password });
    setToken(token);
    setUser(user);
  };

  const logout = () => {
    clearToken();
    setUser(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
```

**Token Storage:**
```typescript
// lib/auth.ts
const TOKEN_KEY = 'leaguehq_token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);
```

## TypeScript

**Type Definitions (MUST MATCH BACKEND):**
```typescript
// types/models.ts

// User roles - MUST MATCH BACKEND
export type UserRole = 'PLATFORM_OWNER' | 'COMPETITION_OWNER' | 'COACH';

// Stripe statuses - MUST MATCH BACKEND SCHEMA
export type StripeConnectStatus = 'NOT_STARTED' | 'INCOMPLETE' | 'VERIFIED' | 'BLOCKED';
export type PayoutStatus = 'NONE' | 'PENDING' | 'ENABLED' | 'BLOCKED';

export interface User {
  id: string;
  email: string;
  passwordHash?: string;  // Never sent to frontend, but listed for completeness
  fullName: string;
  role: UserRole;
  stripeCustomerId?: string;
  stripeConnectAccountId?: string;
  stripeConnectStatus: StripeConnectStatus;
  stripeConnectOnboardedAt?: string;
  payoutStatus: PayoutStatus;
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

// Competition statuses - MUST MATCH BACKEND
export type CompetitionStatus = 
  | 'DRAFT' 
  | 'REGISTRATION_OPEN' 
  | 'REGISTRATION_CLOSED' 
  | 'IN_PROGRESS' 
  | 'COMPLETED' 
  | 'CANCELLED';

export interface Competition {
  id: string;
  ownerId: string;
  name: string;
  description?: string;
  sport: string;
  entryFeeCents: number;
  platformFeePercentage: number;
  policy: CompetitionPolicy;
  maxTeams: number;
  registrationDeadline?: string;
  startDate: string;
  endDate: string;
  status: CompetitionStatus;
  shareToken: string;
  venue?: Venue;  // MVP: query first venue from venues table
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
}

export interface Venue {
  id: string;
  competitionId: string;
  name: string;
  address?: string;
  createdAt: string;
}

export interface CompetitionPolicy {
  scoring: {
    win: number;
    draw: number;
    loss: number;
  };
  tiebreakers: string[];
  roster: {
    minSize: number;
    maxSize: number;
    lockAt: string;
  };
  refunds: {
    fullRefundDaysBefore: number;
    partialRefundDaysBefore: number;
    partialRefundPercentage: number;
  };
}

// Team subscription statuses - MUST MATCH BACKEND
export type SubscriptionStatus = 
  | 'ACTIVE' 
  | 'PAST_DUE' 
  | 'CANCELLED' 
  | 'INCOMPLETE' 
  | 'TRIALING';

export interface Team {
  id: string;
  competitionId: string;
  coachId: string;
  name: string;
  entryFeePaid: boolean;
  entryFeePaidAt?: string;
  entryFeeStripePaymentId?: string;
  subscriptionId?: string;
  subscriptionStatus?: SubscriptionStatus;
  subscriptionCurrentPeriodStart?: string;
  subscriptionCurrentPeriodEnd?: string;
  subscriptionCancelAt?: string;
  isEligible: boolean;
  rosterSize: number;
  rosterLocked: boolean;
  rosterLockedAt?: string;
  createdAt: string;
  updatedAt: string;
  registeredAt?: string;
}

export interface Player {
  id: string;
  teamId: string;
  fullName: string;
  jerseyNumber?: number;
  position?: string;
  photoUrl?: string;
  photoUploadedAt?: string;
  photoSizeBytes?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Match {
  id: string;
  competitionId: string;
  homeTeamId: string;
  awayTeamId: string;
  matchDate: string;
  matchTime?: string;
  venueId?: string;
  homeScore?: number;
  awayScore?: number;
  status: MatchStatus;
  resultEnteredBy?: string;
  resultEnteredAt?: string;
  createdAt: string;
  updatedAt: string;
}

export type MatchStatus = 
  | 'SCHEDULED' 
  | 'IN_PROGRESS' 
  | 'COMPLETED' 
  | 'CANCELLED' 
  | 'POSTPONED' 
  | 'DISPUTED';

export interface Standing {
  teamId: string;
  teamName: string;
  competitionId: string;
  played: number;
  won: number;
  drawn: number;
  lost: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
  points: number;
}

// types/api.ts
export interface CreateCompetitionRequest {
  name: string;
  description?: string;
  sport: string;
  entryFeeCents: number;
  platformFeePercentage?: number;  // Defaults to 8.00 on backend
  policy?: Partial<CompetitionPolicy>;  // Has defaults on backend
  maxTeams: number;
  registrationDeadline?: string;  // ISO date string
  startDate: string;  // ISO date string
  endDate: string;  // ISO date string
  venue: {
    name: string;
    address?: string;
  };
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface ApiError {
  status: number;
  message: string;
  path: string;
  timestamp: string;
}
```

**TypeScript Config (tsconfig.json):**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "strict": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

**Field Name Conventions:**
- Backend uses snake_case in DB, Jackson converts to camelCase for JSON
- Frontend types MUST use camelCase matching Jackson output
- Examples: `full_name` (DB) → `fullName` (JSON/TS), `created_at` → `createdAt`
- Dates: Backend sends ISO 8601 strings, use `date-fns` for formatting

**Money Handling:**
- All amounts stored in cents: `entryFeeCents`, `amountCents`, `platformFeeCents`
- Display with helper: `formatCurrency(cents: number) => string`
- Never use floats for money calculations

**Use Types Everywhere:**
- No `any` types (use `unknown` if truly necessary)
- Props interfaces for all components
- API request/response types
- Leverage type inference where obvious

## Loading & Empty States

**Loading States:**
```typescript
// components/common/LoadingSpinner.tsx
export function LoadingSpinner() {
  return (
    <div className="flex justify-center items-center h-64">
      <div 
        className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"
        role="status"
        aria-label="Loading"
      />
    </div>
  );
}
```

**Empty States:**
```typescript
// components/common/EmptyState.tsx
interface EmptyStateProps {
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="text-center py-12">
      <p className="text-gray-900 font-medium mb-2">{title}</p>
      <p className="text-gray-500 mb-4">{description}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
```

## Accessibility

**Semantic HTML:**
- Use `<button>` for actions, `<a>` for navigation
- Proper heading hierarchy (`<h1>`, `<h2>`, `<h3>`)
- Form labels for all inputs
- Alt text for images

**Keyboard Navigation:**
- All interactive elements focusable
- Logical tab order
- Enter/Space to activate buttons

**ARIA:**
- Use sparingly, prefer semantic HTML
- `aria-label` for icon-only buttons
- `aria-describedby` for error messages
- `role="alert"` for error text
- `aria-live` for toast notifications
```typescript
<button aria-label="Close modal">
  <X className="h-4 w-4" />
</button>

<input
  {...register('email')}
  aria-describedby={errors.email ? 'email-error' : undefined}
/>
{errors.email && (
  <p id="email-error" role="alert" className="text-red-600">
    {errors.email.message}
  </p>
)}
```

## Performance

**Code Splitting:**
```typescript
import { lazy, Suspense } from 'react';

const DashboardPage = lazy(() => import('./pages/DashboardPage'));

<Suspense fallback={<LoadingSpinner />}>
  <DashboardPage />
</Suspense>
```

**Avoid Premature Optimization:**
- Don't memoize everything
- Use `useMemo`/`useCallback` only when profiling shows benefit
- Most re-renders are fast enough

**Images:**
- Optimize before upload (WebP, compression)
- Use appropriate sizes
- Lazy load below-the-fold images

## Testing

**Week 1-2:**
- Manual testing in browser
- Test all user flows end-to-end

**Week 3+:**
- Vitest for unit tests (form validation, utilities)
- React Testing Library for component tests
- Focus on critical paths: auth, registration, payment

**Test Naming:**
```typescript
describe('LoginForm', () => {
  it('should display validation errors for invalid email', () => {
    // Test logic
  });
  
  it('should call login API on valid submission', async () => {
    // Test logic
  });
});
```

## Environment Variables

**Vite:**
- Prefix with `VITE_`: `VITE_API_URL`, `VITE_STRIPE_PUBLISHABLE_KEY`
- Access via `import.meta.env.VITE_API_URL`
- Never commit `.env` file
- Provide `.env.example`:
```
VITE_API_URL=http://localhost:8080/api
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

## Stripe Integration

**MVP Flow: Checkout Redirect + Customer Portal**

Your Stripe flow uses **Checkout Sessions** (redirect) and **Customer Portal** (for subscription management). You do NOT need Stripe Elements or CardElement in MVP.

**Team Registration Flow:**
1. User clicks "Register Team"
2. Frontend calls backend `/api/teams/register`
3. Backend creates Checkout Session, returns `checkoutUrl`
4. Frontend redirects: `window.location.href = checkoutUrl`
5. User completes payment on Stripe
6. Stripe redirects back to success URL
7. Webhook updates backend

**No Stripe Elements needed for MVP.** Post-launch, if you add on-site payments, revisit this section.

## Code Quality

**ESLint Config (.eslintrc.cjs):**
```javascript
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  parser: '@typescript-eslint/parser',
  plugins: ['react-refresh'],
  rules: {
    'react-refresh/only-export-components': [
      'warn',
      { allowConstantExport: true },
    ],
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
  },
}
```

**Prettier Config (.prettierrc):**
```json
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2
}
```

## What NOT to Do

- Don't put API calls in components (use custom hooks or service functions)
- Don't use inline styles (use Tailwind)
- Don't duplicate code (extract to components/hooks/utils)
- Don't ignore TypeScript errors
- Don't commit console.logs (use proper logging if needed)
- Don't build features outside locked scope
- Don't use `@ts-ignore` (fix the type issue)
- Don't store sensitive data in localStorage beyond JWT
- Don't add Stripe Elements or on-site payment flows (use Checkout redirect)
- Don't add React Query yet (only post-launch if needed)

## Comments

- Comment complex logic and business rules
- JSDoc for reusable utility functions
- No commented-out code in commits
- Explain WHY, not WHAT

## Week 1 Implementation Checklist (Aligned with Backend)

**Project Setup:**
- [ ] Vite + React + TypeScript project
- [ ] Install: react-router-dom, axios, react-hook-form, @hookform/resolvers, zod, tailwind, lucide-react, clsx, tailwind-merge, date-fns
- [ ] Configure Tailwind CSS
- [ ] Setup TypeScript with strict mode
- [ ] Add ESLint + Prettier configs
- [ ] Setup environment variables (.env.example)
- [ ] Create project structure (components, pages, hooks, lib, types)

**Auth UI:**
- [ ] AuthContext and AuthProvider with logout event listener
- [ ] Login page with form validation (Zod + RHF)
- [ ] Signup page with form validation
- [ ] ProtectedRoute component
- [ ] API integration (auth endpoints)
- [ ] Token storage helpers
- [ ] Axios interceptors for JWT and 401 handling with custom event
- [ ] Error normalizer utility
- [ ] ErrorBoundary component
- [ ] NotFoundPage component
- [ ] LoadingSpinner common component
- [ ] Dashboard placeholder (protected route)

**Success Criteria:**
- [ ] Can sign up new user
- [ ] Can log in and receive JWT
- [ ] Protected routes redirect to login
- [ ] JWT attached to API requests
- [ ] 401 responses trigger logout event and navigate to login
- [ ] User info displayed in header after login
- [ ] All TypeScript types match backend enums exactly

## Week 2 Implementation Checklist (Aligned with Backend)

**Competition Management UI:**
- [ ] Dashboard page listing owner's competitions
- [ ] Create competition form (name, sport, dates, single venue) with Zod validation
- [ ] CompetitionCard feature component
- [ ] Public competition view page (accessible via share token `/c/:token`)
- [ ] API integration (competition endpoints including GET /api/competitions/share/:token)
- [ ] Form validation matching backend rules
- [ ] EmptyState common component
- [ ] DataError common component
- [ ] Loading states during API calls
- [ ] Competition status badges (DRAFT, REGISTRATION_OPEN, etc.)

**Success Criteria:**
- [ ] Owner can create competition with one venue
- [ ] Dashboard displays all owner's competitions
- [ ] Clicking competition navigates to detail view
- [ ] Share link accessible via `/c/:token` without auth (backend endpoint: GET /api/competitions/share/:token)
- [ ] Form validation prevents invalid submissions
- [ ] Error messages displayed using normalized errors
- [ ] Competition statuses match backend exactly (all 6 states)
- [ ] Single venue rendered correctly (venue property, not array)

## Week 3 Implementation Checklist (Aligned with Backend)

**Stripe Connect Integration:**
- [ ] Stripe Connect onboarding button in dashboard
- [ ] Handle redirect back from Stripe onboarding (success/refresh URLs)
- [ ] Display payout status in UI (NOT_ENABLED, PENDING, ENABLED)
- [ ] Publish competition button (disabled until payout ENABLED)
- [ ] API integration (Stripe endpoints)
- [ ] Toast notifications for onboarding success/errors
- [ ] Visual indicator of Stripe connection status
- [ ] User type guards for role-based UI

**Success Criteria:**
- [ ] Owner can initiate Stripe Connect onboarding
- [ ] After onboarding, payout status updates to ENABLED (webhook updates backend)
- [ ] Can't publish competition until payout status is ENABLED (not just Stripe connected)
- [ ] Publish button works when eligible
- [ ] Clear feedback on connection status via toast (aria-live="assertive")
- [ ] All user roles match backend (PLATFORM_OWNER, COMPETITION_OWNER, COACH)
- [ ] All Stripe statuses match backend schema exactly (NOT_STARTED, INCOMPLETE, VERIFIED, BLOCKED for connect; NONE, PENDING, ENABLED, BLOCKED for payout)

## Notes

- Build UI in parallel with backend API availability
- Test with real backend locally (not mock data)
- Prioritize core flows over polish initially
- Mobile responsiveness from day one
- Refactor components when patterns emerge, not before
- Keep ESLint/Prettier running to catch issues early
- Focus on shipping working slices weekly

---

## Quick Start Configs

### package.json scripts
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "preview": "vite preview",
    "format": "prettier --write \"src/**/*.{ts,tsx,css}\""
  }
}
```

### tsconfig.json (with strict mode)
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### .eslintrc.cjs
```javascript
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  parser: '@typescript-eslint/parser',
  plugins: ['react-refresh'],
  rules: {
    'react-refresh/only-export-components': [
      'warn',
      { allowConstantExport: true },
    ],
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    'no-console': ['warn', { allow: ['warn', 'error'] }],
  },
}
```

### .prettierrc
```json
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "arrowParens": "avoid"
}
```

### .env.example
```
VITE_API_URL=http://localhost:8080/api
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

### .nvmrc (optional but helpful for team consistency)
```
v20
```

### vite.config.ts (with path aliases)
```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

---

## Complete Example: Login Flow

This shows the full stack from form → API → auth context → navigation.

```typescript
// types/api.ts
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

// lib/api/auth.ts
import api from '@/lib/api';
import { LoginRequest, LoginResponse } from '@/types/api';

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', data).then(res => res.data),
  
  getMe: () =>
    api.get<User>('/auth/me').then(res => res.data),
};

// components/forms/LoginForm.tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '@/hooks/useAuth';
import { normalizeError } from '@/lib/errors';

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginForm() {
  const { login } = useAuth();
  const [apiError, setApiError] = useState<string | null>(null);
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setApiError(null);
    try {
      await login(data.email, data.password);
      // AuthContext handles navigation
    } catch (error) {
      const { message } = normalizeError(error);
      setApiError(message);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-md mx-auto">
      {apiError && (
        <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded" role="alert">
          {apiError}
        </div>
      )}
      
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
          Email
        </label>
        <input
          {...register('email')}
          type="email"
          id="email"
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          aria-describedby={errors.email ? 'email-error' : undefined}
        />
        {errors.email && (
          <p id="email-error" className="mt-1 text-sm text-red-600" role="alert">
            {errors.email.message}
          </p>
        )}
      </div>

      <div>
        <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
          Password
        </label>
        <input
          {...register('password')}
          type="password"
          id="password"
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          aria-describedby={errors.password ? 'password-error' : undefined}
        />
        {errors.password && (
          <p id="password-error" className="mt-1 text-sm text-red-600" role="alert">
            {errors.password.message}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isSubmitting ? 'Logging in...' : 'Log In'}
      </button>
    </form>
  );
}

// pages/LoginPage.tsx
import { LoginForm } from '@/components/forms/LoginForm';
import { Link } from 'react-router-dom';

export function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md">
        <h1 className="text-3xl font-bold text-center mb-8">Log In to LeagueHQ</h1>
        <LoginForm />
        <p className="text-center mt-4 text-sm text-gray-600">
          Don't have an account?{' '}
          <Link to="/signup" className="text-blue-600 hover:underline">
            Sign up
          </Link>
        </p>
      </div>
    </div>
  );
}
```

---

## Ready to Ship 🚀

You now have:
- ✅ Complete frontend guidelines aligned with backend
- ✅ Fixed type mismatches (roles, statuses, routes)
- ✅ Removed Stripe Elements (using Checkout redirect)
- ✅ Error normalization helper
- ✅ Auth interceptor with event-based logout
- ✅ ESLint + Prettier + TypeScript strict mode
- ✅ Accessibility patterns (ARIA, focus management)
- ✅ Complete working example (login flow)
- ✅ Weekly checklists matching backend progress

**Next Steps:**
1. Run `npm create vite@latest leaguehq-frontend -- --template react-ts`
2. Copy the configs (tsconfig, eslint, prettier, .nvmrc)
3. Install dependencies
4. Create the folder structure
5. Start with Week 1 checklist (auth slice)

**Critical Reminders:**
- Enum values MUST match backend schema exactly:
  - Stripe Connect: NOT_STARTED, INCOMPLETE, VERIFIED, BLOCKED
  - Payout: NONE, PENDING, ENABLED, BLOCKED
  - Competition: DRAFT, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, COMPLETED, CANCELLED
  - Subscription: ACTIVE, PAST_DUE, CANCELLED, INCOMPLETE, TRIALING
  - Match: SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED, DISPUTED
- Field names: camelCase in TypeScript matching Jackson JSON output (fullName, createdAt, etc.)
- Money: Always in cents (entryFeeCents, amountCents) - use formatCurrency() helper
- Dates: ISO 8601 strings from backend - use date-fns for formatting
- AuthProvider MUST be inside BrowserRouter (uses useNavigate)
- Public competition route uses token, not ID: `/c/:token` → backend `GET /api/competitions/share/:token`
- MVP uses single venue per competition (venue property, not array)
- Policy defaults exist on backend - frontend can send partial or omit entirely

Ship working UI weekly, aligned with your backend. No over-engineering. No premature optimization. Just clean, typed, accessible React code that works.