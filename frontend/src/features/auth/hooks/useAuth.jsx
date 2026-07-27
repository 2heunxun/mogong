import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api } from '../../../shared/api/client';
import { clearTokens, getRefreshToken, setAccessToken, setRefreshToken } from '../../../shared/api/tokenStore';
import { completeOnboarding as completeOnboardingRequest } from '../../user/api/user';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      const refreshToken = getRefreshToken();
      if (!refreshToken) {
        setInitializing(false);
        return;
      }
      try {
        const { data } = await api.post('/api/auth/refresh', { refreshToken });
        applyAuth(data);
      } catch {
        clearTokens();
      } finally {
        setInitializing(false);
      }
    };
    bootstrap();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const applyAuth = (data) => {
    setAccessToken(data.accessToken);
    setRefreshToken(data.refreshToken);
    setUser(data.user);
  };

  const loginWithKakaoCode = useCallback(async (code) => {
    const { data } = await api.post('/api/auth/kakao', { code });
    applyAuth(data);
    return data;
  }, []);

  const devLogin = useCallback(async (nickname) => {
    const { data } = await api.post('/api/auth/dev-login', { nickname });
    applyAuth(data);
    return data;
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post('/api/auth/logout');
    } catch {
      // 이미 만료된 토큰이어도 클라이언트 상태는 정리한다.
    }
    clearTokens();
    setUser(null);
  }, []);

  const completeOnboarding = useCallback(async (payload) => {
    const updatedUser = await completeOnboardingRequest(payload);
    setUser(updatedUser);
    return updatedUser;
  }, []);

  const value = {
    user,
    initializing,
    isAuthenticated: Boolean(user),
    loginWithKakaoCode,
    devLogin,
    logout,
    completeOnboarding,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.');
  }
  return ctx;
}
