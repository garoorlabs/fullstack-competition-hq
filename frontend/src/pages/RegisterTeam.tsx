import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { teamApi } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import Header from '../components/layout/Header';

export default function RegisterTeam() {
  const { competitionId } = useParams<{ competitionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [teamName, setTeamName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!competitionId) {
      setError('Competition ID is missing');
      return;
    }

    if (!teamName.trim()) {
      setError('Team name is required');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await teamApi.registerTeam({
        competitionId,
        teamName: teamName.trim(),
      });

      // Redirect to Stripe Checkout
      window.location.href = response.sessionUrl;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to register team');
      setLoading(false);
    }
  };

  if (!user || user.role !== 'COACH') {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Access Denied
            </h2>
            <p className="text-gray-600">Only coaches can register teams</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-8">
            <h1 className="text-2xl font-bold text-gray-900 mb-6">
              Register Your Team
            </h1>

            <p className="text-gray-600 mb-6">
              Enter your team name to continue to payment. You'll be charged the competition entry fee plus $20 for the first month's subscription.
            </p>

            {error && (
              <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-sm text-red-800">{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="mb-6">
                <label htmlFor="teamName" className="block text-sm font-medium text-gray-700 mb-2">
                  Team Name
                </label>
                <input
                  type="text"
                  id="teamName"
                  value={teamName}
                  onChange={(e) => setTeamName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="Enter team name"
                  maxLength={100}
                  required
                  disabled={loading}
                />
              </div>

              <div className="flex items-center justify-between">
                <button
                  type="button"
                  onClick={() => navigate(-1)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                  disabled={loading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Processing...' : 'Continue to Payment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
