import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, initializing } = useAuth();
  const location = useLocation();

  if (initializing) {
    return <div className="p-10 text-center text-slate-500">로딩 중...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}
