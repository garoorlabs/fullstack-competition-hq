# Week 1-5 Gap Analysis & Documentation Updates

**Date:** Current Build Status Review
**Status:** COMPLETE - All gaps identified and documented

---

## Summary

After completing Weeks 1-5 of implementation, we conducted a comprehensive ultrabrainstorm analysis covering **both Competition Owner and Coach user flows**. We identified critical missing features that would break the user experience if not addressed before moving to Week 6.

---

## Critical Gaps Found

### 1. Competition Owner - No Team Visibility ❌
**Problem:**
- Owners could create competitions but couldn't see who registered
- Team count was hardcoded to `0`
- No Teams tab existed (despite being in wireframes)
- Couldn't view team rosters or verify eligibility

**Impact:** Owners have ZERO visibility into their competition registrations!

---

### 2. Coach - Broken "Manage Roster" Flow ❌
**Problem:**
- Coaches see "Manage Roster" button in My Teams
- Button links to `/teams/:id/roster` which doesn't exist
- Week 6 (roster management) hasn't been built yet
- Creates broken user experience immediately after payment

**Impact:** Coaches pay for team, then hit 404 error when trying to add players!

---

### 3. Coach - No Payment Failure Recovery ❌
**Problem:**
- "Update Payment" button exists for PAST_DUE subscriptions
- Links to `/teams/:id/payment` which doesn't exist
- No self-service way to fix failed payments
- Webhooks set status but coaches can't act on it

**Impact:** Lost revenue from failed subscriptions with no recovery path!

---

### 4. Webhook Payment Processing - Unverified ⚠️
**Problem:**
- Webhook handlers exist but implementation not fully verified
- Need to confirm team records update correctly
- Need to confirm payment_transactions are created
- Need to confirm subscription records are stored

**Impact:** Payments might not be processing correctly in production!

---

## Solution: Modified Build Plan

### Original Plan (6-7 Weeks):
```
Week 1-3: Foundation
Week 4-5: Team Registration & Payments
Week 6: Roster Management
Week 7: Matches & Standings
```

### Updated Plan (8 Weeks):
```
Week 1-3: Foundation ✅
Week 4-5: Team Registration & Payments ✅
Week 5.5: Owner Dashboard & Payment Recovery ⚡ NEW
Week 6: Roster Management
Week 7: Matches & Standings
Week 8: Production Readiness
```

**Week 5.5 is CRITICAL** - it prevents broken user flows for both owners and coaches.

---

## Documentation Updates

### 1. build_plan.md - MAJOR UPDATE ✅
**Changes:**
- Extended timeline from 6-7 weeks to 8 weeks
- Added detailed **Week 5.5: Owner Dashboard & Payment Recovery**
- Broke down every week into Backend + Frontend sections
- Added comprehensive success criteria for each week
- Added "Complete API Endpoints Reference" section
- Added "Launch Readiness Checklist" with owner & coach features
- Added technical notes (Stripe, image processing, performance, security)
- Added timeline table showing Week 5.5 as "Critical Gaps"

**New Week 5.5 Includes:**
- Backend:
  - `GET /api/competitions/{id}/teams` endpoint
  - Fix `CompetitionResponse.currentTeamCount`
  - `POST /api/teams/{id}/update-payment` endpoint
- Frontend:
  - Tabs component on Competition Detail
  - Teams tab with team table
  - Update Payment page
  - Public "Registered Teams" section

---

### 2. WIREFRAMES.md - ADDED MISSING WIREFRAMES ✅
**Changes:**
- Added **5b. Competition Detail - Teams Tab (Owner View)**
  - ASCII wireframe showing team table
  - Columns: Team Name, Coach, Sub Status, Size
  - View Roster button per team
- Added **5c. Update Payment Page (Coach View)**
  - ASCII wireframe for payment recovery
  - Warning alert for failed payment
  - Redirect to Stripe Customer Portal

**Why:** Wireframes mentioned tabs but didn't show Teams tab content or payment recovery page.

---

