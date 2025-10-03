# LeagueHQ - Complete Build Plan (8 Weeks MVP)
**Status:** UPDATED - Gap Analysis Complete
**Approach:** Iterative (ship working features weekly)
**Stack:** Spring Boot 3.2 + JPA + PostgreSQL 15 + React 18 + Vite

---

## Tech Stack

### Backend:
- Spring Boot 3.2
- Spring Data JPA (Hibernate)
- PostgreSQL 15
- Spring Security + JWT
- Stripe Java SDK
- Maven
- Java 17+

### Frontend:
- React 18
- Vite
- Tailwind CSS
- Axios
- React Router

### Infrastructure:
- Railway ($10/mo) - Backend + PostgreSQL
- Vercel (free) - Frontend
- Cloudflare R2 or AWS S3 - Photo storage

### Database:
- V1__core.sql (10 tables, 1 view, 2 triggers)
- Flyway migrations

---

## Phase 1: Foundation (Weeks 1-3)

### Week 1: Authentication & User Management
**Goal:** Users can sign up, log in, and receive JWT tokens

**Backend:**
- Spring Boot project setup (Maven, dependencies)
- Flyway integration (runs V1__core.sql)
- User entity + UserRepository
- AuthService (signup, login with BCrypt)
- JwtTokenProvider utility
- Spring Security configuration
- AuthController (POST /signup, /login, GET /me)

**Frontend:**
- Vite + React project setup
- Tailwind CSS configuration
- Login page (`/login`)
- Signup page (`/signup`)
- Auth context/hook (useAuth)
- Protected routes
- Header component with logout

**Success Criteria:**
- ✅ User can sign up as COMPETITION_OWNER or COACH
- ✅ User can log in and receive JWT
- ✅ Protected endpoints require valid JWT
- ✅ Frontend stores token and maintains session
- ✅ Database has users table with role-based data

---

### Week 2: Competition CRUD
**Goal:** Owners can create, view, and browse competitions

**Backend:**
- Competition entity + CompetitionRepository
- Venue entity + VenueRepository
- CompetitionService (create, findById, findByOwner, findPublished)
- CompetitionController (POST, GET /my, GET /published, GET /:id)
- Share token generation (random 22+ chars)
- Policy JSON handling (use defaults from schema)

**Frontend:**
- My Competitions page (`/competitions` - owner view)
- Create Competition page (`/competitions/new`)
- Competition Detail page (`/competitions/:id`)
- Browse Competitions page (for coaches)
- Status badges component
- Competition cards/grid layout

**Success Criteria:**
- ✅ Owner can create competition with venue
- ✅ Owner can list their competitions
- ✅ Coaches can browse published competitions
- ✅ Public competition detail page shows data
- ✅ Share token generated on create
- ✅ Competition shows status (DRAFT, PUBLISHED, ACTIVE, etc.)

---

### Week 3: Stripe Connect Onboarding
**Goal:** Owners can connect Stripe accounts and receive payouts

**Backend:**
- StripeService (createConnectOnboardingLink, refreshAccountStatus)
- Update User with stripe_connect_account_id
- Webhook endpoint (POST /stripe/webhooks)
- Handle `account.updated` webhook
- Update payout_status based on Stripe verification
- Manual refresh endpoint (POST /stripe/refresh-account-status)
- Block publish until payout_status = ENABLED

**Frontend:**
- Stripe Connect button on Competition Detail (owner view)
- StripeReturn page (`/stripe/return`)
- StripeRefresh page (`/stripe/refresh`)
- Dual status verification (webhook + polling)
- Visual indicators for Stripe connection status
- Alert when Stripe not connected

**Success Criteria:**
- ✅ Owner clicks "Connect Stripe" → redirects to Stripe onboarding
- ✅ After onboarding, payout_status updates to ENABLED
- ✅ Webhook signature verification works
- ✅ Manual refresh ensures status updates (fallback for webhook delays)
- ✅ Can't publish competition until Stripe connected
- ✅ Return page shows success when account verified
- ✅ Competition publish endpoint validates Stripe status

---

## Phase 2: Team Registration & Payments (Weeks 4-5)

### Week 4: Team Registration & Checkout
**Goal:** Coaches can register teams and initiate payment

