import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  approveDinnerPartyJoinRequest,
  deleteDinnerParty,
  fetchDinnerPartyDetail,
  fetchDinnerPartyJoinRequests,
  fetchDinnerPartyMembers,
  joinDinnerParty,
  leaveDinnerParty,
  rejectDinnerPartyJoinRequest,
} from '../api/dinnerParties';
import { useAuth } from '../../auth/hooks/useAuth';

export default function DinnerPartyDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, initializing } = useAuth();

  const [party, setParty] = useState(null);
  const [members, setMembers] = useState([]);
  const [joinRequests, setJoinRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const load = () => {
    setLoading(true);
    setError('');
    Promise.all([fetchDinnerPartyDetail(id), fetchDinnerPartyMembers(id)])
      .then(([detail, memberList]) => {
        setParty(detail);
        setMembers(memberList);
        if (detail.isOwner) {
          return fetchDinnerPartyJoinRequests(id).then(setJoinRequests);
        }
        setJoinRequests([]);
      })
      .catch(() => setError('저녁팟 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (initializing) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, initializing, isAuthenticated]);

  const handleJoin = async () => {
    setActionError('');
    setActionLoading(true);
    try {
      await joinDinnerParty(id);
      load();
    } catch (err) {
      setActionError(err.response?.data?.message ?? '참여 신청에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelRequest = async () => {
    setActionError('');
    setActionLoading(true);
    try {
      await leaveDinnerParty(id);
      load();
    } catch (err) {
      setActionError(err.response?.data?.message ?? '신청 취소에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleLeave = async () => {
    if (!window.confirm('저녁팟에서 탈퇴하시겠습니까?')) return;
    setActionError('');
    setActionLoading(true);
    try {
      await leaveDinnerParty(id);
      load();
    } catch (err) {
      setActionError(err.response?.data?.message ?? '탈퇴에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReapply = async () => {
    setActionError('');
    setActionLoading(true);
    try {
      await leaveDinnerParty(id);
      await joinDinnerParty(id);
      load();
    } catch (err) {
      setActionError(err.response?.data?.message ?? '재신청에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('저녁팟을 삭제하시겠습니까? 되돌릴 수 없습니다.')) return;
    setActionLoading(true);
    try {
      await deleteDinnerParty(id);
      navigate('/dinner-parties');
    } catch (err) {
      setActionError(err.response?.data?.message ?? '삭제에 실패했습니다.');
      setActionLoading(false);
    }
  };

  const handleApproveRequest = async (memberId) => {
    setActionError('');
    setActionLoading(true);
    try {
      await approveDinnerPartyJoinRequest(id, memberId);
      load();
    } catch (err) {
      setActionError(err.response?.data?.message ?? '수락에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectRequest = async (memberId) => {
    setActionError('');
    setActionLoading(true);
    try {
      await rejectDinnerPartyJoinRequest(id, memberId);
      load();
    } catch (err) {
      setActionError(err.response?.data?.message ?? '거절에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <p className="p-10 text-center text-slate-500">불러오는 중...</p>;
  if (error) return <p className="p-10 text-center text-red-500">{error}</p>;
  if (!party) return null;

  const canRequestJoin = isAuthenticated && !party.memberStatus && party.status === 'RECRUITING';

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <div className="mb-4 flex items-center justify-between">
        <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-600">
          {party.location}
        </span>
        <span
          className={`rounded-full px-3 py-1 text-xs font-medium ${
            party.status === 'RECRUITING' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'
          }`}
        >
          {party.status === 'RECRUITING' ? '모집중' : '마감'}
        </span>
      </div>

      <h1 className="mb-2 text-2xl font-bold text-slate-900">{party.title}</h1>
      <p className="mb-6 text-sm text-slate-500">
        파티장 {party.owner.nickname} · {party.memberCount} / {party.capacity}명 ·{' '}
        {new Date(party.meetingAt).toLocaleString('ko-KR', { dateStyle: 'medium', timeStyle: 'short' })}
      </p>

      <p className="mb-8 whitespace-pre-wrap text-slate-700">{party.description || '소개가 없습니다.'}</p>

      {actionError && <p className="mb-4 text-sm text-red-500">{actionError}</p>}

      <div className="mb-8 flex flex-wrap items-center gap-2">
        {party.memberStatus === 'APPROVED' && party.openChatUrl && (
          <a
            href={party.openChatUrl}
            target="_blank"
            rel="noreferrer"
            className="rounded-md bg-[#FEE500] px-4 py-2 text-sm font-semibold text-[#191600] hover:brightness-95"
          >
            오픈채팅 입장하기
          </a>
        )}

        {canRequestJoin && (
          <button
            type="button"
            onClick={handleJoin}
            disabled={actionLoading}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
          >
            참여 신청하기
          </button>
        )}

        {party.memberStatus === 'PENDING' && (
          <>
            <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-700">
              파티장 승인 대기중
            </span>
            <button
              type="button"
              onClick={handleCancelRequest}
              disabled={actionLoading}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50"
            >
              신청 취소
            </button>
          </>
        )}

        {party.memberStatus === 'REJECTED' && (
          <>
            <span className="rounded-full bg-red-100 px-3 py-1 text-xs font-medium text-red-600">
              참여 신청이 거절되었습니다
            </span>
            <button
              type="button"
              onClick={handleReapply}
              disabled={actionLoading}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50"
            >
              다시 신청하기
            </button>
          </>
        )}

        {party.memberStatus === 'APPROVED' && !party.isOwner && (
          <button
            type="button"
            onClick={handleLeave}
            disabled={actionLoading}
            className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50"
          >
            탈퇴하기
          </button>
        )}

        {party.isOwner && (
          <>
            <Link
              to={`/dinner-parties/${party.id}/edit`}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50"
            >
              수정
            </Link>
            <button
              type="button"
              onClick={handleDelete}
              disabled={actionLoading}
              className="rounded-md border border-red-200 px-4 py-2 text-sm text-red-500 hover:bg-red-50 disabled:opacity-50"
            >
              삭제
            </button>
          </>
        )}

        {!isAuthenticated && (
          <Link
            to="/login"
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            로그인 후 참여하기
          </Link>
        )}
      </div>

      {party.isOwner && (
        <div className="mb-8">
          <h2 className="mb-3 text-lg font-semibold text-slate-900">참여 신청 관리 ({joinRequests.length})</h2>
          {joinRequests.length === 0 ? (
            <p className="rounded-md border border-dashed border-slate-300 px-4 py-6 text-center text-sm text-slate-400">
              대기중인 참여 신청이 없습니다.
            </p>
          ) : (
            <ul className="flex flex-col gap-2">
              {joinRequests.map((r) => (
                <li
                  key={r.id}
                  className="flex items-center justify-between rounded-md border border-slate-200 bg-white px-4 py-2 text-sm"
                >
                  <span className="text-slate-700">{r.nickname}</span>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-slate-400">
                      {new Date(r.requestedAt).toLocaleDateString('ko-KR')} 신청
                    </span>
                    <button
                      type="button"
                      onClick={() => handleApproveRequest(r.id)}
                      disabled={actionLoading}
                      className="rounded-md bg-indigo-600 px-3 py-1 text-xs font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
                    >
                      수락
                    </button>
                    <button
                      type="button"
                      onClick={() => handleRejectRequest(r.id)}
                      disabled={actionLoading}
                      className="rounded-md border border-slate-300 px-3 py-1 text-xs text-slate-600 hover:bg-slate-50 disabled:opacity-50"
                    >
                      거절
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div>
        <h2 className="mb-3 text-lg font-semibold text-slate-900">파티원 ({members.length})</h2>
        <ul className="flex flex-col gap-2">
          {members.map((m) => (
            <li
              key={m.userId}
              className="flex items-center justify-between rounded-md border border-slate-200 bg-white px-4 py-2 text-sm"
            >
              <span className="text-slate-700">
                {m.nickname} {m.isOwner && <span className="text-xs text-indigo-500">(파티장)</span>}
              </span>
              <span className="text-xs text-slate-400">
                {new Date(m.joinedAt).toLocaleDateString('ko-KR')} 참여
              </span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
