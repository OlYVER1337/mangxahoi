import { getFirestore } from "firebase-admin/firestore";
import admin from "firebase-admin";

// Kiểm tra nếu Firebase chưa được khởi tạo
if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.applicationDefault(),
    });
}

const db = getFirestore();

export default async function handler(req, res) {
    if (req.method !== "GET") {
        return res.status(405).json({ message: "Method not allowed" });
    }
    try {
        // Lấy tổng số người dùng
        const usersSnapshot = await db.collection("users").get();
        const totalUsers = usersSnapshot.size;
        // Lấy tổng số bài viết
        const postsSnapshot = await db.collection("posts").get();
        const totalPosts = postsSnapshot.size;
        // Đếm số biên tập viên đang hoạt động
        let activeEditors = 0;
        usersSnapshot.forEach((doc) => {
            const data = doc.data();
            if (data.role === "Editor" && data.availability === 1) {
                activeEditors++;
            }
        });
        return res.status(200).json({
            totalUsers,
            totalPosts,
            activeEditors,
        });
    } catch (error) {
        console.error("Error fetching stats:", error);
        return res.status(500).json({ message: "Internal Server Error" });
    }
}
