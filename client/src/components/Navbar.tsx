import { AppBar, Box, Button, Toolbar, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";

function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <AppBar
      position="static"
      elevation={0}
      sx={{
        background: "linear-gradient(90deg, #27500A 0%, #97C459 100%)",
        color: "#fff",
      }}
    >
      <Toolbar sx={{ justifyContent: "space-between" }}>
        <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
          <Typography component={Link} to="/board" sx={linkStyle}>
            게시판
          </Typography>
          <Typography component={Link} to="/chat" sx={linkStyle}>
            채팅방
          </Typography>
        </Box>

        <Button
          variant="outlined"
          onClick={handleLogout}
          sx={buttonStyle}
        >
          로그아웃
        </Button>
      </Toolbar>
    </AppBar>
  );
}

const linkStyle = {
  color: "#fff",
  textDecoration: "none",
  fontWeight: 600,
  fontSize: "1rem",
};

const buttonStyle = {
  color: "#fff",
  borderColor: "rgba(255,255,255,0.8)",
  borderRadius: "4px",
  px: 1.5,
  py: 0.75,
  "&:hover": {
    borderColor: "#fff",
    backgroundColor: "rgba(255,255,255,0.12)",
  },
};

export default Navbar;
