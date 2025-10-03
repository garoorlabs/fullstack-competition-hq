import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './hooks/useAuth';
import Login from './pages/Login';
import Signup from './pages/Signup';
import MyCompetitions from './pages/MyCompetitions';
import CreateCompetition from './pages/CreateCompetition';
import CompetitionDetail from './pages/CompetitionDetail';
import StripeReturn from './pages/StripeReturn';
import StripeRefresh from './pages/StripeRefresh';
import RegisterTeam from './pages/RegisterTeam';
import TeamRegistrationSuccess from './pages/TeamRegistrationSuccess';
import MyTeams from './pages/MyTeams';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route
            path="/competitions"
            element={
              <ProtectedRoute>
                <MyCompetitions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/competitions/new"
            element={
              <ProtectedRoute>
                <CreateCompetition />
              </ProtectedRoute>
            }
          />
          <Route path="/competitions/:id" element={<CompetitionDetail />} />
          <Route
            path="/competitions/:competitionId/register"
            element={
              <ProtectedRoute>
                <RegisterTeam />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teams"
            element={
              <ProtectedRoute>
                <MyTeams />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teams/registration/success"
            element={
              <ProtectedRoute>
                <TeamRegistrationSuccess />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/stripe/return"
            element={
              <ProtectedRoute>
                <StripeReturn />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/stripe/refresh"
            element={
              <ProtectedRoute>
                <StripeRefresh />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/competitions" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
