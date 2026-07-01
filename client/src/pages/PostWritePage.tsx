import { type FormEvent, useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

export default function PostWritePage() {
  const { id } = useParams<{ id: string }>();
  const isEditMode = Boolean(id);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [pace, setPace] = useState("");
  const [boardType, setBoardType] = useState<
    "JOGGING" | "DIET" | "CERTIFICATION"
  >("JOGGING");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isEditMode) return;

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

        setTitle(response.data.title);
        setContent(response.data.content);
        setPace(response.data.pace ?? "");
        setBoardType(response.data.boardType);
      } catch (err) {
        console.error(err);
        setError("게시글을 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [id, isEditMode, navigate]);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    setLoading(true);
    setError("");

    try {
      if (isEditMode && id) {
        await axios.put(
          `http://localhost:9090/api/posts/${id}`,
          {
            boardType,
            title,
            content,
            pace,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          },
        );
        navigate(`/posts/${id}`);
      } else {
        await axios.post(
          "http://localhost:9090/api/posts",
          {
            boardType,
            title,
            content,
            pace,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          },
        );

        navigate("/board");
      }
    } catch (err) {
      console.error(err);
      setError(
        isEditMode
          ? "게시글 수정에 실패했습니다."
          : "게시글 작성에 실패했습니다.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 700, margin: "2rem auto", padding: "1rem" }}>
      <h2>{isEditMode ? "게시글 수정" : "게시글 작성"}</h2>
      <form onSubmit={handleSubmit} style={{ display: "grid", gap: "1rem" }}>
        <label
          style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}
        >
          종류
          <select
            value={boardType}
            onChange={(e) => setBoardType(e.target.value as "JOGGING" | "DIET")}
            style={{ padding: "0.5rem" }}
          >
            <option value="JOGGING">JOGGING</option>
            <option value="DIET">DIET</option>
          </select>
        </label>

        <label
          style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}
        >
          제목
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            placeholder="제목을 입력하세요"
            style={{
              padding: "0.75rem",
              borderRadius: 4,
              border: "1px solid #ccc",
            }}
          />
        </label>
        {boardType === "JOGGING" && (
          <label
            style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}
          >
            페이스
            <input
              type="text"
              value={pace}
              onChange={(e) => setPace(e.target.value)}
              placeholder="예:7m30s/km"
              style={{
                padding: "0.75rem",
                borderRadius: 4,
                border: "1px solid #ccc",
              }}
            />
          </label>
        )}

        <label
          style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}
        >
          내용
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            rows={8}
            placeholder="내용을 입력하세요"
            style={{
              padding: "0.75rem",
              borderRadius: 4,
              border: "1px solid #ccc",
            }}
          />
        </label>

        {error && <p style={{ color: "red" }}>{error}</p>}

        <div style={{ display: "flex", gap: "0.75rem" }}>
          <button
            type="submit"
            disabled={loading}
            style={{ padding: "0.75rem 1.25rem", cursor: "pointer" }}
          >
            {loading
              ? isEditMode
                ? "수정 중..."
                : "작성 중..."
              : isEditMode
                ? "수정 완료"
                : "작성 완료"}
          </button>
          <button
            type="button"
            onClick={() => navigate("/board")}
            style={{ padding: "0.75rem 1.25rem", cursor: "pointer" }}
          >
            취소
          </button>
        </div>
      </form>
    </div>
  );
}
