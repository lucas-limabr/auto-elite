import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { TestDriveAppointment } from '../types';
import { StatusBadge } from '../components/Badge';
import { Calendar, Clock, MapPin, Star, AlertCircle, CheckCircle2, MessageSquare, X } from 'lucide-react';

export const MyAppointmentsPage: React.FC = () => {
  const [appointments, setAppointments] = useState<TestDriveAppointment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Feedback Modal State
  const [selectedAppointment, setSelectedAppointment] = useState<TestDriveAppointment | null>(null);
  const [rating, setRating] = useState<number>(5);
  const [comments, setComments] = useState<string>('');
  const [interestedInBuying, setInterestedInBuying] = useState<boolean>(true);
  const [submittingFeedback, setSubmittingFeedback] = useState<boolean>(false);

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const response = await api.get<TestDriveAppointment[]>('/test-drives/my-appointments');
      setAppointments(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar agendamentos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleCancel = async (id: number) => {
    if (!window.confirm('Tem certeza de que deseja cancelar este agendamento de test-drive?')) {
      return;
    }
    setError(null);
    setSuccess(null);
    try {
      await api.post(`/test-drives/${id}/cancel`);
      setSuccess('Agendamento cancelado com sucesso.');
      fetchAppointments();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Não foi possível cancelar o agendamento.');
    }
  };

  const handleFeedbackSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAppointment) return;
    setSubmittingFeedback(true);
    setError(null);
    try {
      await api.post(`/test-drives/${selectedAppointment.id}/feedback`, {
        rating,
        comments,
        interestedInBuying,
      });
      setSuccess('Avaliação registrada com sucesso! Muito obrigado pelo seu feedback.');
      setSelectedAppointment(null);
      fetchAppointments();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao submeter feedback');
    } finally {
      setSubmittingFeedback(false);
    }
  };

  const formatDateTime = (iso: string) => {
    const d = new Date(iso);
    return d.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <div className="border-b border-slate-200 pb-4">
        <h1 className="text-2xl font-bold text-slate-900">Meus Agendamentos de Test-Drive</h1>
        <p className="text-xs text-slate-500 mt-1">
          Acompanhe o status das suas solicitações, realize cancelamentos com antecedência ou avalie veículos testados.
        </p>
      </div>

      {success && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-lg text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>{success}</span>
          </div>
          <button onClick={() => setSuccess(null)} className="text-emerald-700 hover:text-emerald-900">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {error && (
        <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-lg text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
            <span>{error}</span>
          </div>
          <button onClick={() => setError(null)} className="text-rose-700 hover:text-rose-900">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : appointments.length === 0 ? (
        <div className="bg-white border border-slate-200 rounded-xl p-12 text-center space-y-3">
          <Calendar className="w-10 h-10 text-slate-300 mx-auto" />
          <h3 className="text-base font-semibold text-slate-800">Nenhum agendamento encontrado</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Você ainda não solicitou nenhum test-drive. Acesse nosso catálogo e escolha o modelo ideal para agendar uma experiência.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {appointments.map((item) => (
            <div
              key={item.id}
              className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4"
            >
              <div className="space-y-2">
                <div className="flex items-center gap-2.5">
                  <span className="text-sm font-bold text-slate-900">{item.vehicle.model}</span>
                  <StatusBadge status={item.status} />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1 text-xs text-slate-600">
                  <div className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span>Data: <strong>{formatDateTime(item.scheduledDateTime)}</strong></span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-slate-400" />
                    <span>Unidade: {item.vehicle.branch.name} ({item.vehicle.branch.city})</span>
                  </div>
                </div>

                {item.customerNotes && (
                  <p className="text-xs text-slate-500 italic bg-slate-50 p-2 rounded border border-slate-100">
                    "Obs: {item.customerNotes}"
                  </p>
                )}

                {item.operatorNotes && (
                  <p className="text-xs text-blue-700 bg-blue-50 p-2 rounded border border-blue-100">
                    <strong>Mensagem da Concessionária:</strong> {item.operatorNotes}
                  </p>
                )}

                {/* Feedback Exibição */}
                {item.feedback && (
                  <div className="mt-2 p-2.5 bg-emerald-50/60 border border-emerald-100 rounded-lg text-xs space-y-1">
                    <div className="flex items-center gap-1 text-amber-500 font-semibold">
                      {[...Array(item.feedback.rating)].map((_, i) => (
                        <Star key={i} className="w-3.5 h-3.5 fill-current" />
                      ))}
                      <span className="text-slate-700 ml-1">({item.feedback.rating}/5)</span>
                    </div>
                    {item.feedback.comments && <p className="text-slate-600">"{item.feedback.comments}"</p>}
                    <span className="text-[11px] text-emerald-700 font-medium block">
                      {item.feedback.interestedInBuying ? '✓ Cliente tem interesse em comprar' : 'Não demonstrou interesse de compra'}
                    </span>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-2 shrink-0 self-end md:self-center">
                {(item.status === 'PENDING' || item.status === 'APPROVED') && (
                  <button
                    onClick={() => handleCancel(item.id)}
                    className="px-3 py-1.5 border border-rose-200 text-rose-700 hover:bg-rose-50 text-xs font-semibold rounded-lg transition-colors"
                  >
                    Cancelar Agendamento
                  </button>
                )}

                {item.status === 'COMPLETED' && !item.feedback && (
                  <button
                    onClick={() => setSelectedAppointment(item)}
                    className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white text-xs font-semibold rounded-lg transition-colors flex items-center gap-1.5"
                  >
                    <MessageSquare className="w-3.5 h-3.5" />
                    Avaliar Test-Drive
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Feedback Modal */}
      {selectedAppointment && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-base">Avaliar Test-Drive</h3>
              <button onClick={() => setSelectedAppointment(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-500">
              Veículo: <strong>{selectedAppointment.vehicle.model}</strong>
            </p>

            <form onSubmit={handleFeedbackSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block font-medium text-slate-700 mb-1">Nota de 1 a 5 Estrelas *</label>
                <div className="flex gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setRating(star)}
                      className={`p-2 rounded-lg border flex items-center gap-1 font-bold ${
                        rating >= star ? 'border-amber-400 bg-amber-50 text-amber-700' : 'border-slate-200 text-slate-400'
                      }`}
                    >
                      <Star className={`w-4 h-4 ${rating >= star ? 'fill-current' : ''}`} />
                      {star}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block font-medium text-slate-700 mb-1">Comentários e Impressões</label>
                <textarea
                  rows={3}
                  value={comments}
                  onChange={(e) => setComments(e.target.value)}
                  placeholder="Conte o que achou da dirigibilidade, conforto, atendimento..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 resize-none"
                ></textarea>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="interested"
                  checked={interestedInBuying}
                  onChange={(e) => setInterestedInBuying(e.target.checked)}
                  className="rounded border-slate-300 text-brand-600 focus:ring-brand-500"
                />
                <label htmlFor="interested" className="text-slate-700 font-medium cursor-pointer">
                  Tenho interesse em adquirir este modelo futuramente
                </label>
              </div>

              <div className="flex gap-2 pt-2 justify-end">
                <button
                  type="button"
                  onClick={() => setSelectedAppointment(null)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg hover:bg-slate-50 font-medium"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={submittingFeedback}
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 font-semibold disabled:opacity-50"
                >
                  {submittingFeedback ? 'Enviando...' : 'Enviar Avaliação'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

