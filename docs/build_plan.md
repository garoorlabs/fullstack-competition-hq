LeagueHQ - Final Build Plan (6-7 Weeks)
Status: LOCKED - Ready to Execute
Approach: Iterative (ship working features weekly)
Stack: Spring Boot 3.2 + JPA + PostgreSQL 15 + React 18 + Vite

Tech Stack
Backend:

Spring Boot 3.2
Spring Data JPA (Hibernate)
PostgreSQL 15
Spring Security + JWT
Stripe Java SDK
Maven
Java 17+

Frontend:

React 18
Vite
Tailwind CSS
Axios

Infrastructure:

Railway ($10/mo) - Backend + PostgreSQL
Vercel (free) - Frontend
Cloudflare R2 or AWS S3 - Photo storage

Database:

V1__core.sql (10 tables, 1 view, 2 triggers)
Flyway migrations


Phase 1: Foundation (Weeks 1-3)
Week 1: Authentication
Goal: Users can sign up, log in, and receive JWT tokens
Build:

Spring Boot project setup
Flyway integration (runs V1__core.sql)
User entity + UserRepository
AuthService (signup, login with BCrypt)
JwtTokenProvider utility
Spring Security configuration
AuthController (POST /signup, /login, GET /me)

Success Criteria:

User can sign up via Postman
User can log in and receive JWT
Protected endpoint requires valid JWT
Database has users table with data

Week 2: Competition CRUD
Goal: Owners can create and view competitions
Build:

Competition entity + CompetitionRepository
Venue entity + VenueRepository
CompetitionService (create, findById, findByOwner)
CompetitionController (POST, GET /my, GET /:id)
Share token generation (random 22+ chars)
Policy JSON handling (use defaults from schema)

Success Criteria:

Owner can create competition with one venue
Owner can list their competitions
Public competition detail page data available
Share token generated on create

Week 3: Stripe Connect Onboarding
Goal: Owners can connect Stripe accounts
Build:

StripeService (createConnectOnboardingLink, refreshAccountStatus)
Update User with stripe_connect_account_id
Webhook endpoint (POST /stripe/webhooks)
Handle account.updated webhook
Update payout_status based on Stripe verification
Manual refresh endpoint (POST /stripe/refresh-account-status) for fallback
Frontend: StripeReturn page with dual status verification (webhook + polling)
Block publish until payout_status = ENABLED

Success Criteria:

Owner clicks "Connect Stripe" → redirects to Stripe
After onboarding, payout_status updates to ENABLED
Webhook signature verification works
Manual refresh ensures status updates even if webhooks delayed
Can't publish competition until Stripe connected
Return page shows success when account verified


Phase 2: Team Registration & Payments (Weeks 4-5)
Week 4: Team Registration
Goal: Coaches can register teams and pay
Build:

Team entity + TeamRepository
TeamService (register team, create checkout session)
Stripe Checkout integration (entry fee + $20 first month)
Checkout line items with metadata (team_id, competition_id)
TeamController (POST /teams)
Success/cancel URLs

Success Criteria:

Coach clicks register → team created in database
Redirects to Stripe Checkout
Checkout shows entry fee + $20 subscription
Can cancel and return to competition page

Week 5: Payment Processing & Subscriptions
Goal: Payments split correctly, subscriptions created
Build:

PaymentTransaction entity + repository
SubscriptionEvent entity + repository
StripeWebhookController enhancements
Handle checkout.session.completed:

Split payment (entry fee to owner minus 8%, $20 to platform)
Create subscription via Stripe API
Update team (entry_fee_paid, subscription_id)
Create payment_transactions records


Handle invoice.payment_succeeded
Handle invoice.payment_failed (set PAST_DUE)
Team dashboard showing subscription status

Success Criteria:

Payment completes in Stripe
Money splits correctly in database
Subscription created and bills monthly
Failed payment updates status to PAST_DUE
Coach sees subscription status in dashboard


Phase 3: Roster Management (Week 6)
Week 6: Players & Photos
Goal: Coaches can add players with photos
Build:

Player entity + PlayerRepository
Photo upload endpoint (MultipartFile)
Image processing service:

