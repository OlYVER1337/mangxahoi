import { db } from "../../../firebase";
import {
    collection,
    doc,
    getDocs,
    getDoc,
    addDoc,
    updateDoc,
    deleteDoc,
    query,
    orderBy,
} from "firebase/firestore";

export default async function handler(req, res) {
    try {
        const postsRef = collection(db, "posts");

        // Handle GET posts
        if (req.method === "GET" && !req.query.action) {
            const querySnapshot = await getDocs(query(postsRef, orderBy("postTimestamp", "desc")));
            const posts = querySnapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));
            return res.status(200).json(posts);
        }

        // Handle POST a new post
        if (req.method === "POST") {
            const { content, postImage, userId, userName, userImage } = req.body;

            if (!content || !userId || !userName || !userImage) {
                return res.status(400).json({
                    message: "Content, userId, userName, and userImage are required.",
                });
            }

            const newPost = {
                content,
                postImage: postImage || "",
                userId,
                userName,
                userImage,
                likedBy: [],
                postTimestamp: new Date(),
            };

            const docRef = await addDoc(postsRef, newPost);
            return res.status(201).json({ id: docRef.id, ...newPost });
        }

        // Handle PUT (update post)
        if (req.method === "PUT") {
            const { id, content, postImage } = req.body;

            if (!id || !content) {
                return res.status(400).json({
                    message: "Post ID and updated content are required.",
                });
            }

            const postDoc = doc(postsRef, id);
            await updateDoc(postDoc, {
                content,
                postImage: postImage || "",
                postTimestamp: new Date(),
            });

            return res.status(200).json({ message: "Post updated successfully." });
        }

        // Handle DELETE (delete post)
        if (req.method === "DELETE") {
            const { id } = req.body;

            if (!id) {
                return res.status(400).json({
                    message: "Post ID is required.",
                });
            }

            const postDoc = doc(postsRef, id);
            await deleteDoc(postDoc);

            return res.status(200).json({ message: "Post deleted successfully." });
        }

        // Handle comments API
        if (req.query.action === "comments") {
            const { postId } = req.query;

            if (!postId) {
                return res.status(400).json({ message: "Post ID is required for comments." });
            }

            const commentsRef = collection(db, `posts/${postId}/comments`);

            if (req.method === "GET") {
                // Fetch comments for a specific post
                const querySnapshot = await getDocs(query(commentsRef, orderBy("timestamp", "desc")));
                const comments = querySnapshot.docs.map((doc) => ({
                    id: doc.id,
                    ...doc.data(),
                }));
                return res.status(200).json(comments);
            }

            if (req.method === "POST") {
                const { userId, userName, content } = req.body;

                if (!userId || !userName || !content) {
                    return res.status(400).json({ message: "Missing required comment fields." });
                }

                const newComment = {
                    userId,
                    userName,
                    content,
                    timestamp: new Date(),
                };

                const docRef = await addDoc(commentsRef, newComment);
                return res.status(201).json({ id: docRef.id, ...newComment });
            }

            if (req.method === "PUT") {
                const { commentId, content } = req.body;

                if (!commentId || !content) {
                    return res.status(400).json({ message: "Comment ID and updated content are required." });
                }

                const commentDoc = doc(commentsRef, commentId);
                await updateDoc(commentDoc, {
                    content,
                    timestamp: new Date(),
                });

                return res.status(200).json({ message: "Comment updated successfully." });
            }

            if (req.method === "DELETE") {
                const { commentId } = req.body;

                if (!commentId) {
                    return res.status(400).json({ message: "Comment ID is required." });
                }

                const commentDoc = doc(commentsRef, commentId);
                await deleteDoc(commentDoc);

                return res.status(200).json({ message: "Comment deleted successfully." });
            }
        }

        res.setHeader("Allow", ["GET", "POST", "PUT", "DELETE"]);
        res.status(405).end(`Method ${req.method} Not Allowed`);
    } catch (error) {
        console.error("Error managing posts/comments:", error);
        res.status(500).json({ message: "Internal server error." });
    }
}
