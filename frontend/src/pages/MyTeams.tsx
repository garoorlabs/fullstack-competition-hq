import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { teamApi } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import type { Team } from '../types';
import Header from '../components/layout/Header';
import SubscriptionStatusBadge from '../components/common/SubscriptionStatusBadge';
import Spinner from '../components/common/Spinner';

export default function MyTeams() {
  const { user } = useAuth();
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchTeams();
  }, [user]);

  const fetchTeams = async () => {
    try {
      const data = await teamApi.getMyTeams();
      setTeams(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load teams');
    } finally {
      setLoading(false);
    }
  };

  const getActionButton = (team: Team) => {
    if (team.subscriptionStatus === 'PAST_DUE' || team.subscriptionStatus === 'UNPAID') {
      return (
        <Link
          to={`/teams/${team.id}/payment`}
          className="w-full inline-block text-center bg-yellow-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors"
        >
          Update Payment
        </Link>
      );
    }

    return (
      <Link
        to={`/teams/${team.id}/roster`}
        className="w-full inline-block text-center bg-indigo-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
      >
        Manage Roster
      </Link>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">My Teams</h2>
          </div>

          {error && (
            <div className="rounded-md bg-red-50 p-4 mb-6">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          {teams.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-lg shadow">
              <svg
                className="mx-auto h-12 w-12 text-gray-400 mb-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No teams yet
              </h3>
              <p className="text-gray-500 mb-4">
                Register your first team for an upcoming competition.
              </p>
              <Link
                to="/competitions"
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
              >
                Browse Competitions
              </Link>
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {teams.map((team) => (
                <div
                  key={team.id}
                  className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-6"
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {team.name}
                    </h3>
                    <SubscriptionStatusBadge status={team.subscriptionStatus} />
                  </div>

                  <p className="text-sm text-gray-600 mb-4">
                    {team.competitionName}
                  </p>

                  <div className="space-y-2 text-sm text-gray-500 mb-4">
                    <div className="flex justify-between">
                      <span>Players:</span>
                      <span className="font-medium text-gray-900">
                        {team.rosterSize}
                      </span>
                    </div>

                    {team.entryFeePaid && (
                      <div className="flex justify-between">
                        <span>Entry Fee:</span>
                        <span className="font-medium text-green-600">
                          Paid ✓
                        </span>
                      </div>
                    )}

                    {team.rosterLocked && (
                      <div className="flex justify-between">
                        <span>Roster:</span>
                        <span className="font-medium text-gray-900">
                          Locked
                        </span>
                      </div>
                    )}
                  </div>

                  {getActionButton(team)}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
