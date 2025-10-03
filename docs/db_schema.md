-- ============================================
-- LeagueHQ V1 Core Schema
-- PostgreSQL 15+
-- MVP: 6-7 Week Launch (10 Tables)
-- 
-- Flyway Migration: db/migration/V1__core.sql
-- Next: V2__enhancements.sql (post-launch)
--   - Audit tables (payments, roster, results, domain events)
--   - CSV import (team_imports, team_invite_tokens)
--   - Email queue (email_notifications)
--   - Refund requests (refund_requests)
--   - Optional: Materialized standings view
-- ============================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================
-- USERS & AUTHENTICATION
-- ============================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('PLATFORM_OWNER', 'COMPETITION_OWNER', 'COACH')),
    
    -- Stripe identifiers
    stripe_customer_id VARCHAR(255) UNIQUE,
    stripe_connect_account_id VARCHAR(255) UNIQUE,
    stripe_connect_status VARCHAR(50) DEFAULT 'NOT_STARTED' CHECK (stripe_connect_status IN ('NOT_STARTED', 'INCOMPLETE', 'VERIFIED', 'BLOCKED')),
    stripe_connect_onboarded_at timestamptz,
    payout_status VARCHAR(50) DEFAULT 'NONE' CHECK (payout_status IN ('NONE', 'PENDING', 'ENABLED', 'BLOCKED')),
    
    -- Metadata
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    last_login_at timestamptz
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_stripe_customer ON users(stripe_customer_id);
CREATE INDEX idx_users_stripe_connect ON users(stripe_connect_account_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_payout_status ON users(payout_status);

-- ============================================
-- COMPETITION MANAGEMENT
-- ============================================

CREATE TABLE competitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Basic info
    name VARCHAR(255) NOT NULL,
    description TEXT,
    format VARCHAR(50) NOT NULL CHECK (format IN ('LEAGUE', 'KNOCKOUT', 'ROUND_ROBIN')),
    team_size VARCHAR(50) NOT NULL CHECK (team_size IN ('FIVE_V_FIVE', 'SIX_V_SIX', 'SEVEN_V_SEVEN', 'EIGHT_V_EIGHT', 'NINE_V_NINE', 'ELEVEN_V_ELEVEN')),

    -- Financial
    entry_fee DECIMAL(10,2) NOT NULL CHECK (entry_fee >= 0),
    platform_fee_percentage DECIMAL(5,2) DEFAULT 8.00 CHECK (platform_fee_percentage >= 0 AND platform_fee_percentage <= 100),
    
    -- Competition policy (JSONB for flexibility)
    policy JSONB NOT NULL DEFAULT '{"scoring": {"win": 3, "draw": 1, "loss": 0}, "tiebreakers": ["goal_diff", "goals_for", "head_to_head"], "roster": {"min_size": 8, "max_size": 20, "lock_at": "competition_start"}, "refunds": {"full_refund_days_before": 14, "partial_refund_days_before": 7, "partial_refund_percentage": 50}}'::jsonb,
    
    -- Capacity
    max_teams INTEGER NOT NULL CHECK (max_teams >= 4 AND max_teams <= 50),
    
    -- Dates
    registration_deadline DATE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    
    -- Shareable registration link
    share_token VARCHAR(64) UNIQUE,
    
    -- Metadata
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz,
    
    -- Constraints
    CONSTRAINT chk_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_share_token_len CHECK (share_token IS NULL OR length(share_token) >= 22),
    CONSTRAINT chk_reg_deadline CHECK (registration_deadline IS NULL OR registration_deadline::date <= start_date)
);

CREATE INDEX idx_competitions_owner ON competitions(owner_id);
CREATE INDEX idx_competitions_status ON competitions(status);
CREATE INDEX idx_competitions_dates ON competitions(start_date, end_date);
CREATE INDEX idx_competitions_share_token ON competitions(share_token);
CREATE INDEX idx_competitions_policy_gin ON competitions USING GIN (policy);

CREATE TABLE venues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_venues_competition ON venues(competition_id);

-- ============================================
-- TEAMS & ROSTER
-- ============================================

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    coach_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    
    -- Payment tracking
    entry_fee_paid BOOLEAN DEFAULT FALSE,
    entry_fee_paid_at timestamptz,
    entry_fee_stripe_payment_id VARCHAR(255),
    
    -- Subscription tracking
    subscription_id VARCHAR(255) UNIQUE,
    subscription_status VARCHAR(50) CHECK (subscription_status IN ('ACTIVE', 'PAST_DUE', 'CANCELLED', 'INCOMPLETE', 'TRIALING')),
    subscription_current_period_start timestamptz,
    subscription_current_period_end timestamptz,
    subscription_cancel_at timestamptz,

    -- Stripe reconciliation (V3)
    stripe_customer_id VARCHAR(255),
    stripe_latest_invoice_id VARCHAR(255),
    stripe_latest_payment_intent_id VARCHAR(255),

    -- Eligibility
    is_eligible BOOLEAN DEFAULT TRUE,
    
    -- Roster status (auto-updated by trigger)
    roster_size INTEGER DEFAULT 0 CHECK (roster_size >= 0),
    roster_locked BOOLEAN DEFAULT FALSE,
    roster_locked_at timestamptz,
    
    -- Metadata
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    registered_at timestamptz,
    
    UNIQUE(competition_id, name)
);

