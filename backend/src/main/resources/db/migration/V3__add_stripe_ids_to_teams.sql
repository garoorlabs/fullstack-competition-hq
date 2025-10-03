-- Add Stripe IDs to teams table for reconciliation
ALTER TABLE teams
ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS stripe_latest_invoice_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS stripe_latest_payment_intent_id VARCHAR(255);

-- Add index for customer_id lookups
CREATE INDEX IF NOT EXISTS idx_teams_stripe_customer ON teams(stripe_customer_id);

-- Add comment explaining these fields
COMMENT ON COLUMN teams.stripe_customer_id IS 'Stripe Customer ID for this team - used for billing';
COMMENT ON COLUMN teams.stripe_latest_invoice_id IS 'Latest Stripe Invoice ID - updated on each billing cycle';
COMMENT ON COLUMN teams.stripe_latest_payment_intent_id IS 'Latest Stripe Payment Intent ID - tracks payment status';
