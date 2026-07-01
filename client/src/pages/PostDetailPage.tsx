import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";

interface PostDetail {
  id: number;
  username: string;
  boardType: "JOGGING" | "DIET" | "CERTIFICATION";
  title: string;
  content: string;
  pace?: string | null;
  courseUrl?: string | null;
  imageUrl?: string | null;
  viewCount: number;
  likeCount: number;
  createdAt: string;
  updatedAt: string;
}

interface CommentDetail {
  id: number;
  username: string;
  mentionUsername: string | null;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [post, setPost] = useState<PostDetail | null>(null);
  const [comments, setComments] = useState<CommentDetail[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [commentContent, setCommentContent] = useState("");
  const [mentionUsername, setMentionUsername] = useState("");
  const [commentSubmitting, setCommentSubmitting] = useState(false);
  const [commentError, setCommentError] = useState("");
  const [liked, setLiked] = useState(false);
  const [likeSubmitting, setLikeSubmitting] = useState(false);
  const [likeError, setLikeError] = useState("");
  const [deleteSubmitting, setDeleteSubmitting] = useState(false);
  const navigate = useNavigate();
  const currentUsername = localStorage.getItem("username") ?? "";
  const currentRole = localStorage.getItem("role") ?? "";
  const isAdmin = currentRole === "ADMIN";
  const isAuthor = Boolean(
    post && currentUsername && post.username === currentUsername,
  );

  useEffect(() => {
    if (!id) return;

    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    const fetchPost = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await axios.get(
          `http://localhost:9090/api/posts/${id}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          },
        );
        setPost(response.data);
      } catch (err) {
        console.error(err);
        setError("게시글을 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    const fetchComments = async () => {
      try {
        const response = await axios.get<CommentDetail[]>(
          `http://localhost:9090/api/posts/${id}/comments`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          },
        );
        setComments(response.data);
      } catch (err) {
        console.error(err);
      }
    };

    fetchPost();
    fetchComments();
  }, [id, navigate]);

  const handleLikeToggle = async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    setLikeError("");
    setLikeSubmitting(true);

    try {
      const response = await axios.post<boolean>(
        `http://localhost:9090/api/posts/${id}/like`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      const likedResponse = response.data;
      setPost((prev) => {
        if (!prev) return prev;

        const countDelta = likedResponse ? 1 : -1;
        return {
          ...prev,
          likeCount: Math.max(0, prev.likeCount + countDelta),
        };
      });
      setLiked(likedResponse);
    } catch (err) {
      console.error(err);
      setLikeError("좋아요 토글에 실패했습니다.");
    } finally {
      setLikeSubmitting(false);
    }
  };

  const handleCommentSubmit = async (
    event: React.FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault();
    setCommentError("");
    setCommentSubmitting(true);

    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    if (!commentContent.trim()) {
      setCommentError("댓글 내용을 입력해주세요.");
      setCommentSubmitting(false);
      return;
    }

    try {
      const mentionUsernameForBackend = mentionUsername.startsWith("@")
        ? mentionUsername.slice(1)
        : mentionUsername;

      const response = await axios.post<CommentDetail>(
        `http://localhost:9090/api/posts/${id}/comments`,
        {
          content: commentContent,
          mentionUsername: mentionUsernameForBackend.trim() || null,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );
      setComments((prev) => [...prev, response.data]);
      setCommentContent("");
      setMentionUsername("");
    } catch (err) {
      console.error(err);
      setCommentError("댓글 작성에 실패했습니다.");
    } finally {
      setCommentSubmitting(false);
    }
  };

  const handleDeletePost = async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    if (!id) return;

    setDeleteSubmitting(true);

    try {
      await axios.delete(`http://localhost:9090/api/posts/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      navigate("/board");
    } catch (err) {
      console.error(err);
      setError("게시글 삭제에 실패했습니다.");
    } finally {
      setDeleteSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    try {
      await axios.delete(`http://localhost:9090/api/comments/${commentId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setComments((prev) => prev.filter((comment) => comment.id !== commentId));
    } catch (err) {
      console.error(err);
      setCommentError("댓글 삭제에 실패했습니다.");
    }
  };

  return (
    <div
      style={{
        maxWidth: 800,
        margin: "2rem auto",
        padding: "2rem",
        background: "rgba(255,255,255,0.85)",
        borderRadius: 12,
      }}
    >
      <button
        type="button"
        onClick={() => navigate("/board")}
        style={{
          marginBottom: "1rem",
          padding: "0.75rem 1rem",
          cursor: "pointer",
        }}
      >
        목록으로 돌아가기
      </button>

      {loading && <p>로딩 중...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      {post ? (
        <>
          <article
            style={{
              border: "1px solid #ddd",
              borderRadius: 8,
              padding: "1rem",
              marginBottom: "2rem",
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: "1rem",
                marginBottom: "0.75rem",
              }}
            >
              <h2 style={{ margin: 0 }}>{post.title}</h2>
              {(isAuthor || isAdmin) && (
                <div style={{ display: "flex", gap: "0.5rem" }}>
                  <button
                    type="button"
                    onClick={() => navigate(`/posts/${id}/edit`)}
                    style={{
                      padding: "0.5rem 0.75rem",
                      cursor: "pointer",
                      borderRadius: 4,
                      border: "1px solid #1976d2",
                      background: "#fff",
                      color: "#1976d2",
                    }}
                  >
                    수정
                  </button>
                  <button
                    type="button"
                    onClick={handleDeletePost}
                    disabled={deleteSubmitting}
                    style={{
                      padding: "0.5rem 0.75rem",
                      cursor: deleteSubmitting ? "not-allowed" : "pointer",
                      borderRadius: 4,
                      border: "1px solid #d32f2f",
                      background: "#fff",
                      color: "#d32f2f",
                    }}
                  >
                    {deleteSubmitting ? "삭제 중..." : "삭제"}
                  </button>
                </div>
              )}
            </div>
            <p style={{ margin: "0.5rem 0", color: "#555" }}>
              {post.boardType} · 작성자: {post.username}
              {post.pace ? ` · ♥${post.pace}` : ""}
              {post.courseUrl && (
                <a
                  href={post.courseUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{ color: "#1976d2", textDecoration: "underline" }}
                >
                  코스 보기
                </a>
              )}
            </p>
            <div style={{ whiteSpace: "pre-wrap", marginBottom: "1rem" }}>
              {post.content}
            </div>
            {post.imageUrl && (
              <img
                src={`http://localhost:9090${post.imageUrl}`}
                alt="게시글 이미지"
                style={{
                  maxWidth: "100%",
                  borderRadius: 8,
                  marginBottom: "1rem",
                }}
              />
            )}
            <div
              style={{
                display: "flex",
                gap: "1rem",
                fontSize: 14,
                color: "#555",
                alignItems: "center",
              }}
            >
              <span>조회수: {post.viewCount}</span>
              <span>좋아요: {post.likeCount}</span>
              <button
                type="button"
                onClick={handleLikeToggle}
                disabled={likeSubmitting}
                style={{
                  padding: "0.5rem 0.75rem",
                  borderRadius: 4,
                  border: "1px solid #1976d2",
                  background: liked ? "#1976d2" : "#fff",
                  color: liked ? "#fff" : "#1976d2",
                  cursor: likeSubmitting ? "not-allowed" : "pointer",
                }}
              >
                {liked ? "좋아요 취소" : "좋아요"}
              </button>
              <span>생성: {formatDate(post.createdAt)}</span>
              <span>수정: {formatDate(post.updatedAt)}</span>
            </div>
            {likeError && <p style={{ color: "red" }}>{likeError}</p>}
          </article>

          <section style={{ marginBottom: "2rem" }}>
            <h3 style={{ marginBottom: "1rem" }}>댓글 작성</h3>
            <form onSubmit={handleCommentSubmit}>
              <div style={{ marginBottom: "1rem" }}>
                <label style={{ display: "block", marginBottom: "0.5rem" }}>
                  댓글 내용
                </label>
                <textarea
                  value={commentContent}
                  onChange={(e) => {
                    const value = e.target.value;
                    setCommentContent(value);
                    const match = value.trim().match(/^@([^\s@]+)/);
                    setMentionUsername(match ? `@${match[1]}` : "");
                  }}
                  rows={4}
                  style={{
                    width: "100%",
                    padding: "0.75rem",
                    borderRadius: 4,
                    border: "1px solid #ccc",
                    resize: "vertical",
                  }}
                />
              </div>
              {commentError && (
                <p style={{ color: "red", marginBottom: "1rem" }}>
                  {commentError}
                </p>
              )}
              <button
                type="submit"
                disabled={commentSubmitting}
                style={{
                  padding: "0.75rem 1.5rem",
                  cursor: "pointer",
                  borderRadius: 4,
                  border: "none",
                  background: "#1976d2",
                  color: "#fff",
                }}
              >
                {commentSubmitting ? "작성 중..." : "댓글 작성"}
              </button>
            </form>
          </section>

          <section>
            <h3 style={{ marginBottom: "1rem" }}>댓글 목록</h3>
            {comments.length === 0 ? (
              <p>등록된 댓글이 없습니다.</p>
            ) : (
              comments.map((comment) => {
                const isCommentAuthor = Boolean(
                  currentUsername && comment.username === currentUsername,
                );

                return (
                  <article
                    key={comment.id}
                    style={{
                      border: "1px solid #eee",
                      borderRadius: 8,
                      padding: "1rem",
                      marginBottom: "1rem",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        gap: "1rem",
                      }}
                    >
                      <p
                        style={{ margin: 0, color: "#333", fontWeight: "bold" }}
                      >
                        <span
                          onClick={() => {
                            const mentionTag = `@${comment.username} `;
                            setCommentContent((prev) => {
                              const trimmed = prev.trimStart();
                              const currentMentionMatch =
                                trimmed.match(/^@[^\s@]+\s*/);
                              const remaining = currentMentionMatch
                                ? trimmed.slice(currentMentionMatch[0].length)
                                : trimmed;
                              return `${mentionTag}${remaining}`;
                            });
                            setMentionUsername(`@${comment.username}`);
                          }}
                          style={{
                            cursor: "pointer",
                            color: "#1976d2",
                            textDecoration: "underline",
                          }}
                        >
                          {comment.username}
                        </span>
                        {comment.mentionUsername
                          ? ` → @${comment.mentionUsername}`
                          : ""}
                      </p>
                      {(isCommentAuthor || isAdmin) && (
                        <button
                          type="button"
                          onClick={() => handleDeleteComment(comment.id)}
                          style={{
                            padding: "0.35rem 0.6rem",
                            cursor: "pointer",
                            borderRadius: 4,
                            border: "1px solid #d32f2f",
                            background: "#fff",
                            color: "#d32f2f",
                          }}
                        >
                          삭제
                        </button>
                      )}
                    </div>
                    <p style={{ whiteSpace: "pre-wrap", margin: "0.75rem 0" }}>
                      {comment.content}
                    </p>
                    <div style={{ fontSize: 13, color: "#666" }}>
                      <span>작성: {formatDate(comment.createdAt)}</span>
                      <span style={{ marginLeft: "1rem" }}>
                        수정: {formatDate(comment.updatedAt)}
                      </span>
                    </div>
                  </article>
                );
              })
            )}
          </section>
        </>
      ) : (
        !loading && <p>게시글을 찾을 수 없습니다.</p>
      )}
    </div>
  );
}

const formatDate = (value: string) => {
  if (!value) return "-";
  const date = new Date(value);
  return date.toLocaleString();
};
