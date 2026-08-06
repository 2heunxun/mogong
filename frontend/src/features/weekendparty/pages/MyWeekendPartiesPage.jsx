import { useEffect, useState } from 'react';
import LoadingSpinner from '../../../shared/components/LoadingSpinner';
import { fetchMyWeekendParties } from '../api/weekendParties';
import WeekendPartyCard from '../components/WeekendPartyCard';

function WeekendPartySection({ title, parties, emptyText }) {
  return (
    <section className="mb-10">
      <h2 className="mb-3 text-lg font-semibold text-slate-900">{title}</h2>
      {parties.length === 0 ? (
        <p className="rounded-lg border border-dashed border-slate-300 py-10 text-center text-slate-400">
          {emptyText}
        </p>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {parties.map((party) => (
            <WeekendPartyCard key={party.id} party={party} />
          ))}
        </div>
      )}
    </section>
  );
}

export default function MyWeekendPartiesPage() {
  const [data, setData] = useState({ owned: [], joined: [], pending: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyWeekendParties()
      .then(setData)
      .catch(() => setError('내 주말팟 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <p className="p-10 text-center text-red-500">{error}</p>;

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">내 주말팟</h1>

      <WeekendPartySection title="내가 만든 주말팟" parties={data.owned} emptyText="아직 만든 주말팟이 없습니다." />
      <WeekendPartySection title="참여 중인 주말팟" parties={data.joined} emptyText="아직 참여 중인 주말팟이 없습니다." />
      <WeekendPartySection title="신청 중인 주말팟" parties={data.pending} emptyText="대기중인 참여 신청이 없습니다." />
    </div>
  );
}
