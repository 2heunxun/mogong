import { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { BrowserRouter, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import Navbar from './shared/components/Navbar';
import IntroScreen from './shared/components/IntroScreen';
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
import ProfileEditPage from './features/user/pages/ProfileEditPage';

const INTRO_STORAGE_KEY = 'mogong-intro-shown';

function AppContent() {
  const location = useLocation();
  const navigate = useNavigate();
  const [showIntro, setShowIntro] = useState(() => !sessionStorage.getItem(INTRO_STORAGE_KEY));

  const handleIntroFinish = () => {
    sessionStorage.setItem(INTRO_STORAGE_KEY, '1');
    setShowIntro(false);
    if (location.pathname !== '/') {
      navigate('/', { replace: true });
    }
  };

  return (
    <>
      <AnimatePresence>{showIntro && <IntroScreen onFinish={handleIntroFinish} />}</AnimatePresence>

      <Navbar />
      <OnboardingGuard>
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.25, ease: 'easeOut' }}
          >
            <Routes location={location}>
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
              path="/profile/edit"
              element={
                <ProtectedRoute>
                  <ProfileEditPage />
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
          </motion.div>
        </AnimatePresence>
      </OnboardingGuard>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}
