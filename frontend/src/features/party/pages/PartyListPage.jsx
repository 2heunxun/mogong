import { useEffect, useState } from 'react';
import LoadingSpinner from '../../../shared/components/LoadingSpinner';
import { fetchParties } from '../api/parties';
import PartyCard from '../components/PartyCard';
import { CATEGORIES } from '../constants/categories';

export default function PartyListPage() {
  const [category, setCategory] = useState('');
  const [status, setStatus] = useState('');
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);

  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError('');

    fetchParties({ category: category || undefined, status: status || undefined, keyword: keyword || undefined, page, size: 9 })
      .then((res) => {
        if (!ignore) setData(res);
      })
      .catch(() => {
        if (!ignore) setError('파티 목록을 불러오지 못했습니다.');
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [category, status, keyword, page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim());
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">공부파티 목록</h1>

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <select
          value={category}
          onChange={(e) => {
            setPage(0);
            setCategory(e.target.value);
          }}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="">전체 카테고리</option>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>

        <select
          value={status}
          onChange={(e) => {
            setPage(0);
            setStatus(e.target.value);
          }}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="">모집상태 전체</option>
          <option value="RECRUITING">모집중</option>
          <option value="CLOSED">마감</option>
        </select>

        <form onSubmit={handleSearchSubmit} className="flex flex-1 min-w-[200px] gap-2">
          <input
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
            placeholder="제목/소개 검색"
            className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm"
          />
          <button
            type="submit"
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
          >
            검색
          </button>
        </form>
      </div>

      {loading && <LoadingSpinner />}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && data.content.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-300 py-16 text-center text-slate-400">
          조건에 맞는 공부파티가 없습니다.
        </p>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {data.content.map((party, index) => (
          <PartyCard key={party.id} party={party} index={index} />
        ))}
      </div>

      {data.totalPages > 1 && (
        <div className="mt-8 flex justify-center gap-2">
          {Array.from({ length: data.totalPages }, (_, i) => (
            <button
              key={i}
              type="button"
              onClick={() => setPage(i)}
              className={`h-8 w-8 rounded-md text-sm ${
                i === page ? 'bg-indigo-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-100'
              }`}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
