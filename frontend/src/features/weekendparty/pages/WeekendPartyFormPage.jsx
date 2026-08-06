import { useEffect, useState } from 'react';
import LoadingSpinner from '../../../shared/components/LoadingSpinner';
import { useNavigate, useParams } from 'react-router-dom';
import { createWeekendParty, fetchWeekendPartyDetail, updateWeekendParty } from '../api/weekendParties';
import { CATEGORIES } from '../constants/categories';

const emptyForm = {
  title: '',
  description: '',
  category: CATEGORIES[0],
  meetingAt: '',
  capacity: 4,
  openChatUrl: '',
  recruitmentType: 'APPROVAL',
};

export default function WeekendPartyFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEdit) return;
    fetchWeekendPartyDetail(id)
      .then((party) => {
        if (!party.isOwner) {
          navigate(`/weekend-parties/${id}`, { replace: true });
          return;
        }
        setForm({
          title: party.title,
          description: party.description ?? '',
          category: party.category,
          meetingAt: party.meetingAt ? party.meetingAt.slice(0, 16) : '',
          capacity: party.capacity,
          openChatUrl: party.openChatUrl ?? '',
          recruitmentType: party.recruitmentType,
        });
      })
      .catch(() => setError('주말팟 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, [id, isEdit, navigate]);

  const handleChange = (field) => (e) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    const payload = { ...form, capacity: Number(form.capacity) };

    try {
      if (isEdit) {
        await updateWeekendParty(id, payload);
        navigate(`/weekend-parties/${id}`);
      } else {
        const { id: newId } = await createWeekendParty(payload);
        navigate(`/weekend-parties/${newId}`);
      }
    } catch (err) {
      const fieldErrors = err.response?.data?.fieldErrors;
      if (fieldErrors?.length) {
        setError(fieldErrors.map((f) => f.reason).join(' / '));
      } else {
        setError(err.response?.data?.message ?? '저장에 실패했습니다.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">
        {isEdit ? '주말팟 수정' : '주말팟 만들기'}
      </h1>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">제목</label>
          <input
            required
            maxLength={100}
            value={form.title}
            onChange={handleChange('title')}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">소개</label>
          <textarea
            rows={5}
            maxLength={2000}
            value={form.description}
            onChange={handleChange('description')}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>

        <div className="flex gap-4">
          <div className="flex-1">
            <label className="mb-1 block text-sm font-medium text-slate-700">카테고리</label>
            <select
              value={form.category}
              onChange={handleChange('category')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>
          <div className="w-32">
            <label className="mb-1 block text-sm font-medium text-slate-700">정원</label>
            <input
              type="number"
              min={2}
              max={100}
              required
              value={form.capacity}
              onChange={handleChange('capacity')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
        </div>

        {!isEdit && (
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">모집 방식</label>
            <div className="flex gap-4">
              <label className="flex items-center gap-1.5 text-sm text-slate-700">
                <input
                  type="radio"
                  name="recruitmentType"
                  value="APPROVAL"
                  checked={form.recruitmentType === 'APPROVAL'}
                  onChange={handleChange('recruitmentType')}
                />
                승인제 (신청 후 파티장이 수락)
              </label>
              <label className="flex items-center gap-1.5 text-sm text-slate-700">
                <input
                  type="radio"
                  name="recruitmentType"
                  value="FIRST_COME"
                  checked={form.recruitmentType === 'FIRST_COME'}
                  onChange={handleChange('recruitmentType')}
                />
                선착순 (정원 차면 자동 마감)
              </label>
            </div>
          </div>
        )}

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">주말 모임 일시</label>
          <input
            type="datetime-local"
            required
            value={form.meetingAt}
            onChange={handleChange('meetingAt')}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">카카오 오픈채팅 링크</label>
          <input
            required
            placeholder="https://open.kakao.com/o/xxxxxxx"
            value={form.openChatUrl}
            onChange={handleChange('openChatUrl')}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          />
          <p className="mt-1 text-xs text-slate-400">
            카카오톡 앱에서 오픈채팅방을 만든 뒤 초대 링크를 복사해 붙여넣으세요. 이 링크는 참여가 승인된
            사람에게만 공개됩니다.
          </p>
        </div>

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="mt-2 rounded-md bg-indigo-600 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition-transform duration-150 hover:scale-[1.02] active:scale-95"
        >
          {submitting ? '저장 중...' : isEdit ? '수정 완료' : '주말팟 만들기'}
        </button>
      </form>
    </div>
  );
}
