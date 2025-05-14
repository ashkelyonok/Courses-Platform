import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
// import { useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Courses from './pages/Courses';
import CourseDetails from './pages/CourseDetails';
//import UserDetails from './pages/UserDetails';
import Profile from './pages/Profile';
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import { Layout } from 'antd';

const { Content } = Layout;

// ProtectedRoute component to guard routes requiring authentication
function ProtectedRoute({ children }) {
    // const { user } = useAuth();
    // const location = useLocation();

    // if (!user) {
    //     // Redirect to login, preserving the attempted route
    //     return <Navigate to="/login" state={{ from: location }} replace />;
    // }

    return children;
}

function App() {
    const location = useLocation();
    const isHomePage = location.pathname === '/';

    return (
        <Layout className="min-h-screen">
            {!isHomePage && <Header />}
            <Content className={`${isHomePage ? '' : 'bg-gray-50 py-8'}`}>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route
                        path="/courses"
                        element={
                            <ProtectedRoute>
                                <Courses />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/courses/:id"
                        element={
                            <ProtectedRoute>
                                <CourseDetails />
                            </ProtectedRoute>
                        }
                    />
                    {/* <Route
                        path="/users/:id"
                        element={
                            <ProtectedRoute>
                                <UserDetails />
                            </ProtectedRoute>
                        }
                    /> */}
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <Profile />
                            </ProtectedRoute>
                        }
                    />
                    {/* Catch-all route for 404 */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </Content>
            <Footer />
        </Layout>
    );
}

export default App;