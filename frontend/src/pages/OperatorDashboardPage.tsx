import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Vehicle, TestDriveAppointment, Brand, Category, Branch, TestDriveStatus, VehicleStatus } from '../types';
import { StatusBadge } from '../components/Badge';
import {
  Calendar,
  Car,
  CheckCircle,
  XCircle,
  Plus,
  Trash2,
  Edit,
  AlertCircle,
  Clock,
  User,
  Phone,
  FileText,
  X,
} from 'lucide-react';

export const OperatorDashboardPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'appointments' | 'vehicles'>('appointments');

  // Appointments State
  const [appointments, setAppointments] = useState<TestDriveAppointment[]>([]);
  const [appointmentFilter, setAppointmentFilter] = useState<string>('');
  const [selectedAppointment, setSelectedAppointment] = useState<TestDriveAppointment | null>(null);
  const [targetStatus, setTargetStatus] = useState<TestDriveStatus | null>(null);
  const [operatorNotes, setOperatorNotes] = useState<string>('');

  // Vehicles State
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [branches, setBranches] = useState<Branch[]>([]);

  // Vehicle Modal State
  const [showVehicleModal, setShowVehicleModal] = useState<boolean>(false);
  const [editingVehicleId, setEditingVehicleId] = useState<number | null>(null);
  const [vehicleForm, setVehicleForm] = useState({
    model: '',
    yearManufacture: 2024,
    yearModel: 2024,
    price: '',
    mileage: '',
    color: '',
    fuelType: 'FLEX',
    transmission: 'AUTOMATIC',
    chassisNumber: '',
    status: 'AVAILABLE' as VehicleStatus,
    brandId: '',
    categoryId: '',
    branchId: '',
    imageUrls: '',
  });

  const [loading, setLoading] = useState<boolean>(true);
  const [msgError, setMsgError] = useState<string | null>(null);
  const [msgSuccess, setMsgSuccess] = useState<string | null>(null);

  const fetchAppointments = async () => {
    try {
      const url = appointmentFilter ? `/test-drives?status=${appointmentFilter}` : '/test-drives';
      const res = await api.get<TestDriveAppointment[]>(url);
      setAppointments(res.data);
    } catch (err: any) {
      console.error(err);
    }
  };

  const fetchVehiclesAndMeta = async () => {
    try {
      const [vRes, bRes, cRes, brRes] = await Promise.all([
        api.get<Vehicle[]>('/vehicles'),
        api.get<Brand[]>('/brands'),
        api.get<Category[]>('/categories'),
        api.get<Branch[]>('/branches'),
      ]);
      setVehicles(vRes.data);
      setBrands(bRes.data);
      setCategories(cRes.data);
      setBranches(brRes.data);
    } catch (err: any) {
      console.error(err);
    }
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchAppointments(), fetchVehiclesAndMeta()]).finally(() => setLoading(false));
  }, [appointmentFilter]);

  // Transição de Status de Agendamento
  const handleTransitionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAppointment || !targetStatus) return;
    setMsgError(null);
    setMsgSuccess(null);

    try {
      await api.patch(`/test-drives/${selectedAppointment.id}/status`, {
        newStatus: targetStatus,
        operatorNotes,
      });
      setMsgSuccess(`Agendamento #${selectedAppointment.id} atualizado para ${targetStatus}.`);
      setSelectedAppointment(null);
      setTargetStatus(null);
      setOperatorNotes('');
      fetchAppointments();
      fetchVehiclesAndMeta();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Falha ao atualizar status do agendamento.');
    }
  };

  // Salvar / Editar Veículo
  const handleSaveVehicle = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsgError(null);
    setMsgSuccess(null);

    const payload = {
      model: vehicleForm.model,
      yearManufacture: Number(vehicleForm.yearManufacture),
      yearModel: Number(vehicleForm.yearModel),
      price: Number(vehicleForm.price),
      mileage: Number(vehicleForm.mileage),
      color: vehicleForm.color,
      fuelType: vehicleForm.fuelType,
      transmission: vehicleForm.transmission,
      chassisNumber: vehicleForm.chassisNumber,
      status: vehicleForm.status,
      brandId: Number(vehicleForm.brandId),
      categoryId: Number(vehicleForm.categoryId),
      branchId: Number(vehicleForm.branchId),
      imageUrls: vehicleForm.imageUrls
        ? vehicleForm.imageUrls.split('\n').map((u) => u.trim()).filter(Boolean)
        : [],
    };

    try {
      if (editingVehicleId) {
        await api.put(`/vehicles/${editingVehicleId}`, payload);
        setMsgSuccess('Veículo atualizado com sucesso!');
      } else {
        await api.post('/vehicles', payload);
        setMsgSuccess('Veículo cadastrado com sucesso!');
      }
      setShowVehicleModal(false);
      setEditingVehicleId(null);
      fetchVehiclesAndMeta();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Erro ao salvar veículo');
    }
  };

  const handleEditVehicle = (v: Vehicle) => {
    setEditingVehicleId(v.id);
    setVehicleForm({
      model: v.model,
      yearManufacture: v.yearManufacture,
      yearModel: v.yearModel,
      price: String(v.price),
      mileage: String(v.mileage),
      color: v.color,
      fuelType: v.fuelType,
      transmission: v.transmission,
      chassisNumber: v.chassisNumber,
      status: v.status,
      brandId: String(v.brand.id),
      categoryId: String(v.category.id),
      branchId: String(v.branch.id),
      imageUrls: v.images.map((img) => img.url).join('\n'),
    });
    setShowVehicleModal(true);
  };

  const handleDeleteVehicle = async (id: number) => {
    if (!window.confirm('Tem certeza de que deseja excluir este veículo do catálogo?')) return;
    setMsgError(null);
    setMsgSuccess(null);

    try {
      await api.delete(`/vehicles/${id}`);
      setMsgSuccess('Veículo excluído com sucesso.');
      fetchVehiclesAndMeta();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Não foi possível excluir o veículo.');
    }
  };

  const handleQuickStatusChange = async (vehicleId: number, newStatus: VehicleStatus) => {
    const v = vehicles.find((item) => item.id === vehicleId);
    if (!v) return;

    try {
      await api.put(`/vehicles/${vehicleId}`, {
        model: v.model,
        yearManufacture: v.yearManufacture,
        yearModel: v.yearModel,
        price: v.price,
        mileage: v.mileage,
        color: v.color,
        fuelType: v.fuelType,
        transmission: v.transmission,
        chassisNumber: v.chassisNumber,
        status: newStatus,
        brandId: v.brand.id,
        categoryId: v.category.id,
        branchId: v.branch.id,
      });
      fetchVehiclesAndMeta();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Erro ao alterar status do veículo');
    }
  };

  const formatPrice = (p: number) =>
    new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 }).format(p);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="border-b border-slate-200 pb-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Painel do Operador</h1>
          <p className="text-xs text-slate-500 mt-1">
            Gestão de agendamentos de test-drive e controle de inventário de veículos da concessionária.
          </p>
        </div>

        {/* Tab Navigation */}
        <div className="flex bg-slate-100 p-1 rounded-lg border border-slate-200 text-xs font-semibold">
          <button
            onClick={() => setActiveTab('appointments')}
            className={`px-4 py-2 rounded-md transition-all flex items-center gap-2 ${
              activeTab === 'appointments' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Calendar className="w-3.5 h-3.5" />
            Fila de Test-Drives ({appointments.length})
          </button>
          <button
            onClick={() => setActiveTab('vehicles')}
            className={`px-4 py-2 rounded-md transition-all flex items-center gap-2 ${
              activeTab === 'vehicles' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Car className="w-3.5 h-3.5" />
            Catálogo de Veículos ({vehicles.length})
          </button>
        </div>
      </div>

      {msgSuccess && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-lg text-xs flex items-center justify-between">
          <span>{msgSuccess}</span>
          <button onClick={() => setMsgSuccess(null)}><X className="w-4 h-4" /></button>
        </div>
      )}

      {msgError && (
        <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-lg text-xs flex items-center justify-between">
          <span>{msgError}</span>
          <button onClick={() => setMsgError(null)}><X className="w-4 h-4" /></button>
        </div>
      )}

      {/* TAB 1: APPOINTMENTS */}
      {activeTab === 'appointments' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700">Filtrar por Status:</span>
            <select
              value={appointmentFilter}
              onChange={(e) => setAppointmentFilter(e.target.value)}
              className="px-3 py-1.5 border border-slate-200 rounded-lg text-xs bg-white focus:outline-none"
            >
              <option value="">Todos os Status</option>
              <option value="PENDING">Pendentes</option>
              <option value="APPROVED">Aprovados</option>
              <option value="COMPLETED">Concluídos</option>
              <option value="REJECTED">Rejeitados</option>
              <option value="CANCELED">Cancelados</option>
            </select>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                  <th className="py-3 px-4">ID</th>
                  <th className="py-3 px-4">Cliente & Contato</th>
                  <th className="py-3 px-4">Veículo</th>
                  <th className="py-3 px-4">Data e Hora</th>
                  <th className="py-3 px-4">Filial</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Ações do Operador</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {appointments.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-50/50">
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-500">#{a.id}</td>
                    <td className="py-3.5 px-4">
                      <strong className="text-slate-900 block">{a.customer.name}</strong>
                      <span className="text-slate-500 text-[11px] block">{a.customer.email} • {a.customer.phone}</span>
                      <span className="text-[10px] text-slate-400">CNH: {a.customer.cnhNumber} (Val: {a.customer.cnhExpirationDate})</span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="font-semibold text-slate-800 block">{a.vehicle.model}</span>
                      <span className="text-[11px] text-slate-500">{a.vehicle.brand.name} • {a.vehicle.category.name}</span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="font-medium text-slate-800">
                        {new Date(a.scheduledDateTime).toLocaleString('pt-BR', {
                          day: '2-digit',
                          month: '2-digit',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-600">{a.vehicle.branch.name}</td>
                    <td className="py-3.5 px-4"><StatusBadge status={a.status} /></td>
                    <td className="py-3.5 px-4 text-right space-x-1.5">
                      {a.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => { setSelectedAppointment(a); setTargetStatus('APPROVED'); }}
                            className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-[11px] font-semibold transition-colors"
                          >
                            Aprovar
                          </button>
                          <button
                            onClick={() => { setSelectedAppointment(a); setTargetStatus('REJECTED'); }}
                            className="px-2.5 py-1 bg-rose-600 hover:bg-rose-700 text-white rounded text-[11px] font-semibold transition-colors"
                          >
                            Rejeitar
                          </button>
                        </>
                      )}

                      {a.status === 'APPROVED' && (
                        <>
                          <button
                            onClick={() => { setSelectedAppointment(a); setTargetStatus('COMPLETED'); }}
                            className="px-2.5 py-1 bg-slate-900 hover:bg-slate-800 text-white rounded text-[11px] font-semibold transition-colors"
                          >
                            Concluir Test-Drive
                          </button>
                          <button
                            onClick={() => { setSelectedAppointment(a); setTargetStatus('CANCELED'); }}
                            className="px-2.5 py-1 border border-slate-200 text-slate-700 hover:bg-slate-100 rounded text-[11px] font-semibold transition-colors"
                          >
                            Cancelar
                          </button>
                        </>
                      )}

                      {(a.status === 'COMPLETED' || a.status === 'REJECTED' || a.status === 'CANCELED') && (
                        <span className="text-[11px] text-slate-400 italic">Finalizado</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: VEHICLES */}
      {activeTab === 'vehicles' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700">Inventário de Veículos da Concessionária</span>
            <button
              onClick={() => {
                setEditingVehicleId(null);
                setVehicleForm({
                  model: '',
                  yearManufacture: 2024,
                  yearModel: 2024,
                  price: '',
                  mileage: '',
                  color: '',
                  fuelType: 'FLEX',
                  transmission: 'AUTOMATIC',
                  chassisNumber: '',
                  status: 'AVAILABLE',
                  brandId: brands[0]?.id ? String(brands[0].id) : '',
                  categoryId: categories[0]?.id ? String(categories[0].id) : '',
                  branchId: branches[0]?.id ? String(branches[0].id) : '',
                  imageUrls: '',
                });
                setShowVehicleModal(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition-colors shadow-sm"
            >
              <Plus className="w-3.5 h-3.5" />
              Novo Veículo
            </button>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                  <th className="py-3 px-4">Veículo & Modelo</th>
                  <th className="py-3 px-4">Marca / Cat</th>
                  <th className="py-3 px-4">Ano / Km</th>
                  <th className="py-3 px-4">Preço</th>
                  <th className="py-3 px-4">Unidade</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {vehicles.map((v) => (
                  <tr key={v.id} className="hover:bg-slate-50/50">
                    <td className="py-3.5 px-4">
                      <strong className="text-slate-900 block">{v.model}</strong>
                      <span className="text-[11px] text-slate-400 font-mono">Chassi: {v.chassisNumber}</span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-700">
                      {v.brand.name} • {v.category.name}
                    </td>
                    <td className="py-3.5 px-4 text-slate-600">
                      {v.yearManufacture}/{v.yearModel} • {v.mileage.toLocaleString('pt-BR')} km
                    </td>
                    <td className="py-3.5 px-4 font-bold text-slate-900">{formatPrice(v.price)}</td>
                    <td className="py-3.5 px-4 text-slate-600">{v.branch.name}</td>
                    <td className="py-3.5 px-4">
                      <select
                        value={v.status}
                        onChange={(e) => handleQuickStatusChange(v.id, e.target.value as VehicleStatus)}
                        className="text-[11px] font-medium border border-slate-200 rounded px-2 py-0.5 bg-white focus:outline-none"
                      >
                        <option value="AVAILABLE">AVAILABLE (Disponível)</option>
                        <option value="IN_TEST_DRIVE">IN_TEST_DRIVE</option>
                        <option value="MAINTENANCE">MAINTENANCE</option>
                        <option value="RESERVED">RESERVED</option>
                      </select>
                    </td>
                    <td className="py-3.5 px-4 text-right space-x-2">
                      <button
                        onClick={() => handleEditVehicle(v)}
                        title="Editar veículo"
                        className="p-1.5 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded transition-colors"
                      >
                        <Edit className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteVehicle(v.id)}
                        title="Excluir veículo"
                        className="p-1.5 text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* MODAL TRANSIÇÃO DE STATUS TEST DRIVE */}
      {selectedAppointment && targetStatus && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl space-y-4 text-xs">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-sm">
                Confirmar Transição: #{selectedAppointment.id} → {targetStatus}
              </h3>
              <button onClick={() => { setSelectedAppointment(null); setTargetStatus(null); }}>
                <X className="w-4 h-4 text-slate-400 hover:text-slate-600" />
              </button>
            </div>

            <p className="text-slate-600">
              Cliente: <strong>{selectedAppointment.customer.name}</strong>
              <br />
              Veículo: <strong>{selectedAppointment.vehicle.model}</strong>
            </p>

            <form onSubmit={handleTransitionSubmit} className="space-y-4">
              <div>
                <label className="block font-medium text-slate-700 mb-1">
                  Observações do Operador (opcional)
                </label>
                <textarea
                  rows={3}
                  value={operatorNotes}
                  onChange={(e) => setOperatorNotes(e.target.value)}
                  placeholder="Instruções para o cliente ou motivo da decisão..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 resize-none"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => { setSelectedAppointment(null); setTargetStatus(null); }}
                  className="px-3 py-2 border border-slate-200 rounded-lg hover:bg-slate-50"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 font-semibold"
                >
                  Confirmar {targetStatus}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL VEÍCULO (CRIAR / EDITAR) */}
      {showVehicleModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-2xl w-full p-6 shadow-xl space-y-4 text-xs my-8">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-sm">
                {editingVehicleId ? 'Editar Veículo' : 'Cadastrar Novo Veículo'}
              </h3>
              <button onClick={() => setShowVehicleModal(false)}>
                <X className="w-4 h-4 text-slate-400 hover:text-slate-600" />
              </button>
            </div>

            <form onSubmit={handleSaveVehicle} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="block font-medium text-slate-700 mb-1">Modelo do Veículo *</label>
                  <input
                    type="text"
                    required
                    value={vehicleForm.model}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, model: e.target.value })}
                    placeholder="Ex: BMW 320i M Sport"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Marca *</label>
                  <select
                    required
                    value={vehicleForm.brandId}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, brandId: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="">Selecione</option>
                    {brands.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                  </select>
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Categoria *</label>
                  <select
                    required
                    value={vehicleForm.categoryId}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, categoryId: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="">Selecione</option>
                    {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Filial *</label>
                  <select
                    required
                    value={vehicleForm.branchId}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, branchId: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="">Selecione</option>
                    {branches.map((br) => <option key={br.id} value={br.id}>{br.name}</option>)}
                  </select>
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Preço (R$) *</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={vehicleForm.price}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, price: e.target.value })}
                    placeholder="Ex: 250000"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Quilometragem (km) *</label>
                  <input
                    type="number"
                    required
                    value={vehicleForm.mileage}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, mileage: e.target.value })}
                    placeholder="Ex: 15000"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Cor *</label>
                  <input
                    type="text"
                    required
                    value={vehicleForm.color}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, color: e.target.value })}
                    placeholder="Ex: Preto Perolizado"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Ano Fabricação *</label>
                  <input
                    type="number"
                    required
                    value={vehicleForm.yearManufacture}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, yearManufacture: Number(e.target.value) })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Ano Modelo *</label>
                  <input
                    type="number"
                    required
                    value={vehicleForm.yearModel}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, yearModel: Number(e.target.value) })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Combustível *</label>
                  <select
                    value={vehicleForm.fuelType}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, fuelType: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="FLEX">FLEX</option>
                    <option value="GASOLINE">GASOLINA</option>
                    <option value="DIESEL">DIESEL</option>
                    <option value="HYBRID">HÍBRIDO</option>
                    <option value="ELECTRIC">ELÉTRICO</option>
                  </select>
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Câmbio *</label>
                  <select
                    value={vehicleForm.transmission}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, transmission: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="AUTOMATIC">AUTOMÁTICO</option>
                    <option value="MANUAL">MANUAL</option>
                    <option value="CVT">CVT</option>
                    <option value="AUTOMATED">AUTOMATIZADO</option>
                  </select>
                </div>

                <div className="sm:col-span-2">
                  <label className="block font-medium text-slate-700 mb-1">Número do Chassi *</label>
                  <input
                    type="text"
                    required
                    value={vehicleForm.chassisNumber}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, chassisNumber: e.target.value })}
                    placeholder="Ex: 9BW320IMSPORT20241"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg font-mono uppercase"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block font-medium text-slate-700 mb-1">URLs das Fotos (uma por linha)</label>
                  <textarea
                    rows={3}
                    value={vehicleForm.imageUrls}
                    onChange={(e) => setVehicleForm({ ...vehicleForm, imageUrls: e.target.value })}
                    placeholder="https://images.unsplash.com/photo-..."
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg resize-none font-mono text-[11px]"
                  ></textarea>
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowVehicleModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-medium"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 font-semibold"
                >
                  {editingVehicleId ? 'Salvar Alterações' : 'Cadastrar Veículo'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

