-- V2: Update competitions schema to match build plan requirements
-- Add format and team_size fields, update entry_fee to decimal, fix status enum

-- Add new columns for format and team size
ALTER TABLE competitions
    ADD COLUMN format VARCHAR(50) CHECK (format IN ('LEAGUE', 'KNOCKOUT', 'ROUND_ROBIN')),
    ADD COLUMN team_size VARCHAR(50) CHECK (team_size IN ('FIVE_V_FIVE', 'SIX_V_SIX', 'SEVEN_V_SEVEN', 'EIGHT_V_EIGHT', 'NINE_V_NINE', 'ELEVEN_V_ELEVEN'));

-- Set default values for existing rows (if any)
UPDATE competitions
SET format = 'LEAGUE',
    team_size = 'ELEVEN_V_ELEVEN'
WHERE format IS NULL OR team_size IS NULL;

-- Make columns NOT NULL after setting defaults
ALTER TABLE competitions
    ALTER COLUMN format SET NOT NULL,
    ALTER COLUMN team_size SET NOT NULL;

-- Change entry_fee from cents to decimal
ALTER TABLE competitions
    ADD COLUMN entry_fee DECIMAL(10,2);

-- Migrate existing data (cents to decimal)
UPDATE competitions
SET entry_fee = entry_fee_cents / 100.0;

-- Make new column NOT NULL and add constraint
ALTER TABLE competitions
    ALTER COLUMN entry_fee SET NOT NULL,
    ADD CONSTRAINT check_entry_fee_positive CHECK (entry_fee >= 0);

-- Drop old column
ALTER TABLE competitions
    DROP COLUMN entry_fee_cents;

-- Update status enum to match build plan
ALTER TABLE competitions
    DROP CONSTRAINT IF EXISTS competitions_status_check;

ALTER TABLE competitions
    ADD CONSTRAINT competitions_status_check
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ACTIVE', 'COMPLETED', 'CANCELLED'));

-- Update existing statuses
UPDATE competitions
SET status = 'PUBLISHED'
WHERE status = 'REGISTRATION_OPEN';

UPDATE competitions
SET status = 'ACTIVE'
WHERE status IN ('REGISTRATION_CLOSED', 'IN_PROGRESS');

-- Drop sport column (replaced by format + team_size)
ALTER TABLE competitions
    DROP COLUMN sport;

-- Update registration_deadline to DATE instead of TIMESTAMPTZ
ALTER TABLE competitions
    ADD COLUMN registration_deadline_temp DATE;

UPDATE competitions
SET registration_deadline_temp = registration_deadline::DATE
WHERE registration_deadline IS NOT NULL;

ALTER TABLE competitions
    DROP COLUMN registration_deadline;

ALTER TABLE competitions
    RENAME COLUMN registration_deadline_temp TO registration_deadline;
