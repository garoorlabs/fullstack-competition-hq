import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ExclamationTriangleIcon } from '@heroicons/react/24/outline';
import { stripeApi } from '../services/api';

export default function StripeRefresh() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleTryAgain = async () => {
    setLoading(true);
    setError('');

    try {
      const { url } = await stripeApi.createConnectOnboardingLink();
      window.location.href = url;
    } catch (err: any) {
      console.error('Failed to create Stripe onboarding link:', err);
      setError(
        err.response?.data?.message ||
        'Failed to create onboarding link. Please try again later.'
      );
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div className="text-center">
          <div className="flex justify-center">
            <ExclamationTriangleIcon className="h-16 w-16 text-yellow-600" />
          </div>

          <h2 className="mt-4 text-xl font-semibold text-gray-900">
            Stripe Onboarding Incomplete
          </h2>

          <p className="mt-2 text-sm text-gray-600">
            You exited the Stripe Connect onboarding process before completing it.
            You'll need to complete the onboarding to publish your competitions and receive payments.
          </p>

          {error && (
            <div className="mt-4 bg-red-50 border-l-4 border-red-400 p-4">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          <div className="mt-6 space-y-3">
            <button
              onClick={handleTryAgain}
              disabled={loading}
              className="w-full bg-indigo-600 text-white px-4 py-2 rounded-md font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Loading...' : 'Try Again'}
            </button>

            <button
              onClick={() => navigate('/competitions')}
              className="w-full bg-white text-gray-700 border border-gray-300 px-4 py-2 rounded-md font-medium hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              Return to Competitions
            </button>
          </div>

          <p className="mt-6 text-xs text-gray-500">
            Need help? Contact support at support@leaguehq.com
          </p>
        </div>
      </div>
    </div>
  );
}