**Backend:**
- Team entity + TeamRepository
- TeamService (registerTeam, createCheckoutSession, getMyTeams, getTeamById)
- Stripe Checkout integration (SUBSCRIPTION mode)
- Entry fee as one-time line item
- $20 monthly subscription as recurring line item
- Application fee split (8% to platform, 92% to owner)
- TeamController (POST /teams, GET /my, GET /:id)
- Checkout metadata (team_id, competition_id, team_name)
- Success/cancel URLs
- Validation (competition published, not full, unique team name)

**Frontend:**
- Register Team page (`/competitions/:id/register`)
- Team registration form (team name input)
- My Teams page (`/teams` - coach view)
- Team cards showing subscription status
- Redirect to Stripe Checkout on submit
- Handle checkout cancellation

**Success Criteria:**
- ✅ Coach clicks register → team created in database
- ✅ Redirects to Stripe Checkout
- ✅ Checkout shows entry fee + $20 subscription
- ✅ Checkout metadata includes all required fields
- ✅ Can cancel and return to competition page
- ✅ Team validation (published competition, capacity check, unique name)
- ✅ Coach can view their teams list

---

### Week 5: Payment Processing & Subscriptions
**Goal:** Payments process correctly, money splits as designed, subscriptions created

**Backend:**
- PaymentTransaction entity + repository
- SubscriptionEvent entity + repository
- StripeWebhookController enhancements:
  - Handle `checkout.session.completed`:
    - Retrieve session with expanded customer/subscription/invoice
    - Update team: entryFeePaid = true, subscriptionId, stripeCustomerId
    - Create payment_transactions record (entry fee split)
    - Create subscription_events record (CREATED)
    - Verify subscription created and active
  - Handle `invoice.payment_succeeded`:
    - Log subscription renewal
    - Create payment_transactions record
    - Update team subscription status to ACTIVE
  - Handle `invoice.payment_failed`:
    - Update team subscription status to PAST_DUE
    - Create subscription_events record (PAYMENT_FAILED)
    - Log for manual follow-up
- Subscription tracking on Team entity

**Frontend:**
- Team Registration Success page (`/teams/registration/success`)
- My Teams page enhancements:
  - Subscription status badges (ACTIVE, PAST_DUE, CANCELLED)
  - Entry fee paid indicator
  - Roster size display
  - Action buttons based on status
- Loading states during webhook processing

**Success Criteria:**
- ✅ Payment completes in Stripe
- ✅ Entry fee splits correctly (8% platform, 92% owner)
- ✅ Subscription created with $20/month
- ✅ Team record updated (entry_fee_paid = true, subscription_id set)
- ✅ payment_transactions record created
- ✅ subscription_events record created
- ✅ Monthly subscription bills successfully
- ✅ Failed payment updates status to PAST_DUE
- ✅ Coach sees subscription status in My Teams
- ✅ Success page shows after payment

---

### Week 5.5: Owner Dashboard & Payment Recovery (CRITICAL GAPS)
**Goal:** Owners can see registered teams, coaches can fix payment failures

**Backend:**
- `GET /api/competitions/{id}/teams` endpoint
  - Returns all teams for a competition
  - Includes coach info, subscription status, roster size, registration date
  - Owner-only or public based on competition status
- Fix CompetitionResponse.currentTeamCount (query actual count)
- `POST /api/teams/{id}/update-payment` endpoint
  - Creates Stripe Customer Portal session
  - Allows coach to update payment method
  - Returns portal URL

**Frontend:**
- Competition Detail - Add Tabs component:
  - Overview tab (existing content)
  - **Teams tab** (NEW):
    - Table of registered teams
    - Columns: Team Name, Coach, Registration Date, Subscription Status, Roster Size
    - Click team → view details
  - Matches tab (placeholder for Week 7)
  - Standings tab (placeholder for Week 7)
- Update currentTeamCount display with real data
- Update Payment page (`/teams/:id/payment`):
  - Redirect to Stripe Customer Portal
  - Allow coaches to update payment method for PAST_DUE subscriptions
- Public competition view: Add "Registered Teams" section
  - Show list of team names (no sensitive coach data)

**Success Criteria:**
- ✅ Owner can see all teams registered for their competition
- ✅ Owner can see coach names, contact info, payment status
- ✅ Team count is accurate (not hardcoded to 0)
- ✅ Tabs work correctly on competition detail page
- ✅ Coach with PAST_DUE subscription can update payment
- ✅ Update Payment button links to working page
- ✅ Public can see list of registered teams

---

## Phase 3: Roster Management (Week 6)

### Week 6: Players & Photos
**Goal:** Coaches can add players with photos, owners can view rosters

