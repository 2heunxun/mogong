import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../auth/hooks/useAuth';

const ONBOARDING_PATH = '/onboarding';

export default function OnboardingGuard({ children }) {
  const { user, isAuthenticated, initializing } = useAuth();
  const location = useLocation();

  if (initializing) {
    return children;
  }

  const isOnboardingRoute = location.pathname === ONBOARDING_PATH;

  if (isAuthenticated && user && !user.profileCompleted && !isOnboardingRoute) {
    return <Navigate to={ONBOARDING_PATH} replace />;
  }

  if (isAuthenticated && user && user.profileCompleted && isOnboardingRoute) {
    return <Navigate to="/" replace />;
  }

  return children;
}
