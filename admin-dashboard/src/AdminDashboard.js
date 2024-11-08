// src/AdminDashboard.js
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { db } from './firebaseConfig';
import { collection, getDocs } from 'firebase/firestore';
import { Box, Typography, Button } from '@mui/material';

export default function AdminDashboard() {
    const [userCount, setUserCount] = useState(0);
    const navigate = useNavigate();

    useEffect(() => {
        // Lấy số lượng người dùng từ Firestore
        const fetchUserCount = async () => {
            try {
                const usersSnapshot = await getDocs(collection(db, 'users'));
                setUserCount(usersSnapshot.size);
            } catch (error) {
                console.error('Error fetching user count:', error);
            }
        };

        fetchUserCount();
    }, []);

    // Hàm điều hướng sang trang quản lý người dùng
    const handleNavigateToUserManagement = () => {
        navigate('/user-management'); // Đảm bảo đã cấu hình đường dẫn này trong file routes
    };

    return (
        <Box sx={{ padding: 4 }}>
            <Typography variant="h4" gutterBottom>
                Admin Dashboard
            </Typography>
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: 3,
                    borderRadius: 1,
                    backgroundColor: '#f5f5f5',
                    marginBottom: 2,
                }}
            >
                <Typography variant="h6">Tổng số người dùng:</Typography>
                <Typography variant="h6" color="primary">{userCount}</Typography>
            </Box>
            <Button
                variant="contained"
                color="primary"
                onClick={handleNavigateToUserManagement}
            >
                Quản lý người dùng
            </Button>
        </Box>
    );
}
