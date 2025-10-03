# Week 3: Stripe Connect Onboarding - Progress Report

## Implementation Date
October 2, 2025

## Success Criteria
**Goal:** Competition owners can connect Stripe, webhook updates status

## What We Completed

### Backend Implementation ✅

#### 1. Stripe Service (Already Existed)
**File:** `backend/src/main/java/com/leaguehq/service/StripeService.java`

- **createConnectOnboardingLink()** (lines 47-116)
  - Creates Stripe Connect STANDARD account
  - Requests `card_payments` and `transfers` capabilities
  - Generates onboarding link with return/refresh URLs
  - Saves `stripeConnectAccountId` to user
  - Sets initial status to `INCOMPLETE`

- **handleAccountUpdated()** (lines 118-157)
  - Processes `account.updated` webhook
  - Updates user status based on Stripe account state:
    - `VERIFIED` + `ENABLED` when charges and payouts enabled
    - `INCOMPLETE` + `PENDING` when details submitted
    - `INCOMPLETE` + `NONE` when not started

- **Configuration:**
  - Stripe API key configured in `application.yml:40`
  - Webhook secret configured in `application.yml:41`
  - Frontend URL from `application.yml:37`

#### 2. Stripe Controller (Already Existed)
**File:** `backend/src/main/java/com/leaguehq/controller/StripeController.java`

- **POST /api/stripe/connect-onboarding-link**
  - Requires authentication
  - Returns onboarding URL and expiration timestamp
  - Only accessible to COMPETITION_OWNER role

#### 3. Webhook Controller (Already Existed)
**File:** `backend/src/main/java/com/leaguehq/controller/StripeWebhookController.java`

- **POST /api/stripe/webhooks**
  - Verifies Stripe webhook signature
  - Handles `account.updated` events
  - Returns 200 OK to acknowledge receipt

#### 4. Competition Service Blocking (Already Existed)
**File:** `backend/src/main/java/com/leaguehq/service/CompetitionService.java:151-155`

- Blocks publishing competitions until `payoutStatus === ENABLED`
- Error message: "Cannot publish competition. You must complete Stripe Connect onboarding first."

### Frontend Implementation ✅

#### 1. Type Definitions
**File:** `frontend/src/types/index.ts`

Updated enums to match backend exactly:
```typescript
export type StripeConnectStatus = 'NOT_STARTED' | 'INCOMPLETE' | 'VERIFIED' | 'BLOCKED';
export type PayoutStatus = 'NONE' | 'PENDING' | 'ENABLED' | 'BLOCKED';
```

#### 2. API Service
**File:** `frontend/src/services/api.ts:81-87`

Added Stripe API endpoint:
```typescript
export const stripeApi = {
  createConnectOnboardingLink: async (): Promise<{ url: string; expiresAt: number }> => {
    const response = await api.post('/stripe/connect-onboarding-link');
    return response.data;
  },
};
```

#### 3. Competition Detail Page
**File:** `frontend/src/pages/CompetitionDetail.tsx`

Complete rewrite with:
- Yellow warning alert when Stripe Connect needed (lines 119-144)
- "Connect Stripe Account" button (redirects to Stripe)
- "Publish Competition" button (only shows when `payoutStatus === 'ENABLED'`)
- Displays current payout status
- Share link with copy functionality
- Proper owner/public view separation

**Logic:**
```typescript
const isOwner = user && competition && user.id === competition.ownerId;
const canPublish = isOwner && competition?.status === 'DRAFT' && user.payoutStatus === 'ENABLED';
const needsStripeConnect = isOwner && competition?.status === 'DRAFT' && user.payoutStatus !== 'ENABLED';
```

## Issues Resolved During Implementation

### Issue 1: Invalid Stripe API Key
**Problem:** Spring Boot doesn't natively read `.env` files
**Solution:** Updated `application.yml:40` to include actual test key as default value

### Issue 2: Stripe Connect Not Enabled
**Problem:** Stripe account wasn't enabled for Connect
**Solution:** Enabled at https://dashboard.stripe.com/settings/connect

### Issue 3: Missing card_payments Capability
**Problem:** STANDARD accounts require both `card_payments` and `transfers` capabilities
**Solution:** Updated `StripeService.java:68-70` to request both capabilities

### Issue 4: Port 8080 Already in Use
**Problem:** Multiple backend processes accumulated
**Solution:** Used `taskkill //PID <PID> //F` before each restart

## What's Remaining for Week 3

### 1. Stripe Return/Refresh Pages ✅ COMPLETE
Created two frontend pages:

**Page 1: `/dashboard/stripe/return`** ✅
- Where Stripe redirects after successful onboarding
- Shows success message with CheckCircleIcon
- Refreshes user data to get updated payout status via `authApi.getCurrentUser()`
- Redirects back to competitions page after 2 seconds
- Shows loading spinner while processing
- Error handling with fallback UI

**Page 2: `/dashboard/stripe/refresh`** ✅
- Where Stripe redirects if user needs to re-authenticate
- Explains that onboarding was incomplete
- Shows "Try Again" button to restart onboarding flow via `stripeApi.createConnectOnboardingLink()`
- "Return to Competitions" button for users who want to skip
- Error handling for failed retry attempts
- Support contact information displayed

### 2. Webhook Testing (Recommended)
Test that `account.updated` webhook properly updates user status:

**Option A: Complete Real Onboarding**
- Go through actual Stripe Connect onboarding
- Verify webhook updates `stripeConnectStatus` and `payoutStatus`
- Check that "Publish Competition" button appears

