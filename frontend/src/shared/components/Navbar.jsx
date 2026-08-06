import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/hooks/useAuth';
import logoMark from '../../assets/logo-mark.png';

const SECTIONS = {
  study: { tabLabel: '공부파티', tabTo: '/', myLabel: '내 파티', myTo: '/my-parties', newLabel: '파티 만들기', newTo: '/parties/new' },
  dinner: {
    tabLabel: '저녁팟',
    tabTo: '/dinner-parties',
    myLabel: '내 저녁팟',
    myTo: '/my-dinner-parties',
    newLabel: '저녁팟 만들기',
    newTo: '/dinner-parties/new',
  },
  weekend: {
    tabLabel: '주말팟',
    tabTo: '/weekend-parties',
    myLabel: '내 주말팟',
    myTo: '/my-weekend-parties',
    newLabel: '주말팟 만들기',
    newTo: '/weekend-parties/new',
  },
};

function getActiveSection(pathname) {
  if (pathname.startsWith('/dinner-parties') || pathname === '/my-dinner-parties') return 'dinner';
  if (pathname.startsWith('/weekend-parties') || pathname === '/my-weekend-parties') return 'weekend';
  return 'study';
}

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const activeSection = getActiveSection(location.pathname);
  const current = SECTIONS[activeSection];

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-4 gap-y-2 px-4 py-3 md:flex-nowrap md:justify-between">
        <Link to="/" className="flex items-center gap-2 text-xl font-bold text-indigo-600">
          <img src={logoMark} alt="모공 로고" className="h-8 w-8" />
          모공
        </Link>

        <nav className="order-3 flex w-full items-center gap-1 overflow-x-auto rounded-md bg-slate-100 p-1 md:order-none md:w-auto md:overflow-visible">
          {Object.entries(SECTIONS).map(([key, section]) => (
            <Link
              key={key}
              to={section.tabTo}
              className={`shrink-0 rounded-md px-3 py-1.5 text-sm font-medium ${
                activeSection === key ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-indigo-600'
              }`}
            >
              {section.tabLabel}
            </Link>
          ))}
        </nav>

        <nav className="ml-auto flex items-center gap-2 sm:gap-3 md:ml-0">
          {isAuthenticated ? (
            <>
              <Link to={current.myTo} className="hidden text-sm text-slate-600 hover:text-indigo-600 sm:inline">
                {current.myLabel}
              </Link>
              <Link
                to={current.newTo}
                className="rounded-md bg-indigo-600 px-2.5 py-1.5 text-xs font-medium text-white hover:bg-indigo-700 sm:px-3 sm:text-sm transition-transform duration-150 hover:scale-[1.02] active:scale-95"
              >
                {current.newLabel}
              </Link>
              <Link
                to="/profile/edit"
                className="max-w-[6rem] truncate text-sm text-slate-500 hover:text-indigo-600 sm:max-w-[10rem]"
                title={`${user.nickname}님 (클릭해서 정보 수정)`}
              >
                {user.nickname}님
              </Link>
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
              className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition-transform duration-150 hover:scale-[1.02] active:scale-95"
            >
              로그인
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
