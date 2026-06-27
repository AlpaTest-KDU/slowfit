import { type FormEvent, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function PostWritePage() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [boardType, setBoardType] = useState<'JOGGING' | 'DIET' | 'CERTIFICATION'>('JOGGING');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await axios.post(
        'http://localhost:9090/api/posts',
        {
          boardType,
          title,
          content,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      navigate('/board');
    } catch (err) {
      console.error(err);
      setError('게시글 작성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 700, margin: '2rem auto', padding: '1rem' }}>
      <h2>게시글 작성</h2>
      <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '1rem' }}>
        <label style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          종류
          <select value={boardType} onChange={(e) => setBoardType(e.target.value as 'JOGGING' | 'DIET' | 'CERTIFICATION')} style={{ padding: '0.5rem' }}>
            <option value="JOGGING">JOGGING</option>
            <option value="DIET">DIET</option>
            <option value="CERTIFICATION">CERTIFICATION</option>
          </select>
        </label>

        <label style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          제목
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            placeholder="제목을 입력하세요"
            style={{ padding: '0.75rem', borderRadius: 4, border: '1px solid #ccc' }}
          />
        </label>

        <label style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          내용
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            rows={8}
            placeholder="내용을 입력하세요"
            style={{ padding: '0.75rem', borderRadius: 4, border: '1px solid #ccc' }}
          />
        </label>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button type="submit" disabled={loading} style={{ padding: '0.75rem 1.25rem', cursor: 'pointer' }}>
            {loading ? '작성 중...' : '작성 완료'}
          </button>
          <button type="button" onClick={() => navigate('/board')} style={{ padding: '0.75rem 1.25rem', cursor: 'pointer' }}>
            취소
          </button>
        </div>
      </form>
    </div>
  );
}
