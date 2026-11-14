import React from 'react';
import { Link } from 'react-router-dom';
import { Vehicle } from '../types';
import { StatusBadge } from './Badge';
import { Gauge, Fuel, Cog, MapPin } from 'lucide-react';

interface VehicleCardProps {
  vehicle: Vehicle;
}

export const VehicleCard: React.FC<VehicleCardProps> = ({ vehicle }) => {
  const primaryImage =
    vehicle.images.find((img) => img.isPrimary)?.url ||
    vehicle.images[0]?.url ||
    'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80';

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      maximumFractionDigits: 0,
    }).format(price);
  };

  const formatMileage = (km: number) => {
    return `${km.toLocaleString('pt-BR')} km`;
  };

  const translateFuel = (fuel: string) => {
    const map: Record<string, string> = {
      GASOLINE: 'Gasolina',
      ETHANOL: 'Etanol',
      FLEX: 'Flex',
      DIESEL: 'Diesel',
      ELECTRIC: 'Elétrico',
      HYBRID: 'Híbrido',
    };
    return map[fuel] || fuel;
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 overflow-hidden flex flex-col hover:border-slate-300 hover:shadow-sm transition-all duration-200 group">
      {/* Image Container */}
      <div className="relative aspect-[16/10] bg-slate-100 overflow-hidden">
        <img
          src={primaryImage}
          alt={vehicle.model}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          loading="lazy"
        />
        <div className="absolute top-3 left-3 flex gap-1.5">
          <span className="bg-slate-900/80 backdrop-blur-sm text-white px-2 py-0.5 rounded text-xs font-semibold uppercase tracking-wider">
            {vehicle.brand.name}
          </span>
          <StatusBadge status={vehicle.status} />
        </div>
      </div>

      {/* Content */}
      <div className="p-5 flex-1 flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
            <span>{vehicle.category.name}</span>
            <span className="flex items-center gap-1">
              <MapPin className="w-3.5 h-3.5 text-slate-400" />
              {vehicle.branch.city} - {vehicle.branch.state}
            </span>
          </div>

          <h3 className="font-semibold text-slate-900 text-base line-clamp-1 group-hover:text-brand-700 transition-colors">
            {vehicle.model}
          </h3>

          <p className="text-xs text-slate-500 mt-1">
            {vehicle.yearManufacture}/{vehicle.yearModel} • Cor {vehicle.color}
          </p>

          {/* Quick Specs Grid */}
          <div className="grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-slate-100 text-xs text-slate-600">
            <div className="flex items-center gap-1.5" title="Quilometragem">
              <Gauge className="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span className="truncate">{formatMileage(vehicle.mileage)}</span>
            </div>
            <div className="flex items-center gap-1.5" title="Combustível">
              <Fuel className="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span className="truncate">{translateFuel(vehicle.fuelType)}</span>
            </div>
            <div className="flex items-center gap-1.5" title="Câmbio">
              <Cog className="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span className="truncate capitalize">{vehicle.transmission.toLowerCase()}</span>
            </div>
          </div>
        </div>

        {/* Price & Action */}
        <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between">
          <div>
            <span className="text-[11px] uppercase tracking-wider text-slate-400 font-medium block">Preço à vista</span>
            <span className="text-lg font-bold text-slate-900">{formatPrice(vehicle.price)}</span>
          </div>

          <Link
            to={`/veiculos/${vehicle.id}`}
            className="px-3.5 py-1.5 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-lg transition-colors"
          >
            Ver Detalhes
          </Link>
        </div>
      </div>
    </div>
  );
};

