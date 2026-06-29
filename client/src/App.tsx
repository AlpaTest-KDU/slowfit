import { Navigate, Route, Routes } from "react-router-dom";
import BoardPage from "./pages/BoardPage";
import ChatPage from "./pages/ChatPage";
import LoginPage from "./pages/LoginPage";
import PostDetailPage from "./pages/PostDetailPage";
import PostWritePage from "./pages/PostWritePage";
import SignupPage from "./pages/SignupPage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/board" element={<BoardPage />} />
      <Route path="/chat" element={<ChatPage />} />
      <Route path="/post/write" element={<PostWritePage />} />
      <Route path="/posts/:id" element={<PostDetailPage />} />
      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
