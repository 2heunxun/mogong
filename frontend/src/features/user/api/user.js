import { api } from '../../../shared/api/client';

export const completeOnboarding = (payload) =>
  api.patch('/api/users/me/onboarding', payload).then((res) => res.data);
