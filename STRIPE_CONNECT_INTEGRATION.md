# Stripe Connect Integration Documentation

## Overview

This application uses Stripe Connect to enable competition owners to receive payments directly. The integration supports both webhook-based and polling-based status updates to ensure reliable account verification.

## Architecture

### Flow Diagram

```
User → Create Competition → Stripe Connect Onboarding → Return to App → Verify Status → Publish Competition
```

### Components

1. **Backend (`StripeController` & `StripeService`)**
   - Creates Stripe Connect accounts
   - Generates onboarding links
   - Handles webhooks
   - Refreshes account status from Stripe API

2. **Frontend (`StripeReturn` & `StripeRefresh` pages)**
   - Handles redirect from Stripe
   - Polls for account status updates
   - Displays success/error messages

3. **Webhooks**
   - Listens for `account.updated` events
   - Updates user payout status in real-time

## How It Works

### 1. Creating a Stripe Connect Account

When a competition owner tries to publish a competition:

**Backend: `StripeService.createConnectOnboardingLink()`**
```java
- Checks if user is COMPETITION_OWNER
- Creates or retrieves Stripe Connect account
- Sets account capabilities (card_payments, transfers)
- Generates account onboarding link
- Returns URL for user to complete onboarding
```

**Key endpoints:**
- `POST /api/stripe/connect-onboarding-link` - Creates onboarding link

### 2. Onboarding Flow

1. User clicks "Connect with Stripe" or "Publish Competition"
2. Backend creates/retrieves Stripe account and returns onboarding URL
3. User is redirected to Stripe to complete onboarding
4. After completion, Stripe redirects back to:
   - **Success**: `http://localhost:5173/dashboard/stripe/return`
   - **Incomplete**: `http://localhost:5173/dashboard/stripe/refresh`

### 3. Status Verification (Dual Approach)

After returning from Stripe, the application uses **both** methods to ensure status is updated:

#### Method 1: Webhooks (Primary - Real-time)

**Webhook: `account.updated`**
```java
StripeWebhookController receives event
→ Verifies signature
→ Extracts account ID
→ StripeService.handleAccountUpdated()
  → Retrieves account from Stripe
  → Checks chargesEnabled, payoutsEnabled, detailsSubmitted
  → Updates user.payoutStatus
    - ENABLED: Both charges and payouts enabled
    - PENDING: Details submitted but not fully enabled
    - NONE: Details not submitted
  → Saves to database
```

**Key endpoint:**
- `POST /api/stripe/webhooks` - Receives Stripe webhook events

#### Method 2: Manual Refresh (Fallback - Polling)

**Frontend: `StripeReturn.tsx`**
```typescript
On page load:
1. Poll up to 10 times (20 seconds total)
2. Every attempt (or every 3rd attempt):
   - Call /api/stripe/refresh-account-status
   - Fetch latest user data
   - Check if payoutStatus === 'ENABLED'
3. Show success when enabled
4. Show error if timeout
```

**Backend: `StripeService.refreshAccountStatus()`**
```java
- Retrieves user's Stripe Connect account ID
- Fetches account from Stripe API
- Updates payoutStatus based on current state
- Saves to database
```

**Key endpoint:**
- `POST /api/stripe/refresh-account-status` - Manually refreshes status from Stripe

### Why Both Methods?

- **Webhooks**: Fast, real-time updates (production)
- **Polling**: Fallback for delayed/missed webhooks, works in test mode
- **Together**: Ensures reliable status updates in all environments

## Database Schema

### User Table Fields

```sql
stripe_connect_account_id VARCHAR(255)      -- Stripe account ID (e.g., acct_xxx)
stripe_connect_status VARCHAR(50)           -- NOT_STARTED | INCOMPLETE | VERIFIED | BLOCKED
payout_status VARCHAR(50)                   -- NONE | PENDING | ENABLED | BLOCKED
stripe_connect_onboarded_at TIMESTAMP       -- When account was verified
```

### Status Meanings

**stripe_connect_status:**
- `NOT_STARTED`: No Stripe account created
- `INCOMPLETE`: Account created but onboarding not complete
- `VERIFIED`: Account fully verified and operational
- `BLOCKED`: Account blocked by Stripe

**payout_status:**
- `NONE`: Cannot receive payouts
- `PENDING`: Details submitted, awaiting verification
- `ENABLED`: Can receive payouts ✅
- `BLOCKED`: Payouts blocked

## Configuration

### Environment Variables

