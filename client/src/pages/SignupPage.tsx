import { useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';

export default function SignupPage() {
  const [form, setForm] = useState({
    username: '',
    password: '',
    name: '',
    age: '',
    gender: '',
    email: '',
  });
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await axios.post('http://localhost:9090/api/users/signup', {
        ...form,
        age: Number(form.age),
      });
      navigate('/login');
    } catch (error) {
      console.error('Signup failed:', error);
      alert('회원가입에 실패했습니다.');
    }
  };

  return (
    <div
      style={{
        maxWidth: 400,
        margin: '2rem auto',
        padding: '2rem',
        background: 'rgba(255,255,255,0.85)',
        borderRadius: 12,
      }}
    >
      <h2>회원가입</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        <input name="username" placeholder="username" value={form.username} onChange={handleChange} required />
        <input name="password" type="password" placeholder="password" value={form.password} onChange={handleChange} required />
        <input name="name" placeholder="name" value={form.name} onChange={handleChange} required />
        <input name="age" type="number" placeholder="age" value={form.age} onChange={handleChange} />
        <input name="gender" placeholder="gender" value={form.gender} onChange={handleChange} />
        <input name="email" type="email" placeholder="email" value={form.email} onChange={handleChange} required />
        <button type="submit">회원가입</button>
      </form>
      <p style={{ marginTop: '1rem' }}>
        <Link to="/login">로그인</Link>
      </p>
    </div>
  );
}
