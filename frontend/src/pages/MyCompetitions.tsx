import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { competitionApi } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import type { Competition } from '../types';
import Header from '../components/layout/Header';
import StatusBadge from '../components/common/StatusBadge';
import Spinner from '../components/common/Spinner';

export default function MyCompetitions() {
  const { user } = useAuth();
  const [competitions, setCompetitions] = useState<Competition[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchCompetitions();
  }, [user]);

  const fetchCompetitions = async () => {
    try {
      if (user?.role === 'COMPETITION_OWNER') {
        const data = await competitionApi.getMyCompetitions();
        setCompetitions(data);
      } else if (user?.role === 'COACH') {
        const data = await competitionApi.getPublishedCompetitions();
        setCompetitions(data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load competitions');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
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
            <h2 className="text-2xl font-bold text-gray-900">
              {user?.role === 'COACH' ? 'Available Competitions' : 'My Competitions'}
            </h2>
            {user?.role === 'COMPETITION_OWNER' && (
              <Link
                to="/competitions/new"
                className="bg-indigo-600 text-white px-4 py-2 rounded-md font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
              >
                + New Competition
              </Link>
            )}
          </div>

          {error && (
            <div className="rounded-md bg-red-50 p-4 mb-6">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          {competitions.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-lg shadow">
              <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {user?.role === 'COACH' ? 'No competitions available' : 'No competitions yet'}
              </h3>
              <p className="text-gray-500 mb-4">
                {user?.role === 'COACH'
                  ? 'Check back later for published competitions.'
                  : 'Get started by creating your first competition.'}
              </p>
              {user?.role === 'COMPETITION_OWNER' && (
                <Link
                  to="/competitions/new"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
                >
                  + New Competition
                </Link>
              )}
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {competitions.map((competition) => (
                <Link
                  key={competition.id}
                  to={`/competitions/${competition.id}`}
                  className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-6"
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {competition.name}
                    </h3>
                    <StatusBadge status={competition.status} />
                  </div>
                  <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                    {competition.description}
                  </p>
                  <div className="space-y-2 text-sm text-gray-500">
                    <div className="flex justify-between">
                      <span>Format:</span>
                      <span className="font-medium text-gray-900">
                        {competition.format.replace('_', ' ')}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Teams:</span>
                      <span className="font-medium text-gray-900">
                        {competition.currentTeamCount} / {competition.maxTeams}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Start Date:</span>
                      <span className="font-medium text-gray-900">
                        {formatDate(competition.startDate)}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Entry Fee:</span>
                      <span className="font-medium text-gray-900">
                        ${competition.entryFee}
                      </span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
