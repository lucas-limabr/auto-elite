import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import { Vehicle, VehicleEligibility } from '../types';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from '../components/Badge';
import {
  Calendar,
  Clock,
  Gauge,
  Fuel,
  Cog,
  MapPin,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  FileText,
  Percent,
  Check,
} from 'lucide-react';

export const VehicleDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated, isCustomer } = useAuth();

  const [vehicle, setVehicle] = useState<Vehicle | null>(null);
  const [eligibility, setEligibility] = useState<VehicleEligibility | null>(null);
  const [selectedImage, setSelectedImage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);

  // Form de Agendamento
  const [scheduledDateTime, setScheduledDateTime] = useState<string>('');
  const [customerNotes, setCustomerNotes] = useState<string>('');
  const [scheduling, setScheduling] = useState<boolean>(false);
  const [scheduleSuccess, setScheduleSuccess] = useState<string | null>(null);
  const [scheduleError, setScheduleError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [vRes, eRes] = await Promise.all([
          api.get<Vehicle>(`/vehicles/${id}`),
          api.get<VehicleEligibility>(`/vehicles/${id}/eligibility`),
        ]);
        setVehicle(vRes.data);
        setEligibility(eRes.data);
        const initialImg =
          vRes.data.images.find((img) => img.isPrimary)?.url ||
          vRes.data.images[0]?.url ||
          'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=1200&q=80';
        setSelectedImage(initialImg);
      } catch (err) {
        console.error('Erro ao buscar detalhes do veículo', err);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchData();
    }
  }, [id]);

  const handleScheduleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setScheduleError(null);
    setScheduleSuccess(null);
    setScheduling(true);

    try {
      await api.post('/test-drives/schedule', {
        vehicleId: vehicle?.id,
        scheduledDateTime,
        customerNotes,
      });
      setScheduleSuccess('Agendamento solicitado com sucesso! A equipe entrará em contato para confirmação.');
      setScheduledDateTime('');
      setCustomerNotes('');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Falha ao agendar test-drive. Verifique as restrições.';
      setScheduleError(msg);
    } finally {
      setScheduling(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[50vh] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!vehicle) {
    return (
      <div className="max-w-md mx-auto my-20 p-8 bg-white border border-slate-200 rounded-xl text-center">
        <h2 className="text-lg font-bold text-slate-800">Veículo não encontrado</h2>
        <Link to="/" className="mt-4 inline-block text-sm font-semibold text-brand-600 hover:underline">
          Voltar ao Catálogo
        </Link>
      </div>
    );
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(price);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-10">
      {/* Breadcrumb Clean */}
      <nav className="text-xs text-slate-500 flex items-center gap-2">
        <Link to="/" className="hover:text-slate-800">Catálogo</Link>
        <span>/</span>
        <span>{vehicle.brand.name}</span>
        <span>/</span>
        <span className="text-slate-900 font-medium">{vehicle.model}</span>
      </nav>

      {/* Main Details Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Gallery & Technical Data (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          {/* Main Photo */}
          <div className="aspect-[16/10] bg-slate-100 rounded-xl overflow-hidden border border-slate-200">
            <img src={selectedImage} alt={vehicle.model} className="w-full h-full object-cover" />
          </div>

          {/* Thumbnails */}
          {vehicle.images.length > 1 && (
            <div className="flex gap-3 overflow-x-auto pb-2">
              {vehicle.images.map((img) => (
                <button
                  key={img.id}
                  onClick={() => setSelectedImage(img.url)}
                  className={`w-20 h-14 rounded-lg overflow-hidden border-2 shrink-0 transition-all ${
                    selectedImage === img.url ? 'border-brand-600 ring-2 ring-brand-500/20' : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <img src={img.url} alt="Miniatura" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}

          {/* Technical Specifications */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="text-base font-semibold text-slate-900 border-b border-slate-100 pb-3">
              Ficha Técnica
            </h3>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-xs">
              <div>
                <span className="text-slate-400 block">Ano de Fabricação</span>
                <span className="font-semibold text-slate-800">{vehicle.yearManufacture}</span>
              </div>
              <div>
                <span className="text-slate-400 block">Ano do Modelo</span>
                <span className="font-semibold text-slate-800">{vehicle.yearModel}</span>
              </div>
              <div>
                <span className="text-slate-400 block">Quilometragem</span>
                <span className="font-semibold text-slate-800">{vehicle.mileage.toLocaleString('pt-BR')} km</span>
              </div>
              <div>
                <span className="text-slate-400 block">Combustível</span>
                <span className="font-semibold text-slate-800 capitalize">{vehicle.fuelType.toLowerCase()}</span>
              </div>
              <div>
                <span className="text-slate-400 block">Câmbio</span>
                <span className="font-semibold text-slate-800 capitalize">{vehicle.transmission.toLowerCase()}</span>
              </div>
              <div>
                <span className="text-slate-400 block">Cor Externa</span>
                <span className="font-semibold text-slate-800">{vehicle.color}</span>
              </div>
              <div className="col-span-2">
                <span className="text-slate-400 block">Número do Chassi</span>
                <span className="font-mono text-slate-700">{vehicle.chassisNumber}</span>
              </div>
            </div>
          </div>

          {/* Features / Opcionais */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="text-base font-semibold text-slate-900 border-b border-slate-100 pb-3">
              Opcionais e Equipamentos ({vehicle.features.length})
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {vehicle.features.map((feature) => (
                <div key={feature.id} className="flex items-start gap-2 text-xs">
                  <Check className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                  <div>
                    <span className="font-medium text-slate-800 block">{feature.name}</span>
                    {feature.description && <span className="text-slate-500 text-[11px]">{feature.description}</span>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Pricing, Scheduling & Eligibility Widget (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          {/* Header Card */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{vehicle.brand.name} • {vehicle.category.name}</span>
              <StatusBadge status={vehicle.status} />
            </div>

            <h1 className="text-2xl font-bold text-slate-900">{vehicle.model}</h1>

            <div className="flex items-center gap-1.5 text-xs text-slate-500">
              <MapPin className="w-4 h-4 text-slate-400" />
              <span>{vehicle.branch.name} ({vehicle.branch.city} - {vehicle.branch.state})</span>
            </div>

            <div className="pt-4 border-t border-slate-100">
              <span className="text-xs text-slate-400 font-medium block">Preço à vista</span>
              <span className="text-3xl font-extrabold text-slate-900">{formatPrice(vehicle.price)}</span>
            </div>
          </div>

          {/* Eligibility & Depreciation Analysis Widget */}
          {eligibility && (
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
              <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                <div className="flex items-center gap-2">
                  <ShieldCheck className="w-5 h-5 text-brand-600" />
                  <h3 className="text-sm font-bold text-slate-900">Classificação Comercial</h3>
                </div>
                <span className="text-xs font-semibold px-2 py-0.5 rounded bg-brand-50 text-brand-800">
                  {eligibility.classification}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-3 text-xs pt-1">
                <div className="p-2.5 bg-slate-50 rounded-lg">
                  <span className="text-slate-400 block text-[11px]">Taxa de Depreciação</span>
                  <span className="font-bold text-slate-800 text-sm">{(eligibility.depreciationRate * 100).toFixed(0)}%</span>
                </div>
                <div className="p-2.5 bg-slate-50 rounded-lg">
                  <span className="text-slate-400 block text-[11px]">Seguro Diário Estimado</span>
                  <span className="font-bold text-slate-800 text-sm">R$ {eligibility.estimatedInsurancePerDay.toFixed(2)}</span>
                </div>
              </div>

              <div className="text-xs space-y-1.5 pt-2">
                <span className="font-medium text-slate-700 block">Critérios Avaliados:</span>
                <ul className="space-y-1 text-slate-500 text-[11px]">
                  {eligibility.criteriaEvaluated.map((crit, idx) => (
                    <li key={idx} className="flex items-center gap-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-slate-400"></span>
                      {crit}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}

          {/* Test Drive Scheduling Form */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="text-base font-semibold text-slate-900 flex items-center gap-2">
              <Calendar className="w-4 h-4 text-brand-600" />
              Agendar Test-Drive
            </h3>

            {vehicle.status !== 'AVAILABLE' ? (
              <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800 flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>
                  Este veículo está com status <strong>{vehicle.status}</strong> e não está disponível para novos agendamentos no momento.
                </span>
              </div>
            ) : !isAuthenticated ? (
              <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg text-center space-y-3">
                <p className="text-xs text-slate-600">
                  Para solicitar o agendamento de test-drive é necessário ter uma conta cadastrada com CNH válida.
                </p>
                <div className="flex gap-2 justify-center">
                  <Link
                    to="/login"
                    className="px-4 py-2 text-xs font-semibold text-white bg-slate-900 hover:bg-slate-800 rounded-lg transition-colors"
                  >
                    Entrar
                  </Link>
                  <Link
                    to="/registro"
                    className="px-4 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-lg transition-colors"
                  >
                    Criar Conta
                  </Link>
                </div>
              </div>
            ) : !isCustomer ? (
              <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800">
                Você está conectado com perfil de <strong>Operador/Administrador</strong>. Agendamentos de test-drive na vitrine são exclusivos para contas de clientes.
              </div>
            ) : (
              <form onSubmit={handleScheduleSubmit} className="space-y-4 text-xs">
                {scheduleSuccess && (
                  <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-lg flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                    <span>{scheduleSuccess}</span>
                  </div>
                )}

                {scheduleError && (
                  <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-lg flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
                    <span>{scheduleError}</span>
                  </div>
                )}

                <div>
                  <label className="block font-medium text-slate-700 mb-1">
                    Data e Hora do Test-Drive *
                  </label>
                  <input
                    type="datetime-local"
                    required
                    value={scheduledDateTime}
                    onChange={(e) => setScheduledDateTime(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
                  />
                  <span className="text-[10px] text-slate-400 block mt-1">
                    * Mínimo 2 horas e máximo 30 dias de antecedência. Segunda a Sábado, 08:00 às 17:30.
                  </span>
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">
                    Observações ou Preferências (opcional)
                  </label>
                  <textarea
                    rows={3}
                    placeholder="Ex: Gostaria de testar na rodovia ou verificar espaço para cadeirinha..."
                    value={customerNotes}
                    onChange={(e) => setCustomerNotes(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 resize-none"
                  ></textarea>
                </div>

                <button
                  type="submit"
                  disabled={scheduling}
                  className="w-full py-2.5 px-4 bg-slate-900 hover:bg-slate-800 text-white font-semibold rounded-lg transition-colors flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
                >
                  {scheduling ? (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    'Confirmar Solicitação de Agendamento'
                  )}
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

