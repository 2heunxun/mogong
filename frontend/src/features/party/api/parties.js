import { api } from '../../../shared/api/client';

export const fetchParties = (params) => api.get('/api/parties', { params }).then((res) => res.data);

export const fetchPartyDetail = (id) => api.get(`/api/parties/${id}`).then((res) => res.data);

export const fetchPartyMembers = (id) => api.get(`/api/parties/${id}/members`).then((res) => res.data);

export const createParty = (payload) => api.post('/api/parties', payload).then((res) => res.data);

export const updateParty = (id, payload) => api.put(`/api/parties/${id}`, payload).then((res) => res.data);

export const deleteParty = (id) => api.delete(`/api/parties/${id}`);

export const joinParty = (id) => api.post(`/api/parties/${id}/join`);

export const leaveParty = (id) => api.delete(`/api/parties/${id}/leave`);

export const fetchMyParties = () => api.get('/api/parties/mine').then((res) => res.data);

export const fetchJoinRequests = (id) => api.get(`/api/parties/${id}/join-requests`).then((res) => res.data);

export const approveJoinRequest = (id, memberId) => api.post(`/api/parties/${id}/join-requests/${memberId}/approve`);

export const rejectJoinRequest = (id, memberId) => api.post(`/api/parties/${id}/join-requests/${memberId}/reject`);