Strip EXIF data
Resize to max 1080px width
Compress to <500KB
Upload to S3/Cloudflare R2


RosterService (addPlayer, updatePlayer, deletePlayer)
Roster lock check (can't edit after competition.start_date)
PlayerController (POST, PATCH, DELETE /players)

Success Criteria:

Coach uploads 5MB photo → stored as <500KB
Photo URL saved in players table
roster_size auto-increments (database trigger)
Can't add players after roster locked
Roster displays with photos in grid


Phase 4: Matches & Standings (Week 7)
Week 7: Schedule, Results, Standings
Goal: Owners can create matches, enter results, display standings
Build:

Match entity + MatchRepository
MatchService (createMatch, enterResult)
Results validation (scores non-negative, teams exist)
MatchController (POST /matches, PATCH /:id/result, GET /competitions/:id/matches)
StandingsService (query standings view)
StandingsController (GET /competitions/:id/standings)

Success Criteria:

Owner creates 10 matches manually
Owner enters 3 results
Standings query returns correct data
Standings sorted by points → goal_diff → goals_for
Public standings page shows data
Match with NULL scores excluded from standings


What We're NOT Building (Backlog)
Deferred to Post-Launch:

Schedule generator (manual match creation works)
Refunds UI (manual via Stripe dashboard)
Financial dashboards (use Stripe Connect/admin)
Grace period automation (manual eligibility updates)
Failed payment alerts (check Stripe daily)
Player accounts (coach-entered rosters only)
GDPR export/delete UI (policy page only)
CSV team import
Email verification
Audit tables (add when compliance needed)

Manual Processes (Until Volume Justifies):

Refunds: Process via Stripe dashboard, log in payment_transactions
Password resets: Send emails manually
Failed payments: Check Stripe, manually update is_eligible
Financials: Owners use Stripe Connect dashboard, you use admin panel


API Endpoints (Built Incrementally)
Week 1 (Auth):

POST /api/auth/signup
POST /api/auth/login
GET /api/auth/me

Week 2 (Competitions):

POST /api/competitions
GET /api/competitions/my
GET /api/competitions/:id

Week 3 (Stripe):

POST /api/stripe/connect-onboarding-link
POST /api/stripe/refresh-account-status
POST /api/stripe/webhooks

Week 4-5 (Teams):

POST /api/teams
GET /api/teams/my
GET /api/teams/:id

Week 6 (Roster):

POST /api/teams/:id/players
PATCH /api/players/:id
DELETE /api/players/:id

Week 7 (Matches):

POST /api/matches
GET /api/competitions/:id/matches
PATCH /api/matches/:id/result
GET /api/competitions/:id/standings


Database Schema
V1__core.sql - 10 Tables:

users
competitions
venues
teams
players
matches
payment_transactions
subscription_events

Plus:

standings VIEW (regular, not materialized)
Triggers: updated_at timestamps, roster_size counter
All indices for fast lookups

V2__enhancements.sql (post-launch):

Audit tables
CSV import tables
Email notifications queue
Refund requests table
Optional: Convert standings to materialized view


Weekly Success Criteria
Week 1: Can sign up, log in, get JWT
Week 2: Can create competition, view competitions
Week 3: Can connect Stripe, webhook updates status
Week 4: Can register team, redirects to Stripe
Week 5: Payment splits correctly, subscription created
Week 6: Can add players with photos
Week 7: Can create matches, enter results, view standings
After Week 7: Launchable MVP

Launch Readiness Checklist

 Owner creates competition + connects Stripe
 Coach registers + pays + payment splits correctly
 Subscription bills monthly
 Coach adds roster with photos
 Owner creates 10 matches manually
 Owner enters results
 Standings calculate correctly
 All Stripe webhooks tested


Post-Launch Strategy
First 30 Days:

Get 5-10 real customers
Track: payment success rate, subscription retention, complaints
Document: refund requests, password reset requests, failed payment frequency

Build Backlog Features When:

3+ customers ask for schedule generator
5+ refunds per month
Customers complain about financials in Stripe
Manual processes become time-consuming

Don't build until customers demand it.