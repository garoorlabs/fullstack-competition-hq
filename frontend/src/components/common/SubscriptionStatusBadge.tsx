import type { SubscriptionStatus } from '../../types';

interface SubscriptionStatusBadgeProps {
  status: SubscriptionStatus | null;
}

const statusConfig: Record<SubscriptionStatus, { label: string; className: string }> = {
  ACTIVE: {
    label: 'Active',
    className: 'bg-green-100 text-green-800',
  },
  PAST_DUE: {
    label: 'Past Due',
    className: 'bg-yellow-100 text-yellow-800',
  },
  CANCELED: {
    label: 'Canceled',
    className: 'bg-gray-100 text-gray-800',
  },
  UNPAID: {
    label: 'Unpaid',
    className: 'bg-red-100 text-red-800',
  },
};

export default function SubscriptionStatusBadge({ status }: SubscriptionStatusBadgeProps) {
  if (!status) {
    return (
      <span className="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium bg-gray-100 text-gray-800">
        Pending
      </span>
    );
  }

  const config = statusConfig[status];

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}
