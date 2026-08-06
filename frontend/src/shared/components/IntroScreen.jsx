import { useEffect } from 'react';
import { motion } from 'framer-motion';
import logoMark from '../../assets/logo-mark.png';

const INTRO_DURATION_MS = 2600;

const container = {
  hidden: {},
  show: {
    transition: { staggerChildren: 0.18, delayChildren: 0.1 },
  },
};

const item = {
  hidden: { opacity: 0, y: 16 },
  show: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } },
};

export default function IntroScreen({ onFinish }) {
  useEffect(() => {
    const timer = setTimeout(onFinish, INTRO_DURATION_MS);
    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <motion.div
      className="fixed inset-0 z-50 flex flex-col items-center justify-center overflow-hidden bg-gradient-to-br from-indigo-600 via-indigo-700 to-slate-900"
      initial={{ opacity: 1 }}
      exit={{ opacity: 0, scale: 1.04 }}
      transition={{ duration: 0.5, ease: 'easeInOut' }}
    >
      <motion.div
        className="absolute -left-24 -top-24 h-72 w-72 rounded-full bg-white/10 blur-3xl"
        animate={{ scale: [1, 1.15, 1] }}
        transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
      />
      <motion.div
        className="absolute -bottom-24 -right-24 h-72 w-72 rounded-full bg-indigo-400/20 blur-3xl"
        animate={{ scale: [1.1, 1, 1.1] }}
        transition={{ duration: 7, repeat: Infinity, ease: 'easeInOut' }}
      />

      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="relative flex flex-col items-center gap-4 text-center"
      >
        <motion.img
          variants={item}
          src={logoMark}
          alt="모공 로고"
          className="h-20 w-20 drop-shadow-[0_0_24px_rgba(255,255,255,0.35)]"
        />
        <motion.h1 variants={item} className="text-4xl font-bold tracking-tight text-white">
          모공
        </motion.h1>
        <motion.p variants={item} className="text-sm text-indigo-100">
          모아봐요 공부팀!
        </motion.p>
      </motion.div>

      <div className="absolute bottom-14 h-1 w-40 overflow-hidden rounded-full bg-white/20">
        <motion.div
          className="h-full rounded-full bg-white"
          initial={{ width: '0%' }}
          animate={{ width: '100%' }}
          transition={{ duration: INTRO_DURATION_MS / 1000, ease: 'linear' }}
        />
      </div>

      <motion.button
        type="button"
        onClick={onFinish}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5, duration: 0.4 }}
        className="absolute bottom-6 right-6 text-xs text-indigo-100/80 underline-offset-2 hover:text-white hover:underline"
      >
        건너뛰기
      </motion.button>
    </motion.div>
  );
}
