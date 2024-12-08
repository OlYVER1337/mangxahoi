import { useEffect, useState } from "react";
import { useRouter } from "next/router";
import styles from './Dashboard.module.css';

const AdminDashboard = () => {
    const router = useRouter();
    const [adminInfo, setAdminInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalUsers: 0,
        totalPosts: 0,
        activeEditors: 0,
    });

    useEffect(() => {
        const user = sessionStorage.getItem("user");

        if (!user) {
            router.push("/admin/login");
        } else {
            const userData = JSON.parse(user);

            if (userData.role !== "Admin" && userData.role !== "Editor") {
                alert("Bạn không có quyền truy cập!");
                router.push("/admin/login");
            } else {
                setAdminInfo(userData);
            }
        }

        setLoading(false);
    }, [router]);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await fetch("/api/admin/stats");
                if (!response.ok) throw new Error("Failed to fetch stats");
                const data = await response.json();
                setStats(data);
            } catch (error) {
                console.error("Error fetching stats:", error);
            }
        };

        fetchStats();
    }, []);

    const handleLogout = async () => {
        const user = sessionStorage.getItem("user");

        if (user) {
            const userData = JSON.parse(user);

            await fetch("/api/admin/logout", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: userData.email }),
            });

            sessionStorage.removeItem("user");
            router.push("/admin/login");
        }
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
                    <li>Tổng số người dùng: {stats.totalUsers}</li>
                    <li>Tổng số bài viết: {stats.totalPosts}</li>
                    <li>Số biên tập viên đang hoạt động: {stats.activeEditors}</li>
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