CREATE INDEX idx_teams_competition ON teams(competition_id);
CREATE INDEX idx_teams_coach ON teams(coach_id);
CREATE INDEX idx_teams_subscription ON teams(subscription_id);
CREATE INDEX idx_teams_stripe_customer ON teams(stripe_customer_id);
CREATE INDEX idx_teams_eligibility ON teams(is_eligible);
CREATE INDEX idx_teams_subscription_status ON teams(subscription_status);

CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    jersey_number INTEGER CHECK (jersey_number >= 0 AND jersey_number <= 99),
    position VARCHAR(50),
    
    -- Photo (uploaded by coach)
    photo_url TEXT,
    photo_uploaded_at timestamptz,
    photo_size_bytes INTEGER CHECK (photo_size_bytes >= 0),
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    
    UNIQUE(team_id, jersey_number)
);

CREATE INDEX idx_players_team ON players(team_id);

-- ============================================
-- MATCHES & RESULTS
-- ============================================

CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    home_team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    away_team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    
    -- Scheduling
    match_date DATE NOT NULL,
    match_time TIME,
    venue_id UUID REFERENCES venues(id) ON DELETE SET NULL,
    
    -- Results (NULL = not entered, 0 = actual zero score)
    home_score INTEGER CHECK (home_score IS NULL OR home_score >= 0),
    away_score INTEGER CHECK (away_score IS NULL OR away_score >= 0),
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'POSTPONED', 'DISPUTED')),
    
    -- Result tracking
    result_entered_by UUID REFERENCES users(id),
    result_entered_at timestamptz,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    
    CHECK (home_team_id <> away_team_id)
);

CREATE INDEX idx_matches_competition ON matches(competition_id);
CREATE INDEX idx_matches_teams ON matches(home_team_id, away_team_id);
CREATE INDEX idx_matches_date ON matches(match_date);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_completed_comp ON matches(competition_id) WHERE status = 'COMPLETED';
CREATE INDEX idx_matches_comp_date_time ON matches(competition_id, match_date, match_time);

-- ============================================
-- PAYMENTS & SUBSCRIPTIONS
-- ============================================

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Stripe IDs
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_charge_id VARCHAR(255),
    stripe_checkout_session_id VARCHAR(255),
    
    -- Related entities
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    competition_id UUID REFERENCES competitions(id) ON DELETE SET NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    
    -- Amount (stored in cents)
    amount_cents INTEGER NOT NULL CHECK (amount_cents >= 0),
    platform_fee_cents INTEGER DEFAULT 0 CHECK (platform_fee_cents >= 0),
    net_to_owner_cents INTEGER DEFAULT 0 CHECK (net_to_owner_cents >= 0),
    currency VARCHAR(3) DEFAULT 'USD' CHECK (currency ~ '^[A-Z]{3}$'),
    
    -- Type (includes REFUND for future-proofing)
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('ENTRY_FEE', 'SUBSCRIPTION', 'REFUND')),
    
    -- Status
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED')),
    
    -- Refund tracking (for manual refunds via Stripe dashboard)
    refunded_amount_cents INTEGER DEFAULT 0 CHECK (refunded_amount_cents >= 0),
    refunded_at timestamptz,
    
    stripe_created_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    
    -- Constraint: entry fee split must be correct
    CONSTRAINT chk_amount_split CHECK (
        transaction_type <> 'ENTRY_FEE' 
        OR amount_cents = platform_fee_cents + net_to_owner_cents
    )
);

CREATE INDEX idx_transactions_team ON payment_transactions(team_id);
CREATE INDEX idx_transactions_competition ON payment_transactions(competition_id);
CREATE INDEX idx_transactions_user ON payment_transactions(user_id);
CREATE INDEX idx_transactions_stripe_payment ON payment_transactions(stripe_payment_intent_id);
CREATE INDEX idx_transactions_type ON payment_transactions(transaction_type);
CREATE INDEX idx_transactions_status ON payment_transactions(status);
CREATE INDEX idx_transactions_created ON payment_transactions(created_at DESC);

