import { useCallback, useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const PAGE_SIZE = 10;

export default function BoardPage() {
  const [posts, setPosts] = useState<Array<PostItem>>([]);
  const [activeBoardType, setActiveBoardType] = useState<BoardType | null>(
    null,
  );
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const fetchPosts = useCallback(
    async (boardType: BoardType | null, pageNumber = 0) => {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        navigate("/login");
        return;
      }

      setLoading(true);
      setError("");

      try {
        const params: Record<string, unknown> = {
          page: pageNumber,
          size: PAGE_SIZE,
        };

        if (boardType) {
          params.boardType = boardType;
        }

        const response = await axios.get("http://localhost:9090/api/posts", {
          params,
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        const pageData = response.data;
        const content = pageData?.content ?? [];
        const currentPage = typeof pageData?.number === "number" ? pageData.number : pageNumber;
        const total = typeof pageData?.totalPages === "number" ? pageData.totalPages : 1;

        setPosts(content);
        setPage(currentPage);
        setTotalPages(total);
        setActiveBoardType(boardType);
      } catch (err) {
        console.error(err);
        setError("게시글을 불러오는 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    },
    [navigate],
  );

  useEffect(() => {
    const initialize = async () => {
      await fetchPosts(null, 0);
    };

    initialize();
  }, [fetchPosts]);

  const handleTabClick = (boardType: BoardType | null) => {
    fetchPosts(boardType, 0);
  };

  const handlePrevPage = () => {
    if (page > 0) {
      fetchPosts(activeBoardType, page - 1);
    }
  };

  const handleNextPage = () => {
    if (page + 1 < totalPages) {
      fetchPosts(activeBoardType, page + 1);
    }
  };

  return (
    <div
      style={{
        maxWidth: 900,
        margin: "2rem auto",
        padding: "2rem",
        background: "rgba(255,255,255,0.85)",
        borderRadius: 12,
      }}
    >
      <h2>게시판</h2>
      <button
        type="button"
        onClick={() => navigate("/post/write")}
        style={{
          marginBottom: "1rem",
          padding: "0.75rem 1rem",
          cursor: "pointer",
        }}
      >
        글쓰기
      </button>

      <div style={{ display: "flex", gap: "0.5rem", marginBottom: "1rem" }}>
        <button
          onClick={() => handleTabClick(null)}
          style={tabButtonStyle(activeBoardType === null)}
        >
          전체
        </button>
        <button
          onClick={() => handleTabClick("JOGGING")}
          style={tabButtonStyle(activeBoardType === "JOGGING")}
        >
          JOGGING
        </button>
        <button
          onClick={() => handleTabClick("DIET")}
          style={tabButtonStyle(activeBoardType === "DIET")}
        >
          DIET
        </button>
        <button
          onClick={() => navigate("/chat")}
          style={tabButtonStyle(activeBoardType === "CERTIFICATION")}
        >
          CERTIFICATION
        </button>
      </div>

      {loading && <p>로딩 중...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      {posts.length === 0 && !loading ? (
        <p>등록된 게시글이 없습니다.</p>
      ) : (
        <>
          <div style={{ display: "grid", gap: "1rem" }}>
            {posts.map((post) => (
              <article
                key={post.id}
                style={{
                  border: "1px solid #ddd",
                  padding: "1rem",
                  borderRadius: 8,
                }}
              >
              <div
                style={{
                  marginBottom: "0.5rem",
                  display: "flex",
                  justifyContent: "space-between",
                }}
              >
                <strong
                  style={{ cursor: "pointer", color: "#007bff" }}
                  onClick={() => navigate(`/posts/${post.id}`)}
                >
                  {post.title}
                </strong>
                <span>{post.boardType}</span>
              </div>
              <p style={{ margin: "0.5rem 0" }}>{post.content}</p>
              <div
                style={{
                  display: "flex",
                  gap: "1rem",
                  flexWrap: "wrap",
                  fontSize: 14,
                  color: "#555",
                }}
              >
                <span>작성자: {post.username}</span>
                <span>조회수: {post.viewCount}</span>
                <span>좋아요: {post.likeCount}</span>
                <span>생성: {formatDate(post.createdAt)}</span>
                <span>수정: {formatDate(post.updatedAt)}</span>
              </div>
            </article>
          ))}
        </div>

        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: "1rem",
            marginTop: "1.5rem",
          }}
        >
          <button
            type="button"
            onClick={handlePrevPage}
            disabled={page === 0}
            style={paginationButtonStyle(page === 0)}
          >
            이전
          </button>
          <span>
            {totalPages > 0
              ? `현재 페이지 ${page + 1} / ${totalPages}`
              : `현재 페이지 ${page + 1}`}
          </span>
          <button
            type="button"
            onClick={handleNextPage}
            disabled={page + 1 >= totalPages}
            style={paginationButtonStyle(page + 1 >= totalPages)}
          >
            다음
          </button>
        </div>
      </>
      )}
    </div>
  );
}

type BoardType = "JOGGING" | "DIET" | "CERTIFICATION";

interface PostItem {
  id: number;
  username: string;
  boardType: BoardType;
  title: string;
  content: string;
  viewCount: number;
  likeCount: number;
  createdAt: string;
  updatedAt: string;
}

const tabButtonStyle = (active: boolean) => ({
  padding: "0.5rem 1rem",
  border: "1px solid #ccc",
  borderRadius: 6,
  background: active ? "#333" : "#fff",
  color: active ? "#fff" : "#000",
  cursor: "pointer",
});

const paginationButtonStyle = (disabled: boolean) => ({
  padding: "0.5rem 1rem",
  border: "1px solid #ccc",
  borderRadius: 6,
  background: disabled ? "#f5f5f5" : "#fff",
  color: disabled ? "#999" : "#000",
  cursor: disabled ? "not-allowed" : "pointer",
});

const formatDate = (value: string) => {
  if (!value) return "-";
  const date = new Date(value);
  return date.toLocaleString();
};
