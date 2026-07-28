import { api } from '../../../shared/api/client';

export const fetchWeekendParties = (params) => api.get('/api/weekend-parties', { params }).then((res) => res.data);

export const fetchWeekendPartyDetail = (id) => api.get(`/api/weekend-parties/${id}`).then((res) => res.data);

export const fetchWeekendPartyMembers = (id) => api.get(`/api/weekend-parties/${id}/members`).then((res) => res.data);

export const createWeekendParty = (payload) => api.post('/api/weekend-parties', payload).then((res) => res.data);

export const updateWeekendParty = (id, payload) => api.put(`/api/weekend-parties/${id}`, payload).then((res) => res.data);

export const deleteWeekendParty = (id) => api.delete(`/api/weekend-parties/${id}`);

export const joinWeekendParty = (id) => api.post(`/api/weekend-parties/${id}/join`);

export const leaveWeekendParty = (id) => api.delete(`/api/weekend-parties/${id}/leave`);

export const fetchMyWeekendParties = () => api.get('/api/weekend-parties/mine').then((res) => res.data);

export const fetchWeekendPartyJoinRequests = (id) => api.get(`/api/weekend-parties/${id}/join-requests`).then((res) => res.data);

export const approveWeekendPartyJoinRequest = (id, memberId) =>
  api.post(`/api/weekend-parties/${id}/join-requests/${memberId}/approve`);

export const rejectWeekendPartyJoinRequest = (id, memberId) =>
  api.post(`/api/weekend-parties/${id}/join-requests/${memberId}/reject`);
