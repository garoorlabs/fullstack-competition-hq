# LeagueHQ Backend

Spring Boot 3.2 + PostgreSQL backend for LeagueHQ.

## Setup
See main [README.md](../README.md) for full setup instructions.

## Documentation
- [Build Plan](../docs/build_plan.md)
- [Database Schema](../docs/db_schema.md)
- [Coding Guidelines](../docs/CODING_GUIDELINES_BACKEND.md)

## Running Locally
```bash
# From backend/ directory
mvn spring-boot:run
```

## Testing
```bash
# Run tests
mvn test

# Run with PostgreSQL via Docker
cd .. && docker-compose up -d
mvn spring-boot:run
```

## API Endpoints
Base URL: `http://localhost:8080/api`

### Authentication
- `POST /auth/signup` - Register new user
- `POST /auth/login` - Login and get JWT
- `GET /auth/me` - Get current user (requires JWT)

### Competitions
- `POST /competitions` - Create competition (Owner only)
- `GET /competitions/my` - List my competitions (Owner only)
- `GET /competitions/:id` - Get competition details (Public)

### Stripe
- `POST /stripe/connect-onboarding-link` - Get Stripe Connect link (Owner only)
- `POST /stripe/webhooks` - Stripe webhook handler (Public)

## Environment Variables
Required for local development (create `.env` file):
```
DATABASE_URL=jdbc:postgresql://localhost:5432/leaguehq
JWT_SECRET=your-secret-key-min-32-chars
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

## Stripe CLI Setup for Local Development

To test Stripe webhooks locally, you need to forward webhook events from Stripe to your localhost:

### 1. Install Stripe CLI
```bash
# Windows (using winget)
winget install --id Stripe.StripeCLI

# macOS (using Homebrew)
brew install stripe/stripe-cli/stripe

# Linux
# See: https://docs.stripe.com/stripe-cli#install
```

### 2. Login to Stripe CLI
```bash
stripe login
```
This will open a browser to authenticate with your Stripe account.

### 3. Start Webhook Forwarding
```bash
stripe listen --forward-to localhost:8080/api/stripe/webhooks
```

The CLI will output a webhook signing secret like:
```
> Ready! Your webhook signing secret is whsec_1234567890abcdef...
```

### 4. Update Backend with CLI Webhook Secret
Copy the webhook signing secret from step 3 and either:

**Option A: Set as environment variable**
```bash
export STRIPE_WEBHOOK_SECRET=whsec_1234567890abcdef...
./mvnw spring-boot:run
```

**Option B: Pass directly to Maven**
```bash
STRIPE_WEBHOOK_SECRET=whsec_1234567890abcdef... ./mvnw spring-boot:run
```

### 5. Test the Flow
1. Navigate to a competition detail page
2. Click "Connect Stripe Account"
3. Complete the Stripe onboarding flow
4. After redirect, watch the Stripe CLI terminal - you should see the `account.updated` webhook
5. Your backend will update the user's `payout_status` to `ENABLED`
6. The competition page will now show "Publish Competition" instead of "Connect Stripe"

**Note:** Keep the `stripe listen` command running in a separate terminal while developing. It must be active to receive webhooks.
