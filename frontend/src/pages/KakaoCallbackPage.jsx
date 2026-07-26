import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function KakaoCallbackPage() {
  const [searchParams] = useSearchParams();
  const { loginWithKakaoCode } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const requested = useRef(false);

  useEffect(() => {
    const code = searchParams.get('code');
    if (!code || requested.current) return;
    requested.current = true;

    loginWithKakaoCode(code)
      .then(() => navigate('/', { replace: true }))
      .catch(() => setError('카카오 로그인에 실패했습니다. 다시 시도해주세요.'));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  return (
    <div className="mx-auto max-w-sm px-4 py-16 text-center">
      {error ? (
        <>
          <p className="text-red-500">{error}</p>
          <button
            type="button"
            onClick={() => navigate('/login')}
            className="mt-4 text-sm text-indigo-600 underline"
          >
            로그인 화면으로 돌아가기
          </button>
        </>
      ) : (
        <p className="text-slate-500">카카오 로그인 처리 중...</p>
      )}
    </div>
  );
}
