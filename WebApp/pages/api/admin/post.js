import { db } from "../../../firebase";
import { getDocs, query, where, collection, doc, updateDoc, deleteDoc, orderBy } from "firebase/firestore";

export default async function handler(req, res) {
    const { method } = req;

    try {
        if (method === "GET") {
            // Lấy danh sách bài viết
            const postsRef = collection(db, "posts");
            const q = query(postsRef, orderBy("postTimestamp", "desc"));
            const snapshot = await getDocs(q);

            const posts = snapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));

            res.status(200).json(posts);

        } else if (method === "POST") {
            // Thêm bài viết mới
            const { content, postImage, userId, userName, userImage } = req.body;

            if (!content || !userId || !userName || !userImage) {
                return res.status(400).json({ error: "Missing required fields." });
            }

            const newPost = {
                content,
                postImage: postImage || null,
                userId,
                userName,
                userImage,
                postTimestamp: new Date(),
                likedBy: [],
                comments: [],
            };

            const docRef = await db.collection("posts").add(newPost);

            res.status(201).json({ message: "Post added successfully.", id: docRef.id });

        } else if (method === "PUT") {
            // Chỉnh sửa bài viết
            const { id, content, postImage } = req.body;

            if (!id || !content) {
                return res.status(400).json({ error: "Missing required fields." });
            }

            const postRef = doc(db, "posts", id);
            await updateDoc(postRef, {
                content,
                postImage: postImage || null,
                postTimestamp: new Date(), // Cập nhật lại thời gian chỉnh sửa
            });

            res.status(200).json({ message: "Post updated successfully." });

        } else if (method === "DELETE") {
            const { id, commentId } = req.body;

            if (!id) {
                return res.status(400).json({ error: "Missing post ID." });
            }

            if (commentId) {
                // Xóa một bình luận trong sub-collection "comments"
                const postRef = doc(db, "posts", id);
                const commentRef = doc(postRef, "comments", commentId);

                // Kiểm tra xem bình luận có tồn tại không
                const commentSnapshot = await getDocs(commentRef);
                if (!commentSnapshot.exists) {
                    return res.status(404).json({ error: "Comment not found." });
                }

                await deleteDoc(commentRef);
                return res.status(200).json({ message: "Comment deleted successfully." });
            } else {
                // Xóa bài viết và tất cả bình luận (nếu không có commentId)
                const postRef = doc(db, "posts", id);

                // Xóa bài viết
                await deleteDoc(postRef);

                // Xóa tất cả các bình luận liên quan trong sub-collection
                const commentsRef = collection(postRef, "comments");
                const commentsSnapshot = await getDocs(commentsRef);
                commentsSnapshot.forEach((doc) => deleteDoc(doc.ref));

                res.status(200).json({ message: "Post deleted successfully." });
            }
        } else {
            res.status(405).json({ error: "Method not allowed." });
        }
    } catch (error) {
        console.error("Error handling posts:", error);
        res.status(500).json({ error: "An error occurred." });
    }
}
