// pages/api/admin/login.js

import { db } from "../../../firebase";
import { getDocs, query, where, collection } from "firebase/firestore";

export default async function handler(req, res) {
    if (req.method === "POST") {
        const { email, password } = req.body;

        try {
            // Truy vấn người dùng từ Firestore dựa trên email
            const usersRef = collection(db, "users");
            const q = query(usersRef, where("email", "==", email));
            const querySnapshot = await getDocs(q);

            if (querySnapshot.empty) {
                return res.status(401).json({ message: "Invalid email or password." });
            }

            const user = querySnapshot.docs[0].data();

            // Kiểm tra mật khẩu và role
            if (user.password === password && (user.role === "admin" || user.role === "editor")) {
                // Lưu thông tin người dùng vào sessionStorage hoặc localStorage ở phía client
                return res.status(200).json({
                    success: true,
                    message: "Login successful.",
                    role: user.role,
                    name: user.name,
                    email: user.email,
                    image: user.image
                });
            } else {
                return res.status(403).json({
                    message: "Invalid credentials or insufficient permissions."
                });
            }
        } catch (error) {
            console.error("Error during login:", error);
            return res.status(500).json({ message: "Internal server error." });
        }
    } else {
        res.setHeader("Allow", ["POST"]);
        return res.status(405).end(`Method ${req.method} Not Allowed`);
    }
}
