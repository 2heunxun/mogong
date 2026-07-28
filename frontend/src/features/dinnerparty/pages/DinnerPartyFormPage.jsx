import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createDinnerParty, fetchDinnerPartyDetail, updateDinnerParty } from '../api/dinnerParties';

const emptyForm = {
  title: '',
  description: '',
  location: '',
  meetingAt: '',
  capacity: 4,
  openChatUrl: '',
};

export default function DinnerPartyFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEdit) return;
    fetchDinnerPartyDetail(id)
      .then((party) => {
        if (!party.isOwner) {
          navigate(`/dinner-parties/${id}`, { replace: true });
          return;
        }
        setForm({
          title: party.title,
          description: party.description ?? '',
          location: party.location,
          meetingAt: party.meetingAt ? party.meetingAt.slice(0, 16) : '',
          capacity: party.capacity,
          openChatUrl: party.openChatUrl ?? '',
        });
      })
      .catch(() => setError('저녁팟 정보를 불러오지 못했습니다.'))
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
        await updateDinnerParty(id, payload);
        navigate(`/dinner-parties/${id}`);
      } else {
        const { id: newId } = await createDinnerParty(payload);
        navigate(`/dinner-parties/${newId}`);
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

  if (loading) return <p className="p-10 text-center text-slate-500">불러오는 중...</p>;

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">
        {isEdit ? '저녁팟 수정' : '저녁팟 만들기'}
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

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">식사 장소</label>
          <input
            required
            maxLength={100}
            placeholder="학교 정문 앞 국밥집"
            value={form.location}
            onChange={handleChange('location')}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>

        <div className="flex gap-4">
          <div className="flex-1">
            <label className="mb-1 block text-sm font-medium text-slate-700">저녁 약속 일시</label>
            <input
              type="datetime-local"
              required
              value={form.meetingAt}
              onChange={handleChange('meetingAt')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
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
          className="mt-2 rounded-md bg-indigo-600 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? '저장 중...' : isEdit ? '수정 완료' : '저녁팟 만들기'}
        </button>
      </form>
    </div>
  );
}
