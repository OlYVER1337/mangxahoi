// pages/admin/dashboard.js

import { useEffect, useState } from "react";
import { useRouter } from "next/router";
import styles from './Dashboard.module.css';  // Thêm tệp CSS

const AdminDashboard = () => {
    const router = useRouter();
    const [adminInfo, setAdminInfo] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const user = sessionStorage.getItem("user");

        if (!user) {
            router.push("/admin/login");
        } else {
            const userData = JSON.parse(user);

            if (userData.role !== "admin") {
                alert("Bạn không có quyền truy cập!");
                router.push("/admin/login");
            } else {
                setAdminInfo(userData);
            }
        }

        setLoading(false);
    }, [router]);

    const handleLogout = () => {
        sessionStorage.removeItem("user");
        router.push("/admin/login");
    };

    if (loading) {
        return <p>Đang tải...</p>;
    }

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h1 className={styles.title}>Bảng Điều Khiển Admin</h1>
                <p className={styles.welcome}>Xin chào, {adminInfo?.name || "Admin"}!</p>

                <div className={styles.actionButtons}>
                    <button onClick={handleLogout} className={styles.logoutButton}>Đăng Xuất</button>
                </div>

                <h2>Tổng Quan</h2>
                <ul className={styles.statsList}>
                    <li>Tổng số người dùng: 10</li>
                    <li>Tổng số bài viết: 20</li>
                    <li>Số biên tập viên đang hoạt động: 2</li>
                </ul>

                <h2>Chức Năng Quản Lý</h2>
                <ul className={styles.linksList}>
                    <li><a href="/admin/manage-users">Quản lý người dùng</a></li>
                    <li><a href="/admin/manage-posts">Quản lý bài viết</a></li>
                    <li><a href="/admin/settings">Cài đặt</a></li>
                </ul>
            </div>
        </div>
    );
};

export default AdminDashboard;
