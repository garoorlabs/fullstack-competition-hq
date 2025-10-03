import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircleIcon } from '@heroicons/react/24/outline';
import { useAuth } from '../hooks/useAuth';
import { stripeApi } from '../services/api';
import Spinner from '../components/common/Spinner';

export default function StripeReturn() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { refreshUser } = useAuth();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const pollForPayoutStatus = async () => {
      const maxAttempts = 10; // Poll for up to 20 seconds (10 attempts x 2 seconds)
      let attempts = 0;

      const checkStatus = async (shouldRefreshStripe: boolean): Promise<boolean> => {
        try {
          // Only refresh from Stripe API on first attempt or every 3 attempts
          // This reduces API calls while still ensuring we get fresh data
          if (shouldRefreshStripe) {
            await stripeApi.refreshAccountStatus();
          }

          // Then get the updated user data
          const userData = await refreshUser();

          // Check if payout status is ENABLED
          if (userData?.payoutStatus === 'ENABLED') {
            return true;
          }

          return false;
        } catch (error) {
          console.error('Failed to refresh user data:', error);
          return false;
        }
      };

      // Poll until status is ENABLED or max attempts reached
      while (attempts < maxAttempts) {
        // Refresh from Stripe on first attempt, then every 3 attempts
        const shouldRefreshStripe = attempts === 0 || attempts % 3 === 0;
        const isEnabled = await checkStatus(shouldRefreshStripe);

        if (isEnabled) {
          setStatus('success');
          setMessage('Your Stripe account has been successfully connected!');

          // Redirect to competitions page after 2 seconds
          setTimeout(() => {
            navigate('/competitions');
          }, 2000);
          return;
        }

        attempts++;

        // Wait 2 seconds before next attempt
        if (attempts < maxAttempts) {
          await new Promise(resolve => setTimeout(resolve, 2000));
        }
      }

      // If we reach here, polling timed out
      setStatus('error');
      setMessage('Stripe connection is taking longer than expected. Your account may still be processing. Please refresh the page in a few moments.');
    };

    pollForPayoutStatus();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        {status === 'loading' && (
          <div className="text-center">
            <Spinner />
            <h2 className="mt-4 text-xl font-semibold text-gray-900">
              Processing your Stripe connection...
            </h2>
            <p className="mt-2 text-sm text-gray-500">
              Please wait while we update your account.
            </p>
          </div>
        )}

        {status === 'success' && (
          <div className="text-center">
            <div className="flex justify-center">
              <CheckCircleIcon className="h-16 w-16 text-green-600" />
            </div>
            <h2 className="mt-4 text-xl font-semibold text-gray-900">
              Success!
            </h2>
            <p className="mt-2 text-sm text-gray-600">{message}</p>
            <p className="mt-4 text-xs text-gray-500">
              Redirecting you to your competitions...
            </p>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <div className="flex justify-center">
              <div className="h-16 w-16 rounded-full bg-red-100 flex items-center justify-center">
                <span className="text-3xl text-red-600">✕</span>
              </div>
            </div>
            <h2 className="mt-4 text-xl font-semibold text-gray-900">
              Something went wrong
            </h2>
            <p className="mt-2 text-sm text-gray-600">{message}</p>
            <button
              onClick={() => navigate('/competitions')}
              className="mt-6 bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              Go to Competitions
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
