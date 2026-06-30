import { Link, useNavigate } from "react-router-dom";

function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <nav
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "12px 20px",
        background: "#1f2937",
        color: "#fff",
      }}
    >
      <div style={{ display: "flex", gap: "16px" }}>
        <Link to="/board" style={linkStyle}>
          게시판
        </Link>
        <Link to="/chat" style={linkStyle}>
          채팅방
        </Link>
      </div>

      <button type="button" onClick={handleLogout} style={buttonStyle}>
        로그아웃
      </button>
    </nav>
  );
}

const linkStyle = {
  color: "#fff",
  textDecoration: "none",
  fontWeight: 600,
};

const buttonStyle = {
  background: "transparent",
  border: "1px solid #fff",
  color: "#fff",
  borderRadius: "4px",
  padding: "6px 10px",
  cursor: "pointer",
};

export default Navbar;
