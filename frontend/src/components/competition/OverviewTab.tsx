import type { Competition } from '../../types';

interface OverviewTabProps {
  competition: Competition;
  isOwner: boolean;
}

export default function OverviewTab({ competition, isOwner }: OverviewTabProps) {
  return (
    <div className="space-y-6">
      {/* Description */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">Description</h3>
        <p className="text-gray-600">{competition.description}</p>
      </div>

      {/* Venue */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">Venue</h3>
        {competition.venues.map((venue) => (
          <div key={venue.id} className="text-gray-600">
            <p className="font-medium">{venue.name}</p>
            <p>{venue.address}</p>
          </div>
        ))}
      </div>

      {/* Share Link (Owner Only) */}
      {isOwner && competition.shareToken && (
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Share Link</h3>
          <div className="flex items-center space-x-2">
            <input
              type="text"
              readOnly
              value={`${window.location.origin}/join/${competition.shareToken}`}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-sm text-gray-600"
            />
            <button
              onClick={() => {
                navigator.clipboard.writeText(`${window.location.origin}/join/${competition.shareToken}`);
              }}
              className="px-4 py-2 bg-gray-100 text-gray-700 border border-gray-300 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 text-sm font-medium"
            >
              Copy
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
