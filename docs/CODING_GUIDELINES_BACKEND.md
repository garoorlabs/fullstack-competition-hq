Yes, this is backend only. You'll need separate guidelines for React frontend later.

---

# CODING_GUIDELINES.md

```markdown
# LeagueHQ - Backend Coding Guidelines (MVP)

## Scope
Spring Boot 3.2 + JPA + PostgreSQL backend for 6-7 week MVP.
Frontend guidelines tracked separately.

## Principles
- Ship working slices weekly
- Pick boring tech and defaults
- Keep code obvious; comment WHY, not WHAT
- Avoid premature optimization

## Package Structure
```
com.leaguehq
├─ model         // JPA entities
├─ repository    // Spring Data JPA interfaces
├─ service       // business logic
├─ controller    // REST endpoints
├─ dto           // request/response objects
│  ├─ request
│  └─ response
├─ security      // JWT config, filters
├─ config        // Flyway, CORS, app config
└─ exception     // custom exceptions, global handler
```

## API Conventions

**Base Path:** `/api` (no versioning for MVP)

**Controllers:**
- Thin layer - delegate to services
- Return DTOs, never entities
- Use `@Valid` for request validation
- Return proper HTTP status codes (200, 201, 400, 401, 404, 500)

**Error Responses:**
Global exception handler returns consistent format:
```json
{
  "status": 400,
  "message": "Email already registered",
  "path": "/api/auth/signup",
  "timestamp": "2025-09-30T12:34:56Z"
}
```

**DTOs:**
- Separate request/response objects
- Use Java records (Java 17+) or simple classes
- Validation annotations: `@NotNull`, `@Email`, `@Size`, `@Min`
- No business logic in DTOs

## Entities & Persistence

**Entities:**
- Map 1:1 with database schema (V1__core.sql)
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- UUID primary keys (already in schema)
- `@CreatedDate` and `@LastModifiedDate` for timestamps

**Relationships:**
- `@ManyToOne(fetch = FetchType.LAZY)` by default
- Avoid bidirectional unless needed
- Let database handle cascades (already in schema)

**Queries:**
- Use Spring Data JPA methods (findBy, existsBy)
- Custom queries only when JPA can't express it
- For complex queries (standings), query views directly

## Services

**Business Logic:**
- One service per entity (UserService, CompetitionService, TeamService)
- Business rules and validation live here, not controllers
- Annotate write operations with `@Transactional`
- Keep methods small and focused
- Name methods by intent: `registerTeam()` not `createTeamAndCheckout()`

**Return Types:**
- Return DTOs from services, not entities
- Prevents lazy loading issues in controllers

## Security

**Authentication:**
- Spring Security + JWT
- JWT expires in 24 hours
- BCrypt password hashing (strength 10)
- Store JWT in Authorization header: `Bearer {token}`

**Endpoints:**
- Public: `/api/auth/**`, `/api/competitions/{id}` (read-only)
- Protected: Everything else requires JWT

**CORS:**
- Allow frontend origin only (Vercel domain)
- Credentials: true (for cookies if needed later)

**Never Log:**
- Passwords
- JWT tokens
- Stripe API keys or secrets
- Credit card data (you won't have this, but principle applies)

## Logging

**SLF4J Levels:**
- `ERROR`: Exceptions, payment failures, webhook errors, system failures
- `WARN`: Validation failures, retries, suspicious activity
- `INFO`: Business events (user registered, team paid, result entered)
- `DEBUG`: Detailed flow for local debugging (disable in prod)

**Format:**
- Keep messages short
- Include relevant IDs: `log.info("Team registered: teamId={}, competitionId={}", teamId, compId)`
- Log before Stripe API calls and after responses

## Stripe Integration

**Webhooks:**
- Verify signature using Stripe webhook secret
- Handle idempotency (Stripe retries failed webhooks)
- Log event type + id: `log.info("Webhook received: type={}, id={}", type, eventId)`

**Events to Handle:**
- `checkout.session.completed` - Split payment, create subscription
- `invoice.payment_succeeded` - Update subscription status
- `invoice.payment_failed` - Mark team PAST_DUE
- `account.updated` - Update owner payout status

**Data Storage:**
- Write `payment_transactions` and `subscription_events` records
- Audit tables deferred to V2

## Testing

**Week 1:**
- Manual testing with Postman
- Verify each endpoint works end-to-end

**Week 2+:**
- One integration test per major slice (auth, team registration, webhooks)
- Unit tests for complex business logic (payment splits, standings calculation)
- Use real PostgreSQL for integration tests (local instance)
- Testcontainers deferred to post-launch

**Test Naming:**
- `shouldCreateUserWhenValidDataProvided()`
- `shouldRejectDuplicateEmail()`

## Configuration

**Flyway:**
- All schema changes via migrations
- V1__core.sql already created
- Never alter database manually
- Next migration: V2__enhancements.sql (post-launch)

**Environment Variables:**
- `DATABASE_URL` - PostgreSQL connection
- `JWT_SECRET` - JWT signing key (min 32 chars)
- `STRIPE_SECRET_KEY` - Stripe API key
- `STRIPE_WEBHOOK_SECRET` - Webhook signature verification
- `CORS_ALLOWED_ORIGINS` - Frontend URL

**application.yml:**
- `application-dev.yml` for local development
- `application-prod.yml` for Railway deployment
- Never commit secrets

## What NOT to Do

- Don't expose entities in REST responses (use DTOs)
- Don't put business logic in controllers
- Don't use `@Autowired` on fields (use constructor injection)
- Don't catch exceptions without logging
- Don't write custom SQL unless JPA truly can't handle it
- Don't add dependencies without reason (keep pom.xml lean)
- Don't build features not in the locked scope

## Comments

- Explain WHY, not WHAT
- Document complex business logic
- No commented-out code in commits

## Week 1 Implementation Checklist ✅ COMPLETE

**Project Setup:**
- [x] Spring Boot 3.2 (Web, Security, Validation, JPA, PostgreSQL, Flyway)
- [x] Add V1__core.sql to `src/main/resources/db/migration/`
- [x] Configure application-dev.yml with local database

**Auth Slice:**
- [x] User entity (maps to users table)
- [x] UserRepository extends JpaRepository
- [x] AuthService: signup(email, password, name, role), login(email, password)
- [x] JwtTokenProvider: generateToken(), validateToken(), getUserIdFromToken()
- [x] SecurityConfig: permit /api/auth/**, secure everything else
- [x] AuthController: POST /signup, POST /login, GET /me

**Success Criteria:**
- [x] Application starts, Flyway creates tables
- [x] Can signup via Postman
- [x] Can login and receive JWT
- [x] Can call /api/auth/me with JWT and get user details
- [x] Invalid credentials rejected
- [x] Duplicate email rejected

## Week 2 Implementation Checklist ✅ COMPLETE

**Competition CRUD:**
- [x] Competition entity (maps to competitions table)
- [x] Venue entity (maps to venues table)
- [x] CompetitionRepository with findByOwner, findByShareToken methods
- [x] VenueRepository with findByCompetition method
- [x] CreateCompetitionRequest DTO with validation
- [x] CompetitionResponse and VenueResponse DTOs
- [x] CompetitionService: createCompetition(), findByOwner(), findById()
- [x] Share token generation (22+ chars, secure random)
- [x] Default policy JSON handling
- [x] CompetitionController: POST /, GET /my, GET /:id
- [x] SecurityConfig: permit public access to GET /competitions/:id

**Success Criteria:**
- [x] Owner can create competition with one venue
- [x] Owner can list their competitions (GET /my)
- [x] Public competition detail page data available (GET /:id without auth)
- [x] Share token generated automatically on create
- [x] Default policy applied if not provided

## Week 3 Implementation Checklist ✅ COMPLETE

**Stripe Connect Onboarding:**
- [x] StripeService with createConnectOnboardingLink(), handleAccountUpdated()
- [x] Stripe Connect account creation (Standard type)
- [x] Account link generation with return/refresh URLs
- [x] ConnectOnboardingLinkResponse DTO
- [x] StripeController: POST /connect-onboarding-link
- [x] StripeWebhookController: POST /webhooks with signature verification
- [x] Handle account.updated webhook event
- [x] Update User with stripeConnectAccountId, stripeConnectStatus, payoutStatus
- [x] UserRepository: findByStripeConnectAccountId method
- [x] CompetitionService: publishCompetition() with payout validation
- [x] CompetitionController: POST /:id/publish
- [x] SecurityConfig: permit /api/stripe/webhooks

**Success Criteria:**
- [x] Owner can request Stripe Connect onboarding link
- [x] After onboarding, webhook updates payout_status to ENABLED
- [x] Webhook signature verification works
- [x] Can't publish competition until Stripe connected (payoutStatus = ENABLED)
- [x] Publish endpoint validates ownership and Stripe status

## Notes

- These guidelines prioritize shipping over perfection
- Refactor when patterns become clear, not before
- If something feels "extra" for your situation, skip it
- Add complexity only when pain is real, not anticipated
```

---

## For Frontend (Later)

When you start React in Week 2-3, you'll need a separate `FRONTEND_GUIDELINES.md` covering:
- React component structure
- API integration patterns
- Tailwind conventions
- State management approach
- Error handling

But that's not needed yet. Focus on backend first.

**Ready to give this to Claude Code and start building?**