# LeagueHQ - Wireframes & User Flows

## Overview
Page layouts and user flows for MVP. Aligns with build plan phases and UI design specs.

**Format:** Text-based wireframes with ASCII diagrams
**Purpose:** Guide frontend implementation, not final pixel-perfect designs

---

## User Flows

### Flow 1: Owner Creates Competition → Connects Stripe → Publishes

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   Sign Up   │ --> │ Create Comp  │ --> │  Connect    │ --> │   Publish    │
│   (Owner)   │     │  (Draft)     │     │   Stripe    │     │ Competition  │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
                                                 |
                                                 v
                                          ┌─────────────┐
                                          │   Stripe    │
                                          │  Onboarding │
                                          └─────────────┘
```

### Flow 2: Coach Registers Team → Pays Entry Fee → Adds Roster

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   Sign Up   │ --> │ Find Comp    │ --> │  Register   │ --> │   Stripe     │
│   (Coach)   │     │  (Browse)    │     │    Team     │     │  Checkout    │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
                                                                      |
                                                                      v
                                                               ┌──────────────┐
                                                               │ Add Players  │
                                                               │  + Photos    │
                                                               └──────────────┘
```

### Flow 3: Owner Creates Matches → Enters Results → Standings Update

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│ Competition │ --> │ Create Match │ --> │   Enter     │ --> │  Standings   │
│  Dashboard  │     │  Schedule    │     │  Results    │     │    Update    │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
```

---

## Page Wireframes

### 1. Login Page (`/login`)

```
┌────────────────────────────────────────┐
│                                        │
│         🏆 LeagueHQ Logo               │
│                                        │
│    ┌────────────────────────────┐     │
│    │  Email                     │     │
│    │  [_________________]       │     │
│    │                            │     │
│    │  Password                  │     │
│    │  [_________________]       │     │
│    │                            │     │
│    │  [  Login  ]               │     │
│    │                            │     │
│    │  Don't have an account?    │     │
│    │  Sign up                   │     │
│    └────────────────────────────┘     │
│                                        │
└────────────────────────────────────────┘
```

**Key Elements:**
- Centered card layout
- Email + password inputs
- Login button (primary)
- Link to signup page
- Error message displayed above form

---

### 2. Signup Page (`/signup`)

```
┌────────────────────────────────────────┐
│                                        │
│         🏆 LeagueHQ - Sign Up          │
│                                        │
│    ┌────────────────────────────┐     │
│    │  Full Name                 │     │
│    │  [_________________]       │     │
│    │                            │     │
│    │  Email                     │     │
│    │  [_________________]       │     │
│    │                            │     │
│    │  Password                  │     │
│    │  [_________________]       │     │
│    │                            │     │
│    │  I am a:                   │     │
│    │  ○ Competition Owner       │     │
│    │  ○ Coach                   │     │
│    │                            │     │
│    │  [  Create Account  ]      │     │
│    │                            │     │
│    │  Already have an account?  │     │
│    │  Log in                    │     │
│    └────────────────────────────┘     │
│                                        │
└────────────────────────────────────────┘
```

**Key Elements:**
- Full name, email, password fields
- Role selection (radio buttons)
- Create account button
- Link to login page

---

### 3. My Competitions (Owner) (`/competitions`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | My Competitions | Profile ▼        │
├────────────────────────────────────────────────────────┤
│                                                        │
│  My Competitions             [+ New Competition]      │
│  ────────────────                                     │
│                                                        │
│  ┌─────────────────────┐  ┌─────────────────────┐    │
│  │ Summer League 2025  │  │ Youth Tournament    │    │
│  │                     │  │                     │    │
│  │ 11v11 League        │  │ 7v7 Knockout        │    │
│  │ Entry: $150         │  │ Entry: $75          │    │
│  │ 8/20 teams          │  │ 12/16 teams         │    │
│  │                     │  │                     │    │
│  │ [ACTIVE]            │  │ [PUBLISHED]         │    │
│  └─────────────────────┘  └─────────────────────┘    │
│                                                        │
│  ┌─────────────────────┐                              │
│  │ Fall League 2025    │                              │
│  │                     │                              │
│  │ 11v11 League        │                              │
│  │ Entry: $200         │                              │
│  │ 0/24 teams          │                              │
│  │                     │                              │
│  │ [DRAFT]             │                              │
│  └─────────────────────┘                              │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Header with navigation
- "New Competition" button (top right)
- Grid of competition cards (3 columns desktop, 1 mobile)
- Each card shows: name, format, entry fee, team count, status badge
- Click card to view details

---

### 4. Create Competition (`/competitions/new`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Create Competition | Profile ▼     │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Create Competition                                    │
│  ──────────────────                                    │
│                                                        │
│  Basic Information                                     │
│  ┌──────────────────────────────────────────┐         │
│  │ Competition Name                         │         │
│  │ [_____________________________]          │         │
│  │                                          │         │
│  │ Description                              │         │
│  │ [_____________________________]          │         │
│  │ [_____________________________]          │         │
│  │                                          │         │
│  │ Format            Team Size              │         │
│  │ [League ▼]        [11v11 ▼]              │         │
│  │                                          │         │
│  │ Entry Fee         Max Teams              │         │
│  │ [$_______]        [____]                 │         │
│  │                                          │         │
│  │ Start Date        End Date               │         │
│  │ [__/__/____]      [__/__/____]           │         │
│  └──────────────────────────────────────────┘         │
│                                                        │
│  Venue                                                 │
│  ┌──────────────────────────────────────────┐         │
│  │ Venue Name                               │         │
│  │ [_____________________________]          │         │
│  │                                          │         │
│  │ Address                                  │         │
│  │ [_____________________________]          │         │
│  │ [_____________________________]          │         │
│  └──────────────────────────────────────────┘         │
│                                                        │
│  [Cancel]              [Create Competition]           │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Two-column form layout (desktop)
- Dropdowns for format and team size
- Date pickers for start/end dates
- Venue section (name + address)
- Cancel (secondary) and Create (primary) buttons

---

### 5. Competition Detail (Owner View) (`/competitions/:id`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Competitions | Profile ▼           │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Summer League 2025                        [DRAFT]    │
│  ───────────────────                                   │
│                                                        │
│  11v11 League • Mar 15 - Jun 15, 2025                 │
│  Entry Fee: $150 • 8/20 teams registered              │
│                                                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │ ⚠️  Connect Stripe to publish this competition  │  │
│  │                                                 │  │
│  │ Payout Status: NOT_STARTED                      │  │
│  │ [Connect Stripe Account]                        │  │
│  └─────────────────────────────────────────────────┘  │
│                                                        │
│  [Tabs: Overview | Teams | Matches | Standings]       │
│  ───────────────────────────────────────────           │
│                                                        │
│  Description                                           │
│  Competitive summer league for adult teams...          │
│                                                        │
│  Venue                                                 │
│  Main Stadium, 123 Sports Ave, Springfield            │
│                                                        │
│  Share Link                                            │
│  https://leaguehq.com/join/abc123xyz  [Copy]          │
│                                                        │
│  [Edit Competition]  [Publish Competition]            │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Competition name + status badge (top)
- Meta info: format, dates, entry fee, team count
- Stripe connection alert (if not connected)
- Tabs for different sections
- Share link with copy button
- Action buttons: Edit, Publish

---

### 5b. Competition Detail - Teams Tab (Owner View) (`/competitions/:id` - Teams tab)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Competitions | Profile ▼           │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Summer League 2025                        [ACTIVE]   │
│  ───────────────────                                   │
│                                                        │
│  11v11 League • Mar 15 - Jun 15, 2025                 │
│  Entry Fee: $150 • 8/20 teams registered              │
│                                                        │
│  [Tabs: Overview | TEAMS | Matches | Standings]       │
│  ═══════════════════════════════════════════           │
│                                                        │
│  Registered Teams (8)                                 │
│  ────────────────────                                  │
│                                                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │ Team Name      Coach           Sub Status  Size │  │
│  ├─────────────────────────────────────────────────┤  │
│  │ Thunder FC     John Doe         [ACTIVE]    15  │  │
│  │                john@email.com   Mar 1, 2025     │  │
│  │                                 [View Roster]   │  │
│  ├─────────────────────────────────────────────────┤  │
│  │ Lightning Utd  Sarah Smith      [ACTIVE]    12  │  │
│  │                sarah@email.com  Mar 3, 2025     │  │
│  │                                 [View Roster]   │  │
│  ├─────────────────────────────────────────────────┤  │
│  │ Storm Strikers Mike Jones       [PAST_DUE]  8   │  │
│  │                mike@email.com   Mar 5, 2025     │  │
│  │                                 [View Roster]   │  │
│  ├─────────────────────────────────────────────────┤  │
│  │ ... (5 more teams)                              │  │
│  └─────────────────────────────────────────────────┘  │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Teams tab is active (underlined/highlighted)
- Table showing all registered teams
- Each row shows:
  - Team name
  - Coach name and email
  - Subscription status badge (ACTIVE, PAST_DUE, CANCELLED)
  - Registration date
  - Roster size (current player count)
  - "View Roster" button to see players
- Visual indication of payment issues (PAST_DUE in yellow/red)
- Sortable by registration date (newest first)

---

### 5c. Update Payment Page (Coach View) (`/teams/:id/payment`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | My Teams | Profile ▼               │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Update Payment Method                                │
│  ─────────────────────                                 │
│                                                        │
│  ⚠️  Your subscription payment failed                  │
│                                                        │
│  Team: Thunder FC                                     │
│  Competition: Summer League 2025                      │
│  Status: PAST_DUE                                     │
│                                                        │
│  To continue participating, please update your        │
│  payment method.                                      │
│                                                        │
│  Monthly Subscription: $20.00/month                   │
│  Next Payment Attempt: Mar 15, 2025                   │
│                                                        │
│  [Update Payment Method]                              │
│                                                        │
│  This will redirect you to a secure payment portal   │
│  where you can update your card information.          │
│                                                        │
│  [Cancel]                                             │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Warning alert for payment failure
- Team and competition context
- Clear explanation of issue
- Monthly subscription amount shown
- "Update Payment Method" button redirects to Stripe Customer Portal
- Cancel button returns to My Teams

---

### 6. Competition Detail (Public/Coach View) (`/competitions/:id`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Browse | Profile ▼                 │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Summer League 2025                    [PUBLISHED]    │
│  ───────────────────                                   │
│                                                        │
│  11v11 League • Mar 15 - Jun 15, 2025                 │
│  Entry Fee: $150 • 8/20 teams registered              │
│                                                        │
│  [Register Team]                                       │
│                                                        │
│  Description                                           │
│  Competitive summer league for adult teams...          │
│                                                        │
│  Venue                                                 │
│  Main Stadium, 123 Sports Ave, Springfield            │
│                                                        │
│  Registered Teams                                      │
│  • Thunder FC                                          │
│  • Lightning United                                    │
│  • Storm Strikers                                      │
│  • ... (5 more)                                        │
│                                                        │
│  [Register Team]                                       │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- No edit/publish buttons (public view)
- "Register Team" prominent CTA
- List of registered teams
- Competition details visible

---

### 7. Register Team (`/competitions/:id/register`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Register Team | Profile ▼          │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Register Team for: Summer League 2025                │
│  ──────────────────────────────────────                │
│                                                        │
│  Team Information                                      │
│  ┌──────────────────────────────────────────┐         │
│  │ Team Name                                │         │
│  │ [_____________________________]          │         │
│  └──────────────────────────────────────────┘         │
│                                                        │
│  Payment Summary                                       │
│  ┌──────────────────────────────────────────┐         │
│  │ Entry Fee                        $150.00 │         │
│  │ First Month Subscription          $20.00 │         │
│  │ ─────────────────────────────────────    │         │
│  │ Total Due                         $170.00│         │
│  └──────────────────────────────────────────┘         │
│                                                        │
│  By registering, you agree to:                        │
│  • Monthly subscription of $20/month                  │
│  • Competition terms and conditions                   │
│                                                        │
│  [Cancel]              [Proceed to Payment]           │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Team name input
- Payment breakdown (entry fee + subscription)
- Terms agreement text
- Proceed to Payment redirects to Stripe Checkout

---

### 8. My Teams (Coach) (`/teams`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | My Teams | Profile ▼               │
├────────────────────────────────────────────────────────┤
│                                                        │
│  My Teams                                              │
│  ────────                                              │
│                                                        │
│  ┌─────────────────────┐  ┌─────────────────────┐    │
│  │ Thunder FC          │  │ Lightning Reserves  │    │
│  │                     │  │                     │    │
│  │ Summer League 2025  │  │ Youth Tournament    │    │
│  │                     │  │                     │    │
│  │ 15 players          │  │ 12 players          │    │
│  │ [ACTIVE]            │  │ [PAST_DUE]          │    │
│  │                     │  │                     │    │
│  │ [Manage Roster]     │  │ [Update Payment]    │    │
│  └─────────────────────┘  └─────────────────────┘    │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Grid of team cards
- Each card shows: team name, competition, player count, subscription status
- CTA based on status (Manage Roster or Update Payment)

---

### 9. Team Roster (`/teams/:id/roster`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Teams | Profile ▼                  │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Thunder FC - Roster                                   │
│  ────────────────────                                  │
│                                                        │
│  Summer League 2025 • 15/20 players                   │
│                                                        │
│  [+ Add Player]                                        │
│                                                        │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐      │
│  │  [📷]  │  │  [📷]  │  │  [📷]  │  │  [📷]  │      │
│  │        │  │        │  │        │  │        │      │
│  │ John   │  │ Sarah  │  │ Mike   │  │ Lisa   │      │
│  │ Doe    │  │ Smith  │  │ Jones  │  │ Brown  │      │
│  │        │  │        │  │        │  │        │      │
│  │ #10    │  │ #7     │  │ #9     │  │ #3     │      │
│  │ Forward│  │ Mid    │  │ Goal   │  │ Defense│      │
│  │        │  │        │  │        │  │        │      │
│  │ [Edit] │  │ [Edit] │  │ [Edit] │  │ [Edit] │      │
│  └────────┘  └────────┘  └────────┘  └────────┘      │
│                                                        │
│  ... (11 more players)                                │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Player count indicator
- Add Player button
- Grid of player cards (4 columns desktop, 2 mobile)
- Each card: photo, name, jersey number, position, edit button
- Photo fallback: initials avatar

---

### 10. Add/Edit Player (`/teams/:id/roster/add`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Teams | Profile ▼                  │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Add Player to Thunder FC                             │
│  ─────────────────────────                             │
│                                                        │
│  ┌──────────────────────────────────────────┐         │
│  │ Full Name                                │         │
│  │ [_____________________________]          │         │
│  │                                          │         │
│  │ Jersey Number        Position            │         │
│  │ [____]               [Forward ▼]         │         │
│  │                                          │         │
│  │ Player Photo                             │         │
│  │ ┌──────────────┐                         │         │
│  │ │   [📷]       │                         │         │
│  │ │              │                         │         │
│  │ │  Preview     │                         │         │
│  │ └──────────────┘                         │         │
│  │ [Choose File]                            │         │
│  │ PNG, JPG up to 5MB                       │         │
│  └──────────────────────────────────────────┘         │
│                                                        │
│  [Cancel]              [Add Player]                   │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Full name input
- Jersey number and position
- Photo upload with preview
- File size limit displayed
- Cancel and Add Player buttons

---

### 11. Matches Schedule (Owner) (`/competitions/:id/matches`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Competitions | Profile ▼           │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Summer League 2025 - Matches                         │
│  ─────────────────────────────                         │
│                                                        │
│  [+ Create Match]                                      │
│                                                        │
│  Week 1 - March 15                                    │
│  ┌─────────────────────────────────────────────────┐  │
│  │ Thunder FC  vs  Lightning United                │  │
│  │ 3:00 PM • Main Stadium                          │  │
│  │                                                 │  │
│  │ [COMPLETED]  Score: 2 - 1  [Edit Result]       │  │
│  └─────────────────────────────────────────────────┘  │
│                                                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │ Storm Strikers  vs  Eagles FC                   │  │
│  │ 5:00 PM • Main Stadium                          │  │
│  │                                                 │  │
│  │ [SCHEDULED]  [Enter Result]                     │  │
│  └─────────────────────────────────────────────────┘  │
│                                                        │
│  Week 2 - March 22                                    │
│  ... (more matches)                                   │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Create Match button
- Matches grouped by week/date
- Each match card: teams, time, venue, status
- Action buttons based on status (Enter Result, Edit Result)

---

### 12. Enter Match Result (`/matches/:id/result`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Matches | Profile ▼                │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Enter Match Result                                   │
│  ───────────────────                                   │
│                                                        │
│  Thunder FC  vs  Lightning United                     │
│  March 15, 2025 at 3:00 PM                            │
│                                                        │
│  ┌──────────────────────────────────────────┐         │
│  │                                          │         │
│  │      Thunder FC          Lightning Utd   │         │
│  │                                          │         │
│  │       [___]        :        [___]        │         │
│  │                                          │         │
│  │        Home                   Away        │         │
│  │                                          │         │
│  └──────────────────────────────────────────┘         │
│                                                        │
│  Match Status                                         │
│  ○ Completed   ○ Postponed   ○ Cancelled              │
│                                                        │
│  [Cancel]              [Save Result]                  │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Match details (teams, date, time)
- Score inputs for home/away
- Match status radio buttons
- Save Result button

---

### 13. Standings (`/competitions/:id/standings`)

```
┌────────────────────────────────────────────────────────┐
│  Header: LeagueHQ | Competitions | Profile ▼           │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Summer League 2025 - Standings                       │
│  ───────────────────────────────                       │
│                                                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │ Pos  Team               P  W  D  L  GD  Pts     │  │
│  ├─────────────────────────────────────────────────┤  │
│  │  1   Thunder FC        3  3  0  0  +5   9      │  │
│  │  2   Lightning Utd     3  2  1  0  +3   7      │  │
│  │  3   Storm Strikers    3  1  1  1   0   4      │  │
│  │  4   Eagles FC         3  1  0  2  -2   3      │  │
│  │  5   Wolves United     3  0  1  2  -3   1      │  │
│  │  6   Hawks Athletic    3  0  1  2  -3   1      │  │
│  │  ... (2 more teams)                            │  │
│  └─────────────────────────────────────────────────┘  │
│                                                        │
│  Last Updated: Mar 22, 2025 at 6:30 PM                │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Key Elements:**
- Standings table (scrollable on mobile)
- Columns: Position, Team, Played, Won, Drawn, Lost, Goal Diff, Points
- Sorted by points, then goal difference
- Last updated timestamp

