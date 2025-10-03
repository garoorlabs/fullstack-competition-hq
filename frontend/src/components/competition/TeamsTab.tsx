import type { Team } from '../../types';
import SubscriptionStatusBadge from '../common/SubscriptionStatusBadge';
import Spinner from '../common/Spinner';

interface TeamsTabProps {
  teams: Team[];
  loading: boolean;
}

export default function TeamsTab({ teams, loading }: TeamsTabProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner />
      </div>
    );
  }

  if (teams.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-lg border border-gray-200">
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
        <h3 className="text-lg font-medium text-gray-900 mb-2">No teams registered yet</h3>
        <p className="text-gray-500">Teams will appear here once coaches register</p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow overflow-hidden sm:rounded-md">
      <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
        <h3 className="text-lg leading-6 font-medium text-gray-900">
          Registered Teams ({teams.length})
        </h3>
      </div>
      <ul className="divide-y divide-gray-200">
        {teams.map((team) => (
          <li key={team.id} className="px-4 py-4 sm:px-6 hover:bg-gray-50">
            <div className="flex items-center justify-between">
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <p className="text-lg font-semibold text-gray-900 truncate">
                    {team.name}
                  </p>
                  <div className="ml-4 flex-shrink-0">
                    <SubscriptionStatusBadge status={team.subscriptionStatus} />
                  </div>
                </div>
                <div className="mt-2 flex items-center text-sm text-gray-500">
                  <div className="flex-1">
                    <p className="font-medium text-gray-700">{team.coachName}</p>
                    <p>{team.coachEmail}</p>
                    <p className="mt-1">Registered: {formatDate(team.registeredAt)}</p>
                  </div>
                  <div className="ml-4 flex items-center space-x-6">
                    <div className="text-center">
                      <p className="text-2xl font-bold text-gray-900">{team.rosterSize}</p>
                      <p className="text-xs text-gray-500">Players</p>
                    </div>
                    {team.entryFeePaid && (
                      <div className="flex items-center text-green-600">
                        <svg className="h-5 w-5 mr-1" fill="currentColor" viewBox="0 0 20 20">
                          <path
                            fillRule="evenodd"
                            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                            clipRule="evenodd"
                          />
                        </svg>
                        <span className="text-sm font-medium">Entry Fee Paid</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
            {team.rosterLocked && (
              <div className="mt-2">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                  <svg className="-ml-0.5 mr-1.5 h-2 w-2 text-gray-400" fill="currentColor" viewBox="0 0 8 8">
                    <circle cx={4} cy={4} r={3} />
                  </svg>
                  Roster Locked
                </span>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
