import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Header from '../components/layout/Header';
import Spinner from '../components/common/Spinner';

export default function TeamRegistrationSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const sessionId = searchParams.get('session_id');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate processing time
    const timer = setTimeout(() => {
      setLoading(false);
    }, 2000);

    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
          <div className="bg-white shadow rounded-lg p-8 text-center">
            <Spinner />
            <p className="mt-4 text-gray-600">Processing your registration...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg p-8">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
              <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>

            <h2 className="mt-6 text-3xl font-bold text-gray-900">
              Registration Successful!
            </h2>

            <p className="mt-4 text-lg text-gray-600">
              Your team has been registered successfully.
            </p>

            <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
              <p className="text-sm text-blue-800">
                <strong>What's next?</strong>
              </p>
              <ul className="mt-2 text-sm text-blue-700 text-left list-disc list-inside">
                <li>Check your email for payment confirmation</li>
                <li>Your monthly subscription will begin billing in 30 days</li>
                <li>Start building your roster and preparing for the competition</li>
              </ul>
            </div>

            {sessionId && (
              <p className="mt-4 text-xs text-gray-500">
                Session ID: {sessionId}
              </p>
            )}

            <div className="mt-8 flex justify-center space-x-4">
              <button
                onClick={() => navigate('/competitions')}
                className="px-6 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                View Competitions
              </button>
              <button
                onClick={() => navigate('/')}
                className="px-6 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                Go to Dashboard
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
