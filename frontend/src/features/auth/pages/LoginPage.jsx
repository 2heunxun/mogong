import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { fetchKakaoLoginUrl } from '../api/auth';
import { useAuth } from '../hooks/useAuth';
import logoFull from '../../../assets/logo-full.png';

export default function LoginPage() {
  const { devLogin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname ?? '/';

  const [nickname, setNickname] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleKakaoLogin = async () => {
    setError('');
    try {
      const { loginUrl } = await fetchKakaoLoginUrl();
      window.location.href = loginUrl;
    } catch {
      setError('카카오 로그인 URL을 가져오지 못했습니다.');
    }
  };

  const handleDevLogin = async (e) => {
    e.preventDefault();
    if (!nickname.trim()) return;
    setError('');
    setLoading(true);
    try {
      await devLogin(nickname.trim());
      navigate(from, { replace: true });
    } catch (err) {
      setError(
        err.response?.data?.message ?? '개발용 로그인은 로컬 환경에서만 사용할 수 있습니다.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-sm flex-col gap-6 px-4 py-16">
      <div className="text-center">
        <img src={logoFull} alt="모공" className="mx-auto mb-2 h-32 w-auto" />
        <h1 className="text-2xl font-bold text-slate-900">모공에 로그인</h1>
        <p className="mt-1 text-sm text-slate-500">모아봐요 공부팀!</p>
      </div>

      <button
        type="button"
        onClick={handleKakaoLogin}
        className="flex items-center justify-center gap-2 rounded-md bg-[#FEE500] py-3 text-sm font-semibold text-[#191600] hover:brightness-95"
      >
        카카오로 시작하기
      </button>

      <div className="flex items-center gap-2 text-xs text-slate-400">
        <div className="h-px flex-1 bg-slate-200" />
        또는 로컬 개발용 로그인
        <div className="h-px flex-1 bg-slate-200" />
      </div>

      <form onSubmit={handleDevLogin} className="flex flex-col gap-2">
        <input
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="닉네임을 입력하세요"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={loading}
          className="rounded-md border border-slate-300 bg-white py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
        >
          {loading ? '로그인 중...' : '개발용 로그인'}
        </button>
      </form>

      {error && <p className="text-center text-sm text-red-500">{error}</p>}

      <p className="text-center text-xs text-slate-400">
        카카오 앱이 아직 등록되지 않았다면 개발용 로그인으로 전체 기능을 확인할 수 있습니다.
      </p>
    </div>
  );
}
