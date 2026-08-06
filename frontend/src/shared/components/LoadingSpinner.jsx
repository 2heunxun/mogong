import { motion } from 'framer-motion';

export default function LoadingSpinner({ label = '불러오는 중...', className = '' }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.2 }}
      className={`flex flex-col items-center justify-center gap-3 py-16 ${className}`}
    >
      <span className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-200 border-t-indigo-600" />
      {label && <span className="text-sm text-slate-400">{label}</span>}
    </motion.div>
  );
}
