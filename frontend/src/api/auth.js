import { api } from './client';

export const fetchKakaoLoginUrl = () => api.get('/api/auth/kakao/login-url').then((res) => res.data);

export const fetchMe = () => api.get('/api/users/me').then((res) => res.data);
