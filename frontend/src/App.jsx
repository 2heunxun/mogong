import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Navbar from './shared/components/Navbar';
import ProtectedRoute from './features/auth/components/ProtectedRoute';
import { AuthProvider } from './features/auth/hooks/useAuth';
import KakaoCallbackPage from './features/auth/pages/KakaoCallbackPage';
import LoginPage from './features/auth/pages/LoginPage';
import MyPartiesPage from './features/party/pages/MyPartiesPage';
import PartyDetailPage from './features/party/pages/PartyDetailPage';
import PartyFormPage from './features/party/pages/PartyFormPage';
import PartyListPage from './features/party/pages/PartyListPage';
import MyDinnerPartiesPage from './features/dinnerparty/pages/MyDinnerPartiesPage';
import DinnerPartyDetailPage from './features/dinnerparty/pages/DinnerPartyDetailPage';
import DinnerPartyFormPage from './features/dinnerparty/pages/DinnerPartyFormPage';
import DinnerPartyListPage from './features/dinnerparty/pages/DinnerPartyListPage';
import MyWeekendPartiesPage from './features/weekendparty/pages/MyWeekendPartiesPage';
import WeekendPartyDetailPage from './features/weekendparty/pages/WeekendPartyDetailPage';
import WeekendPartyFormPage from './features/weekendparty/pages/WeekendPartyFormPage';
import WeekendPartyListPage from './features/weekendparty/pages/WeekendPartyListPage';
import OnboardingGuard from './features/user/components/OnboardingGuard';
import OnboardingPage from './features/user/pages/OnboardingPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <OnboardingGuard>
          <Routes>
            <Route path="/" element={<PartyListPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />
            <Route
              path="/onboarding"
              element={
                <ProtectedRoute>
                  <OnboardingPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-parties"
              element={
                <ProtectedRoute>
                  <MyPartiesPage />
                </ProtectedRoute>
              }
            />
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
            <Route path="/dinner-parties" element={<DinnerPartyListPage />} />
            <Route
              path="/my-dinner-parties"
              element={
                <ProtectedRoute>
                  <MyDinnerPartiesPage />
                </ProtectedRoute>
              }
            />
            <Route path="/dinner-parties/:id" element={<DinnerPartyDetailPage />} />
            <Route
              path="/dinner-parties/new"
              element={
                <ProtectedRoute>
                  <DinnerPartyFormPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dinner-parties/:id/edit"
              element={
                <ProtectedRoute>
                  <DinnerPartyFormPage />
                </ProtectedRoute>
              }
            />
            <Route path="/weekend-parties" element={<WeekendPartyListPage />} />
            <Route
              path="/my-weekend-parties"
              element={
                <ProtectedRoute>
                  <MyWeekendPartiesPage />
                </ProtectedRoute>
              }
            />
            <Route path="/weekend-parties/:id" element={<WeekendPartyDetailPage />} />
            <Route
              path="/weekend-parties/new"
              element={
                <ProtectedRoute>
                  <WeekendPartyFormPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/weekend-parties/:id/edit"
              element={
                <ProtectedRoute>
                  <WeekendPartyFormPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </OnboardingGuard>
      </AuthProvider>
    </BrowserRouter>
  );
}
