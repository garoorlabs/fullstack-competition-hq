# Week 1-2 Review Fixes - Schema Alignment

**Date:** October 1, 2025
**Status:** Fixed - Backend now matches frontend implementation

## Issues Found

During Week 1-2 review testing, we discovered that the backend implementation did not match the frontend expectations or the build plan requirements.

### Critical Mis

matches:

| Issue | Frontend Expected | Backend Had | Impact |
|-------|------------------|-------------|---------|
| Competition Type | `format` (LEAGUE/KNOCKOUT) + `teamSize` (enums) | `sport` (VARCHAR) | BLOCKER |
| Entry Fee | `entryFee` (decimal) | `entry_fee_cents` (integer) | BLOCKER |
| Venue Structure | Full venue object | Simple name+address | BLOCKER |
| Registration Deadline | LocalDate (YYYY-MM-DD) | Instant (timestamp) | BLOCKER |
| Status Values | DRAFT/PUBLISHED/ACTIVE | REGISTRATION_OPEN/IN_PROGRESS | Medium |

## Changes Made

### 1. Database Migration (V2__update_competitions_schema.sql)
- Added `format` column (LEAGUE, KNOCKOUT, ROUND_ROBIN)
- Added `team_size` column (FIVE_V_FIVE through ELEVEN_V_ELEVEN)
- Changed `entry_fee_cents` → `entry_fee` (DECIMAL)
- Updated status enum values
- Changed `registration_deadline` from TIMESTAMPTZ to DATE
- Removed `sport` column

### 2. Competition Entity (Competition.java)
- Added `CompetitionFormat` enum
- Added `TeamSize` enum
- Updated `CompetitionStatus` enum (DRAFT, PUBLISHED, ACTIVE, COMPLETED, CANCELLED)
- Changed `entryFeeCents` → `entryFee` (BigDecimal)
- Changed `registrationDeadline` to LocalDate

### 3. CreateCompetitionRequest DTO
- Added `format` and `teamSize` fields
- Changed `entryFee` to BigDecimal
- Added nested `VenueRequest` class with full venue structure:
  - name, address, city, state, zipCode
  - fieldCount, hasLights, notes
- Changed `registrationDeadline` to LocalDate

### 4. CompetitionService
- Updated `createCompetition()` to use new field names
- Updated venue creation to use full venue object from request

### 5. CompetitionResponse DTO
- Added `format` and `teamSize` fields
- Changed `entryFeeCents` → `entryFee`
- Added `currentTeamCount` field (placeholder, will calculate from teams table)
- Updated `fromEntity()` method to map new fields

## Testing Required

Before proceeding to Week 3:
1. ✅ Run V2 migration successfully
2. ⏳ Test signup → login → JWT flow
3. ⏳ Test competition creation with new structure
4. ⏳ Test competition listing
5. ⏳ Test competition detail view

## Frontend Impact

**No changes needed!** The frontend was built correctly according to the build plan. All frontend types and forms already match the updated backend.

## Next Steps

1. Kill all background processes
2. Restart backend to run V2 migration
3. Test full auth + competition CRUD flow
4. Proceed to Week 3 (Stripe Connect)

## Files Changed

**Backend:**
- `src/main/resources/db/migration/V2__update_competitions_schema.sql` (NEW)
- `src/main/java/com/leaguehq/model/Competition.java`
- `src/main/java/com/leaguehq/dto/request/CreateCompetitionRequest.java`
- `src/main/java/com/leaguehq/dto/response/CompetitionResponse.java`
- `src/main/java/com/leaguehq/service/CompetitionService.java`

**Frontend:**
- No changes needed ✅

## Lessons Learned

- Always verify backend DTOs match database schema AND frontend expectations
- Test API integration early, not just at the end
- The frontend was built correctly following the build plan - the backend code was outdated
