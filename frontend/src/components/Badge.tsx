import React from 'react';
import { VehicleStatus, TestDriveStatus } from '../types';

interface StatusBadgeProps {
  status: VehicleStatus | TestDriveStatus | string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  let label = status;
  let colorClasses = 'bg-slate-100 text-slate-700 border-slate-200';

  switch (status) {
    // Vehicle Status
    case 'AVAILABLE':
      label = 'Disponível';
      colorClasses = 'bg-emerald-50 text-emerald-700 border-emerald-200';
      break;
    case 'IN_TEST_DRIVE':
      label = 'Em Test-Drive';
      colorClasses = 'bg-amber-50 text-amber-700 border-amber-200';
      break;
    case 'MAINTENANCE':
      label = 'Em Manutenção';
      colorClasses = 'bg-rose-50 text-rose-700 border-rose-200';
      break;
    case 'RESERVED':
      label = 'Reservado';
      colorClasses = 'bg-blue-50 text-blue-700 border-blue-200';
      break;

    // Test Drive Status
    case 'PENDING':
      label = 'Pendente';
      colorClasses = 'bg-amber-50 text-amber-700 border-amber-200';
      break;
    case 'APPROVED':
      label = 'Aprovado';
      colorClasses = 'bg-emerald-50 text-emerald-700 border-emerald-200';
      break;
    case 'REJECTED':
      label = 'Rejeitado';
      colorClasses = 'bg-rose-50 text-rose-700 border-rose-200';
      break;
    case 'COMPLETED':
      label = 'Concluído';
      colorClasses = 'bg-slate-100 text-slate-700 border-slate-200';
      break;
    case 'CANCELED':
      label = 'Cancelado';
      colorClasses = 'bg-rose-50 text-rose-700 border-rose-200';
      break;

    default:
      label = status;
  }

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${colorClasses}`}>
      {label}
    </span>
  );
};

