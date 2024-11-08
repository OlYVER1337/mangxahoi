import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SignIn from './SignIn'; // Import trang đăng nhập
import AdminDashboard from './AdminDashboard'; // Import trang Dashboard của Admin
import UserManagement from './components/UserManagement'; // Import trang Quản lý người dùng

function App() {
    return (
        <Router>
            <Routes>
                {/* Route cho trang đăng nhập */}
                <Route path="/" element={<SignIn />} />

                {/* Route cho trang Dashboard (admin-only) */}
                <Route path="/dashboard" element={<AdminDashboard />} />

                {/* Route cho trang Quản lý người dùng */}
                <Route path="/user-management" element={<UserManagement />} />
            </Routes>
        </Router>
    );
}

export default App;
