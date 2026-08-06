import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/hooks/useAuth';

const CLASS_OPTIONS = Array.from({ length: 10 }, (_, i) => i + 1);

export default function ProfileEditPage() {
  const { user, completeOnboarding } = useAuth();
  const navigate = useNavigate();

  const [classNo, setClassNo] = useState(String(user?.classNo ?? ''));
  const [teamNo, setTeamNo] = useState(String(user?.teamNo ?? ''));
  const [realName, setRealName] = useState(user?.realName ?? '');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await completeOnboarding({
        classNo: Number(classNo),
        teamNo: Number(teamNo),
        realName: realName.trim(),
      });
      navigate('/', { replace: true });
    } catch (err) {
      const fieldErrors = err.response?.data?.fieldErrors;
      if (fieldErrors?.length) {
        setError(fieldErrors.map((f) => f.reason).join(' / '));
      } else {
        setError(err.response?.data?.message ?? '정보 저장에 실패했습니다.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-sm flex-col gap-6 px-4 py-16">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-slate-900">내 정보 수정</h1>
        <p className="mt-1 text-sm text-slate-500">반/조/이름을 수정할 수 있어요.</p>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">반</label>
          <select
            required
            value={classNo}
            onChange={(e) => setClassNo(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          >
            <option value="" disabled>
              반을 선택하세요
            </option>
            {CLASS_OPTIONS.map((n) => (
              <option key={n} value={n}>
                {n}반
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">조</label>
          <input
            required
            type="number"
            min={1}
            max={30}
            value={teamNo}
            onChange={(e) => setTeamNo(e.target.value)}
            placeholder="예: 3"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">이름 (실명)</label>
          <input
            required
            maxLength={20}
            value={realName}
            onChange={(e) => setRealName(e.target.value)}
            placeholder="실명을 입력하세요"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="mt-2 rounded-md bg-indigo-600 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition-transform duration-150 hover:scale-[1.02] active:scale-95"
        >
          {submitting ? '저장 중...' : '저장하기'}
        </button>
      </form>
    </div>
  );
}
