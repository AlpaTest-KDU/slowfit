import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import type { Message } from '@stomp/stompjs';

type MessageType = 'ENTER' | 'TALK' | 'LEAVE';

interface ChatMessageDto {
  type: MessageType;
  roomId: number;
  username: string;
  content: string;
  createdAt: string;
}

export default function ChatPage() {
  const navigate = useNavigate();
  const [username] = useState(() => localStorage.getItem('username') ?? '');
  const [messages, setMessages] = useState<ChatMessageDto[]>([]);
  const [input, setInput] = useState('');
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const roomId = 1;

  useEffect(() => {
    if (!username) {
      navigate('/login');
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:9090/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);

        client.subscribe(`/topic/chat/${roomId}`, (message: Message) => {
          if (!message.body) return;
          const payload = JSON.parse(message.body) as ChatMessageDto;
          setMessages((prev) => [...prev, payload]);
        });

        const enterMessage: ChatMessageDto = {
          type: 'ENTER',
          roomId,
          username,
          content: `${username}님이 입장했습니다.`,
          createdAt: new Date().toISOString(),
        };

        client.publish({
          destination: `/app/chat/${roomId}`,
          body: JSON.stringify(enterMessage),
        });
      },
      onDisconnect: () => {
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'], frame.body);
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      if (clientRef.current && clientRef.current.active) {
        clientRef.current.deactivate();
      }
    };
  }, [navigate, roomId, username]);

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!input.trim() || !clientRef.current?.connected) {
      return;
    }

    const chatMessage: ChatMessageDto = {
      type: 'TALK',
      roomId,
      username,
      content: input.trim(),
      createdAt: new Date().toISOString(),
    };

    clientRef.current.publish({
      destination: `/app/chat/${roomId}`,
      body: JSON.stringify(chatMessage),
    });

    setInput('');
  };

  return (
    <div
      style={{
        maxWidth: 900,
        margin: '2rem auto',
        padding: '2rem',
        background: 'rgba(255,255,255,0.85)',
        borderRadius: 12,
      }}
    >
      <h2>채팅방</h2>
      <p>채팅방 ID: {roomId}</p>
      <p>사용자: {username}</p>
      <p style={{ color: connected ? '#2d7a2d' : '#d23c3c' }}>
        {connected ? '연결됨' : '연결 중...'}
      </p>

      <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: '1rem', minHeight: 320, marginBottom: '1rem', overflowY: 'auto' }}>
        {messages.length === 0 ? (
          <p>메시지가 없습니다.</p>
        ) : (
          messages.map((message, index) => (
            <div key={`${message.createdAt}-${index}`} style={{ marginBottom: '0.75rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, color: '#555' }}>
                <strong>{message.username}</strong>
                <span>{formatDate(message.createdAt)}</span>
              </div>
              <p style={{ margin: '0.25rem 0 0', whiteSpace: 'pre-wrap' }}>{message.content}</p>
            </div>
          ))
        )}
      </div>

      <form onSubmit={handleSubmit}>
        <textarea
          value={input}
          onChange={(event) => setInput(event.target.value)}
          rows={4}
          placeholder="메시지를 입력하세요."
          style={{ width: '100%', padding: '0.75rem', borderRadius: 8, border: '1px solid #ccc', resize: 'vertical', marginBottom: '1rem' }}
        />
        <button
          type="submit"
          disabled={!connected || !input.trim()}
          style={{ padding: '0.75rem 1.5rem', borderRadius: 8, border: 'none', background: '#1976d2', color: '#fff', cursor: 'pointer' }}
        >
          전송
        </button>
      </form>
    </div>
  );
}

const formatDate = (value: string) => {
  if (!value) return '-';
  return new Date(value).toLocaleString();
};
