import React from 'react';
import { Car, MapPin, Phone, ShieldCheck } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-white border-t border-slate-200 mt-20 text-slate-600 text-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand Info */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-slate-900 flex items-center justify-center text-white">
                <Car className="w-4 h-4" />
              </div>
              <span className="text-lg font-bold text-slate-900">AutoElite</span>
            </div>
            <p className="text-slate-500 max-w-md leading-relaxed">
              Plataforma monorepo corporativa de vitrine de veículos e agendamento de test-drives.
              Atendimento exclusivo, veículos certificados e garantia de procedência.
            </p>
            <div className="flex items-center gap-2 text-xs text-slate-400">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>Ambiente 100% auditado com autenticação JWT e controle de acesso RBAC.</span>
            </div>
          </div>

          {/* Filiais */}
          <div>
            <h4 className="font-semibold text-slate-900 mb-3 flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-slate-500" />
              Nossas Unidades
            </h4>
            <ul className="space-y-2 text-slate-500 text-xs">
              <li>
                <strong className="text-slate-700 block">Matriz São Paulo</strong>
                Av. das Nações Unidas, 14401 - SP
              </li>
              <li>
                <strong className="text-slate-700 block">Filial Curitiba</strong>
                Av. Sete de Setembro, 5500 - PR
              </li>
            </ul>
          </div>

          {/* Atendimento */}
          <div>
            <h4 className="font-semibold text-slate-900 mb-3 flex items-center gap-1.5">
              <Phone className="w-4 h-4 text-slate-500" />
              Horário de Test-Drive
            </h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              Segunda a Sábado: 08:00 às 17:30
              <br />
              Domingos e Feriados: Fechado
            </p>
            <div className="mt-4 pt-4 border-t border-slate-100">
              <span className="text-xs text-slate-400">Suporte: contato@concessionaria.com</span>
            </div>
          </div>
        </div>

        <div className="mt-12 pt-8 border-t border-slate-100 text-center text-xs text-slate-400">
          <p>© {new Date().getFullYear()} AutoElite Concessionária de Veículos. Todos os direitos reservados.</p>
        </div>
      </div>
    </footer>
  );
};

