import { db } from "../../../firebase";
import { getDocs, query, where, collection, doc, updateDoc, deleteDoc } from "firebase/firestore";

export default async function handler(req, res) {
    try {
        const usersRef = collection(db, "users");
        if (req.method === "GET") {
            const querySnapshot = await getDocs(usersRef);
            const users = querySnapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }));
            return res.status(200).json(users);
        }
        if (req.method === "PUT") {
            const { id, role, actions } = req.body;
            if (!id || !role || !Array.isArray(actions)) {
                return res.status(400).json({ message: "User ID, role, and actions are required." });
            }
            const userDoc = doc(usersRef, id);
            await updateDoc(userDoc, { role, actions });
            return res.status(200).json({ message: "User role and permissions updated successfully." });
        }
        if (req.method === "DELETE") {
            const { id } = req.body;
            if (!id) return res.status(400).json({ message: "User ID is required." });
            const userDoc = doc(usersRef, id);
            await deleteDoc(userDoc);
            return res.status(200).json({ message: "User deleted successfully." });
        }
        res.setHeader("Allow", ["GET", "PUT", "DELETE"]);
        res.status(405).end(`Method ${req.method} Not Allowed`);
    } catch (error) {
        console.error("Error managing users:", error);
        res.status(500).json({ message: "Internal server error." });
    }
}