**Backend (`application.yml`):**
```yaml
stripe:
  api-key: ${STRIPE_SECRET_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

### Production Setup

1. **Get Stripe API Keys:**
   - Go to https://dashboard.stripe.com/apikeys
   - Copy Secret key → Set as `STRIPE_SECRET_KEY`

2. **Setup Webhook Endpoint:**
   - Go to https://dashboard.stripe.com/webhooks
   - Add endpoint: `https://yourdomain.com/api/stripe/webhooks`
   - Select events: `account.updated`
   - Copy webhook signing secret → Set as `STRIPE_WEBHOOK_SECRET`

3. **Set CORS:**
   - Set `CORS_ALLOWED_ORIGINS` to your frontend URL (e.g., `https://yourdomain.com`)

### Development Setup

1. **Install Stripe CLI:**
   ```bash
   # Download from https://stripe.com/docs/stripe-cli
   stripe login
   ```

2. **Forward webhooks:**
   ```bash
   stripe listen --forward-to localhost:8080/api/stripe/webhooks
   ```

3. **Copy webhook secret:**
   - From CLI output, copy `whsec_xxx` → Set as `STRIPE_WEBHOOK_SECRET`

## Testing

### Test Mode

Stripe provides test mode for development:

1. Use test API keys (starts with `sk_test_`)
2. Test onboarding uses fake data
3. Webhooks work via Stripe CLI

### Test the Flow

1. Login as competition owner
2. Create a competition
3. Click "Publish" or "Connect with Stripe"
4. Complete Stripe onboarding (use test data)
5. After redirect, should see "Processing..." then "Success!"
6. User should have `payoutStatus: ENABLED`
7. Competition can now be published

## Troubleshooting

### Issue: "Stripe connection taking longer than expected"

**Causes:**
1. Webhook not received (check Stripe CLI is running)
2. Account not fully verified by Stripe
3. Network issues

**Solutions:**
1. Check webhook logs in Stripe Dashboard
2. Check backend logs for webhook processing
3. Manually refresh the page (triggers new API call)
4. Verify account in Stripe Dashboard

### Issue: Webhook signature verification failed

**Causes:**
1. Wrong webhook secret
2. Webhook sent to wrong endpoint

**Solutions:**
1. Verify `STRIPE_WEBHOOK_SECRET` matches Stripe Dashboard
2. Ensure webhook endpoint URL is correct
3. Check for proxy/load balancer modifying request

### Issue: User stuck in PENDING status

**Causes:**
1. Stripe requires additional verification
2. Account has requirements not met

**Solutions:**
1. Check Stripe Dashboard → Connect → Accounts
2. Look for "Requirements" section
3. User may need to provide additional documents

## Security Considerations

1. **Webhook Signature Verification**: Always verify webhook signatures
2. **API Key Security**: Never commit API keys to git
3. **CORS Configuration**: Restrict to your domains only
4. **User Authorization**: Only COMPETITION_OWNER can connect accounts
5. **Account Ownership**: Verify user owns the Stripe account

## Monitoring

### Key Metrics to Monitor

1. **Onboarding Success Rate**: % of users who complete onboarding
2. **Webhook Delivery**: Monitor webhook failures
3. **Account Status Distribution**: Track PENDING vs ENABLED accounts
4. **API Call Volume**: Monitor /refresh-account-status calls

### Logs to Watch

**Backend logs:**
```
"Creating Stripe Connect onboarding link for user: {userId}"
"WEBHOOK: Processing account.updated webhook: accountId={}"
"Manually refreshing account status for user: {userId}"
"Setting user account to VERIFIED/ENABLED"
```

**Frontend logs:**
```javascript
console.error('Failed to refresh user data:', error)
```

## API Reference

### POST /api/stripe/connect-onboarding-link
Creates or retrieves Stripe Connect onboarding link.

**Authentication**: Required (JWT)
**Role**: COMPETITION_OWNER

**Response:**
```json
{
  "url": "https://connect.stripe.com/setup/...",
  "expiresAt": 1234567890
}
```

### POST /api/stripe/refresh-account-status
Manually refreshes user's Stripe account status from Stripe API.

**Authentication**: Required (JWT)
**Role**: COMPETITION_OWNER

**Response:** 200 OK (no body)

### POST /api/stripe/webhooks
Receives Stripe webhook events.

**Authentication**: Webhook signature verification
**Headers**: `Stripe-Signature`

**Events Handled:**
- `account.updated`: Updates user payout status
- `account.application.authorized`: Logged (not implemented)

## Future Enhancements

1. **Dashboard Integration**: Show Stripe account status in user dashboard
2. **Retry Logic**: Automatic retry for failed webhook processing
3. **Email Notifications**: Notify users when account is verified
4. **Account Requirements**: Display missing requirements to users
5. **Payout History**: Show payout history from Stripe
6. **Tax Forms**: Handle tax form collection for users
