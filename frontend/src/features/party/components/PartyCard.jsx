import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';

const MotionLink = motion.create(Link);

const STATUS_LABEL = {
  RECRUITING: { text: '모집중', className: 'bg-emerald-100 text-emerald-700' },
  CLOSED: { text: '마감', className: 'bg-slate-200 text-slate-600' },
};

const RECRUITMENT_LABEL = {
  FIRST_COME: { text: '선착순', className: 'bg-amber-50 text-amber-600' },
  APPROVAL: { text: '승인제', className: 'bg-sky-50 text-sky-600' },
};

export default function PartyCard({ party, index = 0 }) {
  const status = STATUS_LABEL[party.status] ?? STATUS_LABEL.CLOSED;
  const recruitment = RECRUITMENT_LABEL[party.recruitmentType] ?? RECRUITMENT_LABEL.APPROVAL;

  return (
    <MotionLink
      to={`/parties/${party.id}`}
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay: index * 0.04, ease: 'easeOut' }}
      whileHover={{ y: -4 }}
      whileTap={{ scale: 0.98 }}
      className="block rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition-shadow hover:border-indigo-300 hover:shadow-lg"
    >
      <div className="mb-2 flex items-center justify-between">
        <div className="flex items-center gap-1">
          <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-600">
            {party.category}
          </span>
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${recruitment.className}`}>
            {recruitment.text}
          </span>
        </div>
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
    </MotionLink>
  );
}