### 3. db_schema.md - MINOR UPDATE ✅
**Changes:**
- Updated timeline comment from "6-7 week MVP" to "8-week MVP"
- Added Week 5.5 note:
  - Query teams by competition_id
  - Uses roster_size, subscription_status, registered_at
  - Joins with users table for coach info
- Added Week 8: Production readiness

**Why:** Schema was already complete, just needed timeline update.

---

## What Week 5.5 Fixes

### For Competition Owners:
✅ Can see all teams registered for their competition
✅ Can see coach names and contact info
✅ Can see subscription status (ACTIVE, PAST_DUE, CANCELLED)
✅ Can see roster size per team
✅ Can click to view team rosters
✅ Team count displays correctly (not hardcoded to 0)
✅ Tabs work correctly (Overview | Teams | Matches | Standings)

### For Coaches:
✅ Can fix failed payments via Customer Portal
✅ "Update Payment" button actually works
✅ Self-service payment recovery (no manual intervention)
✅ Clear guidance when subscription is PAST_DUE

### For Public/Other Coaches:
✅ Can see list of registered teams on competition page
✅ Social proof for competition popularity

---

## Implementation Priority

### Phase 1 (DO FIRST - Week 5.5):
1. **Owner Teams View**
   - `GET /api/competitions/{id}/teams` endpoint
   - Tabs component + Teams tab UI
   - Fix currentTeamCount calculation

2. **Payment Recovery**
   - `POST /api/teams/{id}/update-payment` endpoint
   - Update Payment page with Stripe portal redirect

3. **Webhook Verification**
   - Test checkout.session.completed
   - Test invoice.payment_succeeded
   - Test invoice.payment_failed
   - Verify team records update
   - Verify transactions are created

### Phase 2 (THEN - Week 6):
4. **Roster Management**
   - Player CRUD endpoints
   - Photo upload service
   - Team Roster page
   - Add/Edit Player modal

---

## Testing Checklist Before Week 6

**Week 5.5 Must Work:**
- [ ] Owner can view teams for their competition
- [ ] Team count shows correct number (not 0)
- [ ] Tabs switch between Overview/Teams/Matches/Standings
- [ ] Teams table shows all required data
- [ ] Coach with PAST_DUE can click "Update Payment"
- [ ] Update Payment redirects to Stripe portal
- [ ] Coach can update card and fix subscription
- [ ] Public view shows registered team names

**Payment Processing Must Work:**
- [ ] Checkout completes successfully
- [ ] Entry fee splits correctly (8% platform, 92% owner)
- [ ] Subscription created with $20/month
- [ ] Team.entryFeePaid set to true
- [ ] Team.subscriptionId stored
- [ ] payment_transactions record created
- [ ] subscription_events record created
- [ ] Failed payment sets status to PAST_DUE

---

## Files Updated

1. ✅ `docs/build_plan.md` - Complete rewrite with 8-week plan
2. ✅ `docs/WIREFRAMES.md` - Added Teams tab and Update Payment wireframes
3. ✅ `docs/db_schema.md` - Updated timeline notes
4. ✅ `docs/WEEK3_GAP_ANALYSIS.md` - This file (summary of changes)

---

## Next Steps

1. **Implement Week 5.5 features** (estimated 2-3 days)
   - Owner teams endpoint + UI
   - Payment recovery page
   - Verify webhook processing

2. **Test end-to-end flows**
   - Owner creates competition → publishes → views teams
   - Coach registers → pays → (will need roster in Week 6)
   - Coach with failed payment → updates card → subscription resumes

3. **Move to Week 6** (roster management)
   - Only proceed after Week 5.5 is complete and tested
   - This ensures "Manage Roster" button will work

---

## Key Learnings

1. **User flow mapping is critical** - We found gaps by walking through actual user journeys
2. **Both personas matter** - Can't just focus on one user type (owner OR coach)
3. **Wireframes showed the vision** - Tabs were planned but not implemented
4. **Test early** - Would have found these gaps earlier with manual testing
5. **Features are interconnected** - Can't skip Week 5.5 and jump to Week 6

---

**Status:** Documentation complete. Ready to implement Week 5.5 features.