---

## Mobile Considerations

### Navigation (Mobile <768px)
```
┌─────────────────────────┐
│ ☰  LeagueHQ        👤   │  <- Hamburger + Avatar
├─────────────────────────┤
│                         │
│  [Content Area]         │
│                         │
└─────────────────────────┘

Hamburger opens slide-out menu:
┌─────────────────┐
│ ✕ Menu          │
├─────────────────┤
│ My Competitions │
│ My Teams        │
│ Browse          │
│ Settings        │
│ Logout          │
└─────────────────┘
```

### Forms (Mobile)
- Single column layout
- Full-width inputs
- Larger touch targets (44px min)

### Tables (Mobile)
- Horizontal scroll
- OR card-based layout instead of table

---

## Error States

### 404 Not Found
```
┌────────────────────────────────────────┐
│                                        │
│           404                          │
│    Page Not Found                      │
│                                        │
│  The page you're looking for          │
│  doesn't exist.                        │
│                                        │
│  [← Back to Dashboard]                 │
│                                        │
└────────────────────────────────────────┘
```

### Payment Failed
```
┌────────────────────────────────────────┐
│                                        │
│     ⚠️  Payment Failed                 │
│                                        │
│  Your payment could not be processed. │
│                                        │
│  Please check your payment method     │
│  and try again.                        │
│                                        │
│  [Try Again]  [Contact Support]       │
│                                        │
└────────────────────────────────────────┘
```

---

## Success States

### Team Registered
```
┌────────────────────────────────────────┐
│                                        │
│     ✅  Team Registered!               │
│                                        │
│  Thunder FC has been successfully     │
│  registered for Summer League 2025.   │
│                                        │
│  Next steps:                           │
│  • Add players to your roster          │
│  • Upload player photos                │
│                                        │
│  [Add Players]  [View Team]            │
│                                        │
└────────────────────────────────────────┘
```

---

## Notes

- Wireframes show desktop layouts; mobile adapts to single column
- All flows align with build plan phases (Week 1-7)
- Interactive elements follow UI design specs
- Actual implementation may adjust layouts based on testing
- Add more wireframes as features are built
