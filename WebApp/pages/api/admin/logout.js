// pages/api/admin/logout.js
import { db } from "../../../firebase";
import { query, where, collection, getDocs, updateDoc, doc } from "firebase/firestore";

export default async function handler(req, res) {
    if (req.method === "POST") {
        const { email } = req.body; // Lấy email của người dùng từ request

        try {
            // Truy vấn người dùng trong Firestore dựa trên email
            const usersRef = collection(db, "users");
            const q = query(usersRef, where("email", "==", email));
            const querySnapshot = await getDocs(q);

            if (querySnapshot.empty) {
                return res.status(404).json({ message: "User not found." });
            }

            const userDoc = querySnapshot.docs[0];
            const userRef = doc(db, "users", userDoc.id);

            // Cập nhật availability = 0
            await updateDoc(userRef, { availability: 0 });

            return res.status(200).json({ success: true, message: "User logged out successfully." });
        } catch (error) {
            console.error("Error during logout:", error);
            return res.status(500).json({ message: "Internal server error." });
        }
    } else {
        res.setHeader("Allow", ["POST"]);
        return res.status(405).end(`Method ${req.method} Not Allowed`);
    }
}
