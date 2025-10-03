import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { teamApi } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import type { Team } from '../types';
import Header from '../components/layout/Header';
import Spinner from '../components/common/Spinner';

export default function UpdatePayment() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [team, setTeam] = useState<Team | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) {
      setError('Team ID is missing');
      setLoading(false);
      return;
    }

    // Note: We'd need to add getTeamById to teamApi for this to work properly
    // For now, we'll just show the form
    setLoading(false);
  }, [id]);

  const handleUpdatePayment = async () => {
    if (!id) return;

    setSubmitting(true);
    setError('');

    try {
      const response = await teamApi.updatePaymentMethod(id);
      // Redirect to Stripe Customer Portal
      window.location.href = response.sessionUrl;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create payment portal session');
      setSubmitting(false);
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
            <p className="text-gray-600">Only coaches can update payment methods</p>
          </div>
        </div>
      </div>
    );
  }

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

      <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-8">
            <div className="mb-6">
              <h1 className="text-2xl font-bold text-gray-900">
                Update Payment Method
              </h1>
            </div>

            {/* Warning Alert */}
            <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path
                      fillRule="evenodd"
                      d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-yellow-800">
                    Payment Update Required
                  </h3>
                  <div className="mt-2 text-sm text-yellow-700">
                    <p>
                      Your subscription payment has failed. To continue participating in the competition,
                      please update your payment method.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {error && (
              <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-sm text-red-800">{error}</p>
              </div>
            )}

            <div className="space-y-4 mb-6">
              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="text-sm font-medium text-gray-900 mb-2">What happens next?</h3>
                <ul className="text-sm text-gray-600 list-disc list-inside space-y-1">
                  <li>You'll be redirected to a secure payment portal</li>
                  <li>Update your payment method or billing details</li>
                  <li>Your subscription will automatically retry</li>
                  <li>You'll receive a confirmation email once updated</li>
                </ul>
              </div>

              <div className="bg-blue-50 rounded-lg p-4">
                <p className="text-sm text-blue-800">
                  <strong>Monthly Subscription:</strong> $20.00/month
                </p>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <button
                type="button"
                onClick={() => navigate('/teams')}
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                disabled={submitting}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleUpdatePayment}
                disabled={submitting}
                className="px-6 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submitting ? 'Loading...' : 'Update Payment Method'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