CREATE TABLE subscription_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    subscription_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN ('CREATED', 'RENEWED', 'PAYMENT_FAILED', 'CANCELLED', 'PAST_DUE', 'GRACE_PERIOD_STARTED', 'REACTIVATED')),
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    stripe_event_id VARCHAR(255),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscription_events_team ON subscription_events(team_id);
CREATE INDEX idx_subscription_events_subscription ON subscription_events(subscription_id);
CREATE INDEX idx_subscription_events_type ON subscription_events(event_type);

-- ============================================
-- STANDINGS (Regular View)
-- ============================================

CREATE VIEW standings AS
WITH match_results AS (
    SELECT 
        m.competition_id,
        m.home_team_id AS team_id,
        CASE 
            WHEN m.home_score > m.away_score THEN 'W'
            WHEN m.home_score < m.away_score THEN 'L'
            ELSE 'D'
        END AS result,
        m.home_score AS goals_for,
        m.away_score AS goals_against
    FROM matches m
    WHERE m.status = 'COMPLETED'
    
    UNION ALL
    
    SELECT 
        m.competition_id,
        m.away_team_id AS team_id,
        CASE 
            WHEN m.away_score > m.home_score THEN 'W'
            WHEN m.away_score < m.home_score THEN 'L'
            ELSE 'D'
        END AS result,
        m.away_score AS goals_for,
        m.home_score AS goals_against
    FROM matches m
    WHERE m.status = 'COMPLETED'
)
SELECT 
    t.id AS team_id,
    t.name AS team_name,
    t.competition_id,
    c.policy,
    COUNT(mr.result) AS played,
    SUM(CASE WHEN mr.result = 'W' THEN 1 ELSE 0 END) AS won,
    SUM(CASE WHEN mr.result = 'D' THEN 1 ELSE 0 END) AS drawn,
    SUM(CASE WHEN mr.result = 'L' THEN 1 ELSE 0 END) AS lost,
    COALESCE(SUM(mr.goals_for), 0) AS goals_for,
    COALESCE(SUM(mr.goals_against), 0) AS goals_against,
    COALESCE(SUM(mr.goals_for - mr.goals_against), 0) AS goal_difference,
    COALESCE(
        SUM(CASE 
            WHEN mr.result = 'W' THEN (c.policy->'scoring'->>'win')::int
            WHEN mr.result = 'D' THEN (c.policy->'scoring'->>'draw')::int
            ELSE (c.policy->'scoring'->>'loss')::int
        END),
        0
    ) AS points
FROM teams t
JOIN competitions c ON t.competition_id = c.id
LEFT JOIN match_results mr ON mr.team_id = t.id AND mr.competition_id = c.id
GROUP BY t.id, t.name, t.competition_id, c.policy;

-- ============================================
-- TRIGGERS & FUNCTIONS
-- ============================================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_competitions_updated_at BEFORE UPDATE ON competitions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_teams_updated_at BEFORE UPDATE ON teams FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_players_updated_at BEFORE UPDATE ON players FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_matches_updated_at BEFORE UPDATE ON matches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto-update team roster_size when players added/removed/moved
CREATE OR REPLACE FUNCTION update_team_roster_size()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE teams SET roster_size = GREATEST(roster_size + 1, 0) WHERE id = NEW.team_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE teams SET roster_size = GREATEST(roster_size - 1, 0) WHERE id = OLD.team_id;
    ELSIF TG_OP = 'UPDATE' AND NEW.team_id <> OLD.team_id THEN
        UPDATE teams SET roster_size = GREATEST(roster_size - 1, 0) WHERE id = OLD.team_id;
        UPDATE teams SET roster_size = GREATEST(roster_size + 1, 0) WHERE id = NEW.team_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_roster_size 
AFTER INSERT OR DELETE OR UPDATE OF team_id ON players 
FOR EACH ROW EXECUTE FUNCTION update_team_roster_size();

-- ============================================
-- NOTES
-- ============================================

-- This schema supports the 8-week MVP (updated timeline):
-- Week 1-3: Users, competitions, venues
-- Week 4-5: Teams, payments, subscriptions
-- Week 5.5: Owner dashboard (teams view) - uses existing schema
--   - Query teams by competition_id for owner dashboard
--   - Uses roster_size, subscription_status, registered_at fields
--   - Joins with users table for coach info
-- Week 6: Players with photos
-- Week 7: Matches, results, standings
-- Week 8: Production readiness & polish

-- Deferred to V2__enhancements.sql (post-launch):
-- - Audit tables (payments_audit, roster_audit, results_audit, domain_events)
-- - CSV import tables (team_imports, team_invite_tokens)
-- - Email notifications queue (email_notifications)
-- - Refund requests table (refund_requests)
-- - Materialized standings view (if performance needed)

-- Manual handling until volume justifies automation:
-- - Refunds via Stripe dashboard
-- - Password resets via manual email
-- - Failed payment monitoring via Stripe
-- - Financials via Stripe Connect/admin dashboards