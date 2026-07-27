import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Navbar from './shared/components/Navbar';
import ProtectedRoute from './features/auth/components/ProtectedRoute';
import { AuthProvider } from './features/auth/hooks/useAuth';
import KakaoCallbackPage from './features/auth/pages/KakaoCallbackPage';
import LoginPage from './features/auth/pages/LoginPage';
import PartyDetailPage from './features/party/pages/PartyDetailPage';
import PartyFormPage from './features/party/pages/PartyFormPage';
import PartyListPage from './features/party/pages/PartyListPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <Routes>
          <Route path="/" element={<PartyListPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />
          <Route path="/parties/:id" element={<PartyDetailPage />} />
          <Route
            path="/parties/new"
            element={
              <ProtectedRoute>
                <PartyFormPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/parties/:id/edit"
            element={
              <ProtectedRoute>
                <PartyFormPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
