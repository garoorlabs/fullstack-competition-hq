import axios, { AxiosError } from 'axios';
import type {
  AuthResponse,
  LoginRequest,
  SignupRequest,
  Competition,
  CreateCompetitionRequest,
  Team,
  ApiError,
} from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  signup: async (data: SignupRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/signup', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },
};

// Competition API
export const competitionApi = {
  create: async (data: CreateCompetitionRequest): Promise<Competition> => {
    const response = await api.post<Competition>('/competitions', data);
    return response.data;
  },

  getMyCompetitions: async (): Promise<Competition[]> => {
    const response = await api.get<Competition[]>('/competitions/my');
    return response.data;
  },

  getPublishedCompetitions: async (): Promise<Competition[]> => {
    const response = await api.get<Competition[]>('/competitions/published');
    return response.data;
  },

  getById: async (id: string): Promise<Competition> => {
    const response = await api.get<Competition>(`/competitions/${id}`);
    return response.data;
  },

  publish: async (id: string): Promise<Competition> => {
    const response = await api.post<Competition>(`/competitions/${id}/publish`);
    return response.data;
  },

  getTeams: async (competitionId: string): Promise<Team[]> => {
    const response = await api.get<Team[]>(`/competitions/${competitionId}/teams`);
    return response.data;
  },
};

// Stripe API
export const stripeApi = {
  createConnectOnboardingLink: async (): Promise<{ url: string; expiresAt: number }> => {
    const response = await api.post('/stripe/connect-onboarding-link');
    return response.data;
  },
  refreshAccountStatus: async (): Promise<void> => {
    await api.post('/stripe/refresh-account-status');
  },
};

// Team API
export const teamApi = {
  registerTeam: async (data: { competitionId: string; teamName: string }): Promise<{ sessionUrl: string; sessionId: string }> => {
    const response = await api.post('/teams', data);
    return response.data;
  },

  getMyTeams: async (): Promise<Team[]> => {
    const response = await api.get<Team[]>('/teams/my');
    return response.data;
  },

  updatePaymentMethod: async (teamId: string): Promise<{ sessionUrl: string; sessionId: string }> => {
    const response = await api.post(`/teams/${teamId}/update-payment`);
    return response.data;
  },
};

export default api;
