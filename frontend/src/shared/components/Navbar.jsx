import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/hooks/useAuth';
import logoMark from '../../assets/logo-mark.png';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Link to="/" className="flex items-center gap-2 text-xl font-bold text-indigo-600">
          <img src={logoMark} alt="모공 로고" className="h-8 w-8" />
          모공
        </Link>
        <nav className="flex items-center gap-3">
          <Link to="/" className="text-sm text-slate-600 hover:text-indigo-600">
            파티 목록
          </Link>
          {isAuthenticated ? (
            <>
              <Link to="/my-parties" className="text-sm text-slate-600 hover:text-indigo-600">
                내 파티
              </Link>
              <Link
                to="/parties/new"
                className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700"
              >
                파티 만들기
              </Link>
              <span className="text-sm text-slate-500">{user.nickname}님</span>
              <button
                type="button"
                onClick={handleLogout}
                className="text-sm text-slate-500 hover:text-red-500"
              >
                로그아웃
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700"
            >
              로그인
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