**Backend:**
- Player entity + PlayerRepository
- Photo upload endpoint (POST /api/teams/:id/players/upload-photo)
- Image processing service:
  - Strip EXIF data (privacy)
  - Resize to max 1080px width
  - Compress to <500KB
  - Upload to S3/Cloudflare R2
  - Return photo URL
- PlayerService (addPlayer, updatePlayer, deletePlayer, getPlayersForTeam)
- Roster lock check (can't edit after competition.start_date)
- PlayerController:
  - POST /api/teams/:id/players
  - PATCH /api/players/:id
  - DELETE /api/players/:id
  - GET /api/teams/:id/players
- Jersey number uniqueness validation
- Roster size limits (policy.roster.min_size, policy.roster.max_size)

**Frontend:**
- Team Roster page (`/teams/:id/roster`):
  - Grid of player cards (4 columns desktop, 2 mobile)
  - Player photo, name, jersey number, position
  - Add Player button
  - Edit/Delete buttons per player
  - Roster size indicator (e.g., "12/20 players")
- Add/Edit Player modal:
  - Full name input
  - Jersey number input
  - Position dropdown
  - Photo upload with preview
  - File size validation (max 5MB)
  - Image preview before upload
- Photo fallback: Initials avatar if no photo
- Roster locked state (read-only after competition starts)

**Success Criteria:**
- ✅ Coach uploads 5MB photo → stored as <500KB
- ✅ Photo URL saved in players table
- ✅ roster_size auto-increments (database trigger)
- ✅ Can't add players after roster locked
- ✅ Roster displays with photos in grid
- ✅ Jersey number uniqueness enforced per team
- ✅ Can edit existing players
- ✅ Can delete players (only before roster lock)
- ✅ Owner can view team rosters from Teams tab
- ✅ Manage Roster button works from My Teams page

---

## Phase 4: Matches & Standings (Week 7)

### Week 7: Schedule, Results, Standings
**Goal:** Owners can create matches, enter results, display standings

**Backend:**
- Match entity + MatchRepository
- MatchService (createMatch, enterResult, getMatchesForCompetition)
- Results validation:
  - Scores non-negative
  - Teams exist and belong to competition
  - Only owner can enter results
  - Home team ≠ away team
- MatchController:
  - POST /api/matches
  - GET /api/competitions/:id/matches
  - PATCH /api/matches/:id/result
- StandingsService (query standings view)
  - Sort by points DESC, goal_diff DESC, goals_for DESC
  - Filter by competition
- StandingsController (GET /api/competitions/:id/standings)

**Frontend:**
- Competition Detail - Matches tab:
  - List of matches grouped by date
  - Match cards: teams, time, venue, status, score
  - Create Match button (owner only)
  - Enter Result / Edit Result buttons (owner only)
- Create Match modal:
  - Home team dropdown
  - Away team dropdown
  - Match date picker
  - Match time picker
  - Venue dropdown
- Enter Result modal:
  - Match details display
  - Home score input
  - Away score input
  - Match status (Completed, Postponed, Cancelled)
  - Save button
- Competition Detail - Standings tab:
  - Standings table
  - Columns: Pos, Team, Played, Won, Drawn, Lost, GF, GA, GD, Points
  - Sorted by points → goal_diff → goals_for
  - Last updated timestamp
  - Responsive (horizontal scroll on mobile)

**Success Criteria:**
- ✅ Owner creates matches manually
- ✅ Owner enters match results
- ✅ Standings query returns correct data
- ✅ Standings sorted correctly (points → GD → GF)
- ✅ Public standings page shows data
- ✅ Matches with NULL scores excluded from standings
- ✅ Standings update immediately after result entry
- ✅ Coaches can view standings for their competition

---

## Phase 5: Polish & Edge Cases (Week 8)

### Week 8: Production Readiness
**Goal:** Handle edge cases, improve UX, prepare for launch

**Backend:**
- Error handling improvements:
  - Consistent error responses
  - Validation error messages
  - Stripe error handling
- Logging enhancements:
  - Request/response logging
  - Payment event logging
  - Error tracking
- Performance optimization:
  - Database query optimization
  - Index verification
  - N+1 query prevention
- Security review:
  - CORS configuration
  - JWT expiration handling
  - Input validation
  - SQL injection prevention

**Frontend:**
- Loading states for all async operations
- Error handling & user-friendly messages
- Form validation improvements
- Empty states (no teams, no matches, etc.)
- Mobile responsiveness verification
- Accessibility improvements (ARIA labels, keyboard navigation)
- Toast notifications for success/error
- Confirmation dialogs for destructive actions
- 404 page
- Network error handling

**Testing:**
- End-to-end user flows:
  - Owner creates competition → connects Stripe → publishes
  - Coach registers team → pays → adds roster
  - Owner creates matches → enters results → standings update
- Stripe webhook testing (all scenarios)
- Payment failure scenarios
- Subscription renewal testing
- Edge cases:
  - Competition full
  - Registration deadline passed
  - Roster locked
  - Duplicate team names
  - Invalid match results

**Success Criteria:**
- ✅ No major bugs in critical user flows
- ✅ All Stripe webhooks tested and working
- ✅ Error messages are user-friendly
- ✅ Loading states prevent duplicate submissions
- ✅ Mobile experience is smooth
- ✅ Forms validate properly
- ✅ Edge cases handled gracefully
- ✅ Ready for beta users

---

## Complete API Endpoints Reference

### Auth (Week 1)
- `POST /api/auth/signup` - User signup
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user

### Competitions (Week 2)
- `POST /api/competitions` - Create competition
- `GET /api/competitions/my` - Get owner's competitions
- `GET /api/competitions/published` - Browse published competitions
- `GET /api/competitions/:id` - Get competition details

### Stripe (Week 3)
- `POST /api/stripe/connect-onboarding-link` - Start Stripe Connect
- `POST /api/stripe/refresh-account-status` - Manual status refresh
- `POST /api/stripe/webhooks` - Stripe webhook handler
- `POST /api/competitions/:id/publish` - Publish competition

### Teams (Week 4-5.5)
- `POST /api/teams` - Register team (creates checkout session)
- `GET /api/teams/my` - Get coach's teams
- `GET /api/teams/:id` - Get team details
- `GET /api/competitions/:id/teams` - Get teams for competition (Week 5.5)
- `POST /api/teams/:id/update-payment` - Update payment method (Week 5.5)

### Roster (Week 6)
- `POST /api/teams/:id/players` - Add player to roster
- `GET /api/teams/:id/players` - Get team roster
- `PATCH /api/players/:id` - Update player
- `DELETE /api/players/:id` - Delete player
- `POST /api/teams/:id/players/upload-photo` - Upload player photo

### Matches (Week 7)
- `POST /api/matches` - Create match
- `GET /api/competitions/:id/matches` - Get matches for competition
- `PATCH /api/matches/:id/result` - Enter/update match result
- `GET /api/competitions/:id/standings` - Get competition standings

---

## Database Schema

### V1__core.sql - 10 Tables:
- `users` - Authentication & roles
- `competitions` - Competition details
- `venues` - Competition venues
- `teams` - Registered teams
- `players` - Team rosters
- `matches` - Match schedule & results
- `payment_transactions` - Payment history
- `subscription_events` - Subscription event log

### Plus:
- `standings` VIEW - Calculated standings (regular, not materialized)
- Triggers: `updated_at` timestamps, `roster_size` counter
- All indices for fast lookups

### V2__enhancements.sql (post-launch):
- Audit tables (payment_audit, roster_audit, results_audit)
- CSV import tables (team_imports, team_invite_tokens)
- Email notifications queue
- Refund requests table
- Domain events table
- Optional: Materialized standings view (if performance needed)

---

## Weekly Success Criteria Summary

### Week 1: ✅ Authentication
- Can sign up, log in, get JWT, access protected routes

### Week 2: ✅ Competition CRUD
- Can create competition, view competitions, browse published

### Week 3: ✅ Stripe Connect
- Can connect Stripe, webhook updates status, can publish after verified

### Week 4: ✅ Team Registration
- Can register team, redirects to Stripe Checkout

### Week 5: ✅ Payment Processing
- Payment splits correctly, subscription created, webhooks work

### Week 5.5: ✅ Owner Dashboard & Payment Recovery
- Owner sees teams, team count accurate, coaches can fix payments

### Week 6: ✅ Roster Management
- Can add players with photos, roster displays, manage roster works

### Week 7: ✅ Matches & Standings
- Can create matches, enter results, view standings

### Week 8: ✅ Production Ready
- All flows tested, edge cases handled, ready for beta

---

## Launch Readiness Checklist

**Core Flows:**
- ✅ Owner creates competition + connects Stripe + publishes
- ✅ Coach registers team + pays (entry fee + subscription)
- ✅ Payment splits correctly (8% platform, 92% owner)
- ✅ Subscription bills monthly
- ✅ Coach adds roster with photos
- ✅ Owner views registered teams and rosters
- ✅ Owner creates matches and enters results
- ✅ Standings calculate correctly
- ✅ All Stripe webhooks tested

**Payment Edge Cases:**
- ✅ Failed payment sets PAST_DUE
- ✅ Coach can update payment method
- ✅ Subscription renewal works
- ✅ Entry fee split verified in Stripe dashboard

**Owner Features:**
- ✅ Can see all teams for their competition
- ✅ Can see subscription status for each team
- ✅ Can view team rosters
- ✅ Team count displays correctly

**Coach Features:**
- ✅ Can browse published competitions
- ✅ Can register and pay for team
- ✅ Can add/edit/delete players
- ✅ Can see subscription status
- ✅ Can update payment if needed
- ✅ "Manage Roster" button works

---

## What We're NOT Building (Backlog)

### Deferred to Post-Launch:
- Schedule generator (manual match creation works)
- Automated refunds UI (manual via Stripe dashboard)
- Financial dashboards (use Stripe Connect/admin)
- Grace period automation (manual eligibility updates)
- Automated failed payment alerts (check Stripe daily)
- Player accounts (coach-entered rosters only)
- GDPR export/delete UI (policy page only)
- CSV team import
- Email verification
- Password reset UI
- Audit tables (add when compliance needed)
- Advanced search/filters
- Email notifications
- Push notifications

### Manual Processes (Until Volume Justifies):
- **Refunds:** Process via Stripe dashboard, log in payment_transactions
- **Password resets:** Send emails manually
- **Failed payments:** Check Stripe, manually contact coaches
- **Financials:** Owners use Stripe Connect dashboard, platform uses admin panel
- **Customer support:** Manual email/phone support

---

## Post-Launch Strategy

### First 30 Days:
- Get 5-10 real customers
- Track: payment success rate, subscription retention, complaints
- Document: refund requests, password reset requests, failed payment frequency
- Monitor: Stripe webhooks, error logs, user feedback

### Build Backlog Features When:
- **3+ customers** ask for schedule generator
- **5+ refunds per month** → build refunds UI
- **Customers complain** about financials in Stripe → build dashboard
- **Manual processes** become time-consuming
- **Users request** specific features consistently

**Philosophy:** Don't build until customers demand it. Validate with real usage first.

---

## Technical Notes

### Stripe Integration:
- Use **SUBSCRIPTION mode** checkout (combines one-time + recurring)
- Entry fee: one-time line item on first invoice
- Monthly dues: recurring line item ($20/month)
- Application fee: 8% of entry fee to platform
- Subscription: 100% to platform (0% to owner)
- Webhooks required: `account.updated`, `checkout.session.completed`, `invoice.payment_succeeded`, `invoice.payment_failed`

### Image Processing:
- Strip EXIF data for privacy
- Resize to 1080px max width
- Compress to <500KB
- Support PNG, JPG, WebP
- Store in S3/Cloudflare R2
- Generate signed URLs if needed

### Performance:
- Database indices on foreign keys
- Eager loading for relationships
- Pagination for large lists (defer to post-launch if not needed)
- Query optimization (avoid N+1)

### Security:
- JWT with secure secret (256+ bits)
- BCrypt for passwords (strength 10+)
- CORS configured correctly
- Stripe webhook signature verification
- Input validation on all endpoints
- SQL injection prevention (JPA parameterized queries)

---

## Modified Timeline (8 Weeks Total)

| Week | Phase | Focus |
|------|-------|-------|
| 1 | Foundation | Auth & User Management |
| 2 | Foundation | Competition CRUD |
| 3 | Foundation | Stripe Connect |
| 4 | Teams & Payments | Team Registration & Checkout |
| 5 | Teams & Payments | Payment Processing & Subscriptions |
| 5.5 | **Critical Gaps** | **Owner Dashboard & Payment Recovery** |
| 6 | Roster | Players & Photos |
| 7 | Matches | Schedule, Results, Standings |
| 8 | Polish | Production Readiness |

**Week 5.5 is critical** - it fills the gaps found in our analysis. Without it, owners can't manage their competitions and coaches can't fix payment issues.

---

**Status:** Ready to execute. All gaps identified and planned. Week 5.5 added to prevent broken user flows.
