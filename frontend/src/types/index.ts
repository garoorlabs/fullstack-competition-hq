// User & Auth Types
export type UserRole = 'COMPETITION_OWNER' | 'COACH' | 'PLATFORM_OWNER';

export type StripeConnectStatus = 'NOT_STARTED' | 'INCOMPLETE' | 'VERIFIED' | 'BLOCKED';

export type PayoutStatus = 'NONE' | 'PENDING' | 'ENABLED' | 'BLOCKED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  stripeCustomerId: string | null;
  stripeConnectAccountId: string | null;
  stripeConnectStatus: StripeConnectStatus;
  payoutStatus: PayoutStatus;
  createdAt: string | null;
  lastLoginAt: string | null;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
}

// Competition Types
export type CompetitionFormat = 'LEAGUE' | 'KNOCKOUT' | 'ROUND_ROBIN';
export type CompetitionStatus = 'DRAFT' | 'PUBLISHED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type TeamSize = 'FIVE_V_FIVE' | 'SIX_V_SIX' | 'SEVEN_V_SEVEN' | 'EIGHT_V_EIGHT' | 'NINE_V_NINE' | 'ELEVEN_V_ELEVEN';

export interface Policy {
  refundPolicy: string;
  codeOfConduct: string;
  photoConsent: boolean;
}

export interface Venue {
  id: string;
  name: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  fieldCount: number;
  hasLights: boolean;
  notes: string | null;
}

export interface Competition {
  id: string;
  name: string;
  description: string;
  format: CompetitionFormat;
  teamSize: TeamSize;
  status: CompetitionStatus;
  startDate: string;
  endDate: string;
  registrationDeadline: string;
  entryFee: number;
  maxTeams: number;
  currentTeamCount: number;
  policy: Policy;
  shareToken: string;
  ownerId: string;
  ownerName: string;
  venues: Venue[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateCompetitionRequest {
  name: string;
  description: string;
  format: CompetitionFormat;
  teamSize: TeamSize;
  startDate: string;
  endDate: string;
  registrationDeadline: string;
  entryFee: number;
  maxTeams: number;
  policy?: Policy;
  venue: {
    name: string;
    address: string;
    city: string;
    state: string;
    zipCode: string;
    fieldCount: number;
    hasLights: boolean;
    notes?: string;
  };
}

// Team Types
export type SubscriptionStatus = 'ACTIVE' | 'PAST_DUE' | 'CANCELED' | 'UNPAID';

export interface Team {
  id: string;
  competitionId: string;
  competitionName: string;
  coachId: string;
  coachName: string;
  coachEmail: string;
  name: string;
  entryFeePaid: boolean;
  entryFeePaidAt: string | null;
  subscriptionId: string | null;
  subscriptionStatus: SubscriptionStatus | null;
  subscriptionCurrentPeriodStart: string | null;
  subscriptionCurrentPeriodEnd: string | null;
  subscriptionCancelAt: string | null;
  isEligible: boolean;
  rosterSize: number;
  rosterLocked: boolean;
  rosterLockedAt: string | null;
  createdAt: string;
  registeredAt: string;
}

// API Error Response
export interface ApiError {
  status: number;
  message: string;
  path: string;
  timestamp: string;
  errors?: Record<string, string>;
}
