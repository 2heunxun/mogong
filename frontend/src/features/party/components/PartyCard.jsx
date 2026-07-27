import { Link } from 'react-router-dom';

const STATUS_LABEL = {
  RECRUITING: { text: '모집중', className: 'bg-emerald-100 text-emerald-700' },
  CLOSED: { text: '마감', className: 'bg-slate-200 text-slate-600' },
};

export default function PartyCard({ party }) {
  const status = STATUS_LABEL[party.status] ?? STATUS_LABEL.CLOSED;

  return (
    <Link
      to={`/parties/${party.id}`}
      className="block rounded-lg border border-slate-200 bg-white p-4 transition hover:border-indigo-300 hover:shadow-sm"
    >
      <div className="mb-2 flex items-center justify-between">
        <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-600">
          {party.category}
        </span>
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${status.className}`}>
          {status.text}
        </span>
      </div>
      <h3 className="mb-1 line-clamp-1 text-lg font-semibold text-slate-900">{party.title}</h3>
      <div className="flex items-center justify-between text-sm text-slate-500">
        <span>파티장 {party.ownerNickname}</span>
        <span>
          {party.memberCount} / {party.capacity}명
        </span>
      </div>
    </Link>
  );
}
