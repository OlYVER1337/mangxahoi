// pages/api/admin/users.js

import { db } from "../../../firebase"; // Thêm đường dẫn đúng tới file firebase.js của bạn
import { getDocs, query, where, collection, doc, updateDoc, deleteDoc } from "firebase/firestore";

export default async function handler(req, res) {
    try {
        const usersRef = collection(db, "users");

        // Lấy danh sách người dùng
        if (req.method === "GET") {
            const querySnapshot = await getDocs(usersRef);
            const users = querySnapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));
            return res.status(200).json(users);
        }

        // Xóa người dùng
        if (req.method === "DELETE") {
            const { id } = req.body;
            if (!id) return res.status(400).json({ message: "User ID is required." });

            const userDoc = doc(usersRef, id);
            await deleteDoc(userDoc);
            return res.status(200).json({ message: "User deleted successfully." });
        }

        // Cập nhật vai trò người dùng
        if (req.method === "PUT") {
            const { id, role } = req.body;
            if (!id || !role) return res.status(400).json({ message: "User ID and role are required." });

            const userDoc = doc(usersRef, id);
            await updateDoc(userDoc, { role });
            return res.status(200).json({ message: "User role updated successfully." });
        }

        res.setHeader("Allow", ["GET", "DELETE", "PUT"]);
        return res.status(405).end(`Method ${req.method} Not Allowed`);
    } catch (error) {
        console.error("Error managing users:", error);
        return res.status(500).json({ message: "Internal server error." });
    }
}