**Option B: Use Stripe CLI**
```bash
stripe listen --forward-to localhost:8080/api/stripe/webhooks
stripe trigger account.updated
```

## Production Changes Needed

### 1. Environment Variables
**Current (Development):**
```yaml
# application.yml
stripe:
  api-key: ${STRIPE_SECRET_KEY:sk_test_51SDAiQE0uPsgn3jq...}  # Test key in default
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_placeholder}
```

**Production Changes Required:**
```yaml
# application.yml - Remove default values
stripe:
  api-key: ${STRIPE_SECRET_KEY}  # No default - must be set
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}  # No default - must be set
```

**Environment Setup:**
- Set `STRIPE_SECRET_KEY` to live key (starts with `sk_live_`)
- Set `STRIPE_WEBHOOK_SECRET` from production webhook endpoint
- Configure webhook at https://dashboard.stripe.com/webhooks
  - URL: `https://yourdomain.com/api/stripe/webhooks`
  - Event: `account.updated`
  - Copy webhook secret to environment variable

### 2. Frontend URLs
**Current (Development):**
```java
// StripeService.java:93-94
String returnUrl = frontendUrl + "/dashboard/stripe/return";
String refreshUrl = frontendUrl + "/dashboard/stripe/refresh";
```

**Production Changes Required:**
- Update `CORS_ALLOWED_ORIGINS` environment variable to production domain
- Verify `frontendUrl` resolves to production domain (e.g., `https://leaguehq.com`)

### 3. Database
**Current:** User table already has all required fields from migration V2
- `stripe_connect_account_id`
- `stripe_connect_status` (enum)
- `payout_status` (enum)
- `stripe_connect_onboarded_at`

**Production:** No changes needed - schema is ready

### 4. Security Considerations

**Webhook Signature Verification:**
- Already implemented in `StripeWebhookController`
- Uses `webhookSecret` to verify requests are from Stripe
- Do NOT disable in production

**API Key Security:**
- Never commit live keys to git
- Use environment variables only
- Rotate keys if exposed
- Monitor Stripe dashboard for suspicious activity

**HTTPS Required:**
- Stripe webhooks require HTTPS in production
- Set up SSL certificate for your domain
- Configure webhook URL with `https://`

### 5. Monitoring & Logging

**Current Logging:**
```java
log.info("Created Stripe Connect account: userId={}, accountId={}", userId, accountId);
log.error("Stripe error creating Connect account: userId={}, error={}", userId, e.getMessage(), e);
```

**Production Recommendations:**
- Keep all existing log statements
- Monitor webhook failures in application logs
- Set up alerts for:
  - Failed webhook deliveries (check Stripe dashboard)
  - Repeated onboarding failures
  - Accounts stuck in INCOMPLETE status
- Consider adding metrics:
  - Number of successful onboardings
  - Time from account creation to VERIFIED status
  - Webhook processing latency

### 6. Error Handling

**Current:** Basic error messages to frontend

**Production Enhancements:**
- Add retry logic for transient Stripe API errors
- Handle edge cases:
  - Account deleted in Stripe but still in DB
  - User tries to connect multiple accounts
  - Stripe account restricted/disabled
- Provide clearer error messages for common failures

## Testing Checklist Before Production

- [ ] Complete actual Stripe Connect onboarding in test mode
- [ ] Verify webhook updates user status correctly
- [ ] Test with live Stripe keys in staging environment
- [ ] Verify publish blocking works correctly
- [ ] Test return and refresh URL flows
- [ ] Verify webhook signature validation
- [ ] Test error scenarios (network failures, invalid data)
- [ ] Load test webhook endpoint
- [ ] Verify HTTPS certificate for webhook URL
- [ ] Test with multiple competition owners
- [ ] Verify no test keys in production code

## Files Modified

### Backend
- `backend/src/main/resources/application.yml` (line 40: added Stripe test key)
- `backend/src/main/java/com/leaguehq/service/StripeService.java` (lines 68-70: added card_payments capability)

### Frontend
- `frontend/src/types/index.ts` (updated enums)
- `frontend/src/services/api.ts` (added stripeApi)
- `frontend/src/pages/CompetitionDetail.tsx` (complete rewrite)
- `frontend/src/pages/StripeReturn.tsx` (created - handles successful Stripe onboarding)
- `frontend/src/pages/StripeRefresh.tsx` (created - handles incomplete Stripe onboarding)
- `frontend/src/App.tsx` (added routes for /dashboard/stripe/return and /dashboard/stripe/refresh)

### Configuration
- `backend/.env` (created from .env.example - contains test keys)

## Next Session TODO

1. ✅ Create `StripeReturn.tsx` page at `/dashboard/stripe/return`
2. ✅ Create `StripeRefresh.tsx` page at `/dashboard/stripe/refresh`
3. ✅ Add routes to React Router configuration
4. Test complete onboarding flow (end-to-end testing)
5. Verify webhook updates user status (end-to-end testing)
6. Test publish button appears after successful onboarding (end-to-end testing)

## Success Metrics Achieved

✅ Competition owner can click "Connect Stripe Account"
✅ Redirects to Stripe onboarding flow successfully
✅ Backend creates Connect account with correct capabilities
✅ Publish button only appears when eligible
✅ Warning alert shows when Stripe Connect needed
✅ Return/refresh pages complete
⏳ Webhook flow tested end-to-end (pending - requires actual Stripe onboarding)

## Week 3 Status: 95% Complete

**All development work complete - ready for end-to-end testing with Stripe**
