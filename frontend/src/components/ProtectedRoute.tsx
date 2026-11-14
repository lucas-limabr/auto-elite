import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Role } from '../types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: Role[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-[50vh] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-brand-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return (
      <div className="max-w-md mx-auto my-20 p-6 bg-white border border-rose-200 rounded-xl text-center">
        <h2 className="text-lg font-bold text-rose-700 mb-2">Acesso Não Autorizado</h2>
        <p className="text-sm text-slate-600 mb-4">
          Seu perfil de usuário ({user.role}) não possui permissão para acessar esta seção.
        </p>
        <Navigate to="/" replace />
      </div>
    );
  }

  return <>{children}</>;
};

