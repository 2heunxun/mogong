import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import { AuthProvider } from './hooks/useAuth';
import KakaoCallbackPage from './pages/KakaoCallbackPage';
import LoginPage from './pages/LoginPage';
import PartyDetailPage from './pages/PartyDetailPage';
import PartyFormPage from './pages/PartyFormPage';
import PartyListPage from './pages/PartyListPage';

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
