import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { ProtectedRoute } from './components/ProtectedRoute';

import { CatalogPage } from './pages/CatalogPage';
import { VehicleDetailPage } from './pages/VehicleDetailPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { MyAppointmentsPage } from './pages/MyAppointmentsPage';
import { OperatorDashboardPage } from './pages/OperatorDashboardPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="flex flex-col min-h-screen">
          <Navbar />
          <main className="flex-1">
            <Routes>
              {/* Rotas Públicas */}
              <Route path="/" element={<CatalogPage />} />
              <Route path="/veiculos/:id" element={<VehicleDetailPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/registro" element={<RegisterPage />} />

              {/* Rotas Protegidas - Cliente */}
              <Route
                path="/meus-agendamentos"
                element={
                  <ProtectedRoute allowedRoles={['CUSTOMER']}>
                    <MyAppointmentsPage />
                  </ProtectedRoute>
                }
              />

              {/* Rotas Protegidas - Operador / Admin */}
              <Route
                path="/operador"
                element={
                  <ProtectedRoute allowedRoles={['OPERATOR', 'ADMIN']}>
                    <OperatorDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Rotas Protegidas - Administrador */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;

