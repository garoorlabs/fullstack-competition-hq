import type { CompetitionStatus } from '../../types';

interface StatusBadgeProps {
  status: CompetitionStatus;
}

const statusConfig: Record<CompetitionStatus, { label: string; className: string }> = {
  DRAFT: {
    label: 'Draft',
    className: 'bg-gray-100 text-gray-800',
  },
  PUBLISHED: {
    label: 'Published',
    className: 'bg-blue-100 text-blue-800',
  },
  ACTIVE: {
    label: 'Active',
    className: 'bg-green-100 text-green-800',
  },
  COMPLETED: {
    label: 'Completed',
    className: 'bg-gray-100 text-gray-800',
  },
  CANCELLED: {
    label: 'Cancelled',
    className: 'bg-red-100 text-red-800',
  },
};

export default function StatusBadge({ status }: StatusBadgeProps) {
  const config = statusConfig[status];

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}
