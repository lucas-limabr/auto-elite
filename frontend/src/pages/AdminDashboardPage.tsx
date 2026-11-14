import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Operator, Branch, AuditLog } from '../types';
import {
  Users,
  MapPin,
  FileSpreadsheet,
  Plus,
  Shield,
  CheckCircle,
  XCircle,
  ToggleLeft,
  ToggleRight,
  Clock,
  X,
  Building,
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const AdminDashboardPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'operators' | 'branches' | 'audit'>('operators');

  const [operators, setOperators] = useState<Operator[]>([]);
  const [branches, setBranches] = useState<Branch[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [msgSuccess, setMsgSuccess] = useState<string | null>(null);
  const [msgError, setMsgError] = useState<string | null>(null);

  // Modal Novo Operador
  const [showOpModal, setShowOpModal] = useState<boolean>(false);
  const [opForm, setOpForm] = useState({
    name: '',
    email: '',
    password: '',
    registrationCode: '',
    hireDate: new Date().toISOString().split('T')[0],
    department: 'Vendas e Test-Drive',
    branchId: '',
  });

  // Modal Nova Filial
  const [showBranchModal, setShowBranchModal] = useState<boolean>(false);
  const [branchForm, setBranchForm] = useState({
    name: '',
    cnpj: '',
    city: '',
    state: '',
    address: '',
    maxDailyTestDrives: 10,
  });

  const fetchData = async () => {
    setLoading(true);
    try {
      const [opRes, brRes, logRes] = await Promise.all([
        api.get<Operator[]>('/operators'),
        api.get<Branch[]>('/branches'),
        api.get<AuditLog[]>('/audit'),
      ]);
      setOperators(opRes.data);
      setBranches(brRes.data);
      setAuditLogs(logRes.data);
      if (brRes.data.length > 0 && !opForm.branchId) {
        setOpForm((prev) => ({ ...prev, branchId: String(brRes.data[0].id) }));
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreateOperator = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsgError(null);
    setMsgSuccess(null);

    try {
      await api.post('/operators', {
        ...opForm,
        branchId: Number(opForm.branchId),
      });
      setMsgSuccess('Operador cadastrado com sucesso!');
      setShowOpModal(false);
      setOpForm({
        name: '',
        email: '',
        password: '',
        registrationCode: '',
        hireDate: new Date().toISOString().split('T')[0],
        department: 'Vendas e Test-Drive',
        branchId: branches[0]?.id ? String(branches[0].id) : '',
      });
      fetchData();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Erro ao cadastrar operador');
    }
  };

  const handleToggleOperatorStatus = async (id: number) => {
    setMsgError(null);
    setMsgSuccess(null);
    try {
      await api.patch(`/operators/${id}/toggle-status`);
      setMsgSuccess('Status do operador alterado.');
      fetchData();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Erro ao alterar status');
    }
  };

  const handleCreateBranch = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsgError(null);
    setMsgSuccess(null);

    try {
      await api.post('/branches', {
        ...branchForm,
        maxDailyTestDrives: Number(branchForm.maxDailyTestDrives),
      });
      setMsgSuccess('Filial cadastrada com sucesso!');
      setShowBranchModal(false);
      setBranchForm({
        name: '',
        cnpj: '',
        city: '',
        state: '',
        address: '',
        maxDailyTestDrives: 10,
      });
      fetchData();
    } catch (err: any) {
      setMsgError(err.response?.data?.message || 'Erro ao cadastrar filial');
    }
  };

  const formatTimestamp = (ts: string) => {
    const d = new Date(ts);
    return d.toLocaleString('pt-BR');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="border-b border-slate-200 pb-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <Shield className="w-5 h-5 text-brand-600" />
            <h1 className="text-2xl font-bold text-slate-900">Painel do Administrador</h1>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Gestão global de funcionários, filiais da concessionária e trilha de auditoria completa.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/operador"
            className="px-3.5 py-1.5 border border-slate-200 hover:bg-slate-50 text-slate-700 rounded-lg text-xs font-semibold transition-colors"
          >
            Acessar Fila de Test-Drive & Veículos →
          </Link>

          {/* Navigation Tabs */}
          <div className="flex bg-slate-100 p-1 rounded-lg border border-slate-200 text-xs font-semibold">
            <button
              onClick={() => setActiveTab('operators')}
              className={`px-3 py-1.5 rounded-md transition-all flex items-center gap-1.5 ${
                activeTab === 'operators' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Users className="w-3.5 h-3.5" />
              Operadores ({operators.length})
            </button>
            <button
              onClick={() => setActiveTab('branches')}
              className={`px-3 py-1.5 rounded-md transition-all flex items-center gap-1.5 ${
                activeTab === 'branches' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Building className="w-3.5 h-3.5" />
              Filiais ({branches.length})
            </button>
            <button
              onClick={() => setActiveTab('audit')}
              className={`px-3 py-1.5 rounded-md transition-all flex items-center gap-1.5 ${
                activeTab === 'audit' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <FileSpreadsheet className="w-3.5 h-3.5" />
              Auditoria ({auditLogs.length})
            </button>
          </div>
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

      {/* TAB 1: OPERADORES */}
      {activeTab === 'operators' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700">Equipe de Operadores Credenciados</span>
            <button
              onClick={() => setShowOpModal(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition-colors shadow-sm"
            >
              <Plus className="w-3.5 h-3.5" />
              Novo Operador
            </button>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                  <th className="py-3 px-4">Matrícula</th>
                  <th className="py-3 px-4">Nome & Email</th>
                  <th className="py-3 px-4">Departamento</th>
                  <th className="py-3 px-4">Filial Vinculada</th>
                  <th className="py-3 px-4">Contratação</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {operators.map((op) => (
                  <tr key={op.id} className="hover:bg-slate-50/50">
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-600">{op.registrationCode}</td>
                    <td className="py-3.5 px-4">
                      <strong className="text-slate-900 block">{op.name}</strong>
                      <span className="text-slate-500 text-[11px]">{op.email}</span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-600">{op.department}</td>
                    <td className="py-3.5 px-4 font-medium text-slate-800">{op.branchName}</td>
                    <td className="py-3.5 px-4 text-slate-600">{new Date(op.hireDate).toLocaleDateString('pt-BR')}</td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold border ${
                          op.active
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : 'bg-rose-50 text-rose-700 border-rose-200'
                        }`}
                      >
                        {op.active ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => handleToggleOperatorStatus(op.id)}
                        className={`px-3 py-1 rounded text-[11px] font-semibold border transition-colors ${
                          op.active
                            ? 'border-rose-200 text-rose-700 hover:bg-rose-50'
                            : 'border-emerald-200 text-emerald-700 hover:bg-emerald-50'
                        }`}
                      >
                        {op.active ? 'Desativar' : 'Ativar'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: FILIAIS */}
      {activeTab === 'branches' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700">Unidades e Filiais da Concessionária</span>
            <button
              onClick={() => setShowBranchModal(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition-colors shadow-sm"
            >
              <Plus className="w-3.5 h-3.5" />
              Nova Filial
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {branches.map((b) => (
              <div key={b.id} className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">{b.name}</h3>
                    <span className="text-xs font-mono text-slate-500">CNPJ: {b.cnpj}</span>
                  </div>
                  <span className="px-2.5 py-1 bg-brand-50 text-brand-700 font-semibold rounded text-xs">
                    Capacidade: {b.maxDailyTestDrives} testes/dia
                  </span>
                </div>

                <div className="text-xs text-slate-600 space-y-1 pt-2 border-t border-slate-100">
                  <div className="flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-slate-400" />
                    <span>{b.city} - {b.state}</span>
                  </div>
                  <p className="text-slate-500 pl-5">{b.address}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 3: AUDITORIA */}
      {activeTab === 'audit' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700">Trilha de Auditoria do Sistema ({auditLogs.length} eventos)</span>
            <span className="text-[11px] text-slate-400">Ordenado por eventos mais recentes</span>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                  <th className="py-3 px-4">Data e Hora</th>
                  <th className="py-3 px-4">Ação</th>
                  <th className="py-3 px-4">Entidade / ID</th>
                  <th className="py-3 px-4">Executado Por</th>
                  <th className="py-3 px-4">Detalhes</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {auditLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/50">
                    <td className="py-3 px-4 text-slate-500 font-mono text-[11px]">
                      {formatTimestamp(log.timestamp)}
                    </td>
                    <td className="py-3 px-4">
                      <span className="font-semibold text-slate-900 bg-slate-100 px-2 py-0.5 rounded text-[11px]">
                        {log.action}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-700">
                      {log.entityName} #{log.entityId}
                    </td>
                    <td className="py-3 px-4 text-slate-600 font-medium">
                      {log.performedByEmail}
                    </td>
                    <td className="py-3 px-4 text-slate-500 max-w-xs truncate" title={log.details}>
                      {log.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* MODAL NOVO OPERADOR */}
      {showOpModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl space-y-4 text-xs">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-sm">Cadastrar Novo Operador</h3>
              <button onClick={() => setShowOpModal(false)}>
                <X className="w-4 h-4 text-slate-400 hover:text-slate-600" />
              </button>
            </div>

            <form onSubmit={handleCreateOperator} className="space-y-3">
              <div>
                <label className="block font-medium text-slate-700 mb-1">Nome Completo *</label>
                <input
                  type="text"
                  required
                  value={opForm.name}
                  onChange={(e) => setOpForm({ ...opForm, name: e.target.value })}
                  placeholder="Ex: Roberto Silva"
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                />
              </div>

              <div>
                <label className="block font-medium text-slate-700 mb-1">Email Profissional *</label>
                <input
                  type="email"
                  required
                  value={opForm.email}
                  onChange={(e) => setOpForm({ ...opForm, email: e.target.value })}
                  placeholder="roberto@concessionaria.com"
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                />
              </div>

              <div>
                <label className="block font-medium text-slate-700 mb-1">Senha Provisória *</label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={opForm.password}
                  onChange={(e) => setOpForm({ ...opForm, password: e.target.value })}
                  placeholder="••••••••"
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-medium text-slate-700 mb-1">Matrícula *</label>
                  <input
                    type="text"
                    required
                    value={opForm.registrationCode}
                    onChange={(e) => setOpForm({ ...opForm, registrationCode: e.target.value })}
                    placeholder="OP-003"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Filial *</label>
                  <select
                    required
                    value={opForm.branchId}
                    onChange={(e) => setOpForm({ ...opForm, branchId: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                  </select>
                </div>
              </div>

              <div>
                <label className="block font-medium text-slate-700 mb-1">Departamento *</label>
                <input
                  type="text"
                  required
                  value={opForm.department}
                  onChange={(e) => setOpForm({ ...opForm, department: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowOpModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-medium"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 font-semibold"
                >
                  Cadastrar Operador
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL NOVA FILIAL */}
      {showBranchModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl space-y-4 text-xs">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-sm">Cadastrar Nova Filial</h3>
              <button onClick={() => setShowBranchModal(false)}>
                <X className="w-4 h-4 text-slate-400 hover:text-slate-600" />
              </button>
            </div>

            <form onSubmit={handleCreateBranch} className="space-y-3">
              <div>
                <label className="block font-medium text-slate-700 mb-1">Nome da Filial *</label>
                <input
                  type="text"
                  required
                  value={branchForm.name}
                  onChange={(e) => setBranchForm({ ...branchForm, name: e.target.value })}
                  placeholder="Ex: Concessionária Filial Campinas"
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-medium text-slate-700 mb-1">CNPJ *</label>
                  <input
                    type="text"
                    required
                    value={branchForm.cnpj}
                    onChange={(e) => setBranchForm({ ...branchForm, cnpj: e.target.value })}
                    placeholder="33.444.555/0001-66"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">Limite Test-Drives/Dia *</label>
                  <input
                    type="number"
                    required
                    min={1}
                    value={branchForm.maxDailyTestDrives}
                    onChange={(e) => setBranchForm({ ...branchForm, maxDailyTestDrives: Number(e.target.value) })}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="col-span-2">
                  <label className="block font-medium text-slate-700 mb-1">Cidade *</label>
                  <input
                    type="text"
                    required
                    value={branchForm.city}
                    onChange={(e) => setBranchForm({ ...branchForm, city: e.target.value })}
                    placeholder="Campinas"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                  />
                </div>

                <div>
                  <label className="block font-medium text-slate-700 mb-1">UF *</label>
                  <input
                    type="text"
                    required
                    maxLength={2}
                    value={branchForm.state}
                    onChange={(e) => setBranchForm({ ...branchForm, state: e.target.value.toUpperCase() })}
                    placeholder="SP"
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg uppercase"
                  />
                </div>
              </div>

              <div>
                <label className="block font-medium text-slate-700 mb-1">Endereço Completo *</label>
                <input
                  type="text"
                  required
                  value={branchForm.address}
                  onChange={(e) => setBranchForm({ ...branchForm, address: e.target.value })}
                  placeholder="Av. José de Souza Campos, 1200 - Cambuí"
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowBranchModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-medium"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 font-semibold"
                >
                  Cadastrar Filial
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

