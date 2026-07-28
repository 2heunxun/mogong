import { api } from '../../../shared/api/client';

export const fetchDinnerParties = (params) => api.get('/api/dinner-parties', { params }).then((res) => res.data);

export const fetchDinnerPartyDetail = (id) => api.get(`/api/dinner-parties/${id}`).then((res) => res.data);

export const fetchDinnerPartyMembers = (id) => api.get(`/api/dinner-parties/${id}/members`).then((res) => res.data);

export const createDinnerParty = (payload) => api.post('/api/dinner-parties', payload).then((res) => res.data);

export const updateDinnerParty = (id, payload) => api.put(`/api/dinner-parties/${id}`, payload).then((res) => res.data);

export const deleteDinnerParty = (id) => api.delete(`/api/dinner-parties/${id}`);

export const joinDinnerParty = (id) => api.post(`/api/dinner-parties/${id}/join`);

export const leaveDinnerParty = (id) => api.delete(`/api/dinner-parties/${id}/leave`);

export const fetchMyDinnerParties = () => api.get('/api/dinner-parties/mine').then((res) => res.data);

export const fetchDinnerPartyJoinRequests = (id) => api.get(`/api/dinner-parties/${id}/join-requests`).then((res) => res.data);

export const approveDinnerPartyJoinRequest = (id, memberId) =>
  api.post(`/api/dinner-parties/${id}/join-requests/${memberId}/approve`);

export const rejectDinnerPartyJoinRequest = (id, memberId) =>
  api.post(`/api/dinner-parties/${id}/join-requests/${memberId}/reject`);
