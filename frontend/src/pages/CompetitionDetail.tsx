import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { competitionApi, stripeApi } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import type { Competition, Team } from '../types';
import Header from '../components/layout/Header';
import StatusBadge from '../components/common/StatusBadge';
import SubscriptionStatusBadge from '../components/common/SubscriptionStatusBadge';
import Spinner from '../components/common/Spinner';
import Tabs from '../components/common/Tabs';
import OverviewTab from '../components/competition/OverviewTab';
import TeamsTab from '../components/competition/TeamsTab';

export default function CompetitionDetail() {
  const { id } = useParams<{ id: string }>();
  const { user, refreshUser } = useAuth();
  const [competition, setCompetition] = useState<Competition | null>(null);
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [teamsLoading, setTeamsLoading] = useState(false);
  const [error, setError] = useState('');
  const [publishLoading, setPublishLoading] = useState(false);
  const [connectLoading, setConnectLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    if (id) {
      fetchCompetition(id);
    }
  }, [id]);

  useEffect(() => {
    // Refresh user data when component mounts to get latest Stripe status
    refreshUser().catch(err => {
      console.error('Failed to refresh user data:', err);
    });
  }, []);

  const fetchCompetition = async (competitionId: string) => {
    try {
      const data = await competitionApi.getById(competitionId);
      setCompetition(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load competition');
    } finally {
      setLoading(false);
    }
  };

  const fetchTeams = async (competitionId: string) => {
    setTeamsLoading(true);
    try {
      const data = await competitionApi.getTeams(competitionId);
      setTeams(data);
    } catch (err: any) {
      console.error('Failed to load teams:', err);
    } finally {
      setTeamsLoading(false);
    }
  };

  useEffect(() => {
    if (id && activeTab === 'teams') {
      fetchTeams(id);
    }
  }, [id, activeTab]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const handleConnectStripe = async () => {
    setConnectLoading(true);
    try {
      const response = await stripeApi.createConnectOnboardingLink();
      // Redirect to Stripe Connect onboarding
      window.location.href = response.url;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create Stripe onboarding link');
      setConnectLoading(false);
    }
  };

  const handlePublish = async () => {
    if (!id) return;
    setPublishLoading(true);
    try {
      const updated = await competitionApi.publish(id);
      setCompetition(updated);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to publish competition');
    } finally {
      setPublishLoading(false);
    }
  };

  const handleSyncStatus = async () => {
    try {
      await stripeApi.refreshAccountStatus();
      await refreshUser();
    } catch (err: any) {
      console.error('Failed to sync Stripe status:', err);
      setError('Failed to sync Stripe status. Please try again.');
    }
  };

  const isOwner = user && competition && user.id === competition.ownerId;
  const canPublish = isOwner && competition?.status === 'DRAFT' && user.payoutStatus === 'ENABLED';
  const needsStripeConnect = isOwner && competition?.status === 'DRAFT' && user.payoutStatus !== 'ENABLED';

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (!competition) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Competition Not Found
            </h2>
            <p className="text-gray-600">This competition does not exist</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-6">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">{competition.name}</h1>
              <p className="mt-2 text-gray-600">
                {competition.teamSize.replace(/_/g, ' ')} {competition.format} • {formatDate(competition.startDate)} - {formatDate(competition.endDate)}
              </p>
              <p className="mt-1 text-gray-600">
                Entry Fee: ${competition.entryFee} • {competition.currentTeamCount}/{competition.maxTeams} teams registered
              </p>
            </div>
            <StatusBadge status={competition.status} />
          </div>
        </div>

        {/* Stripe Success Banner (Owner Only) */}
        {isOwner && competition?.status === 'DRAFT' && user?.payoutStatus === 'ENABLED' && (
          <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <h3 className="text-sm font-medium text-green-800">
                  Stripe Connected - Ready to Publish
                </h3>
                <p className="mt-1 text-sm text-green-700">
                  Your Stripe account is connected and verified. You can now publish this competition to accept team registrations.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Stripe Connect Alert (Owner Only) */}
        {needsStripeConnect && (
          <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3 flex-1">
                <h3 className="text-sm font-medium text-yellow-800">
                  Connect Stripe to publish this competition
                </h3>
                <p className="mt-2 text-sm text-yellow-700">
                  Payout Status: {user?.payoutStatus || 'NOT_STARTED'}
                </p>
                <div className="mt-3 flex space-x-2">
                  <button
                    onClick={handleConnectStripe}
                    disabled={connectLoading}
                    className="bg-yellow-600 text-white px-4 py-2 rounded-md hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium"
                  >
                    {connectLoading ? 'Loading...' : user?.payoutStatus === 'PENDING' ? 'Resume Onboarding' : 'Connect Stripe Account'}
                  </button>
                  {user?.payoutStatus === 'PENDING' && (
                    <button
                      onClick={handleSyncStatus}
                      className="bg-white text-yellow-700 px-4 py-2 rounded-md border border-yellow-300 hover:bg-yellow-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 text-sm font-medium"
                    >
                      Sync Status
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        {/* Content Card with Tabs (Owner View) */}
        {isOwner ? (
          <div className="bg-white shadow rounded-lg">
            <div className="p-6">
              <Tabs
                tabs={[
                  {
                    id: 'overview',
                    label: 'Overview',
                    content: <OverviewTab competition={competition} isOwner={isOwner} />,
                  },
                  {
                    id: 'teams',
                    label: 'Teams',
                    content: <TeamsTab teams={teams} loading={teamsLoading} />,
                  },
                  {
                    id: 'matches',
                    label: 'Matches',
                    content: <div className="py-12 text-center text-gray-500">Matches feature coming in Week 7</div>,
                  },
                  {
                    id: 'standings',
                    label: 'Standings',
                    content: <div className="py-12 text-center text-gray-500">Standings feature coming in Week 7</div>,
                  },
                ]}
                activeTab={activeTab}
                onChange={setActiveTab}
              />
            </div>

            {/* Action Buttons */}
            <div className="bg-gray-50 px-6 py-4 flex space-x-3 border-t border-gray-200">
              <button className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                Edit Competition
              </button>
              {canPublish && (
                <button
                  onClick={handlePublish}
                  disabled={publishLoading}
                  className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {publishLoading ? 'Publishing...' : 'Publish Competition'}
                </button>
              )}
            </div>
          </div>
        ) : (
          /* Public/Coach View */
          <div className="bg-white shadow rounded-lg">
            <div className="p-6 space-y-6">
              <OverviewTab competition={competition} isOwner={false} />
            </div>

            {/* Register Button (Public/Coach View) */}
            {competition.status === 'PUBLISHED' && user?.role === 'COACH' && (
              <div className="bg-gray-50 px-6 py-4">
                <button
                  onClick={() => window.location.href = `/competitions/${competition.id}/register`}
                  className="w-full sm:w-auto bg-indigo-600 text-white px-6 py-3 rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 font-medium"
                >
                  Register Team
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
