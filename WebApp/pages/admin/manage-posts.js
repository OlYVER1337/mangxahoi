import { useEffect, useState } from "react";
import styles from "./ManagePosts.module.css";
import { useRouter } from "next/router";

const ManagePosts = () => {
    const router = useRouter();
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [editingPost, setEditingPost] = useState(null);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deletePostId, setDeletePostId] = useState(null);
    const [selectedPostId, setSelectedPostId] = useState(null);
    const [selectedComments, setSelectedComments] = useState([]);


    // Fetch danh sách bài viết từ backend
    const fetchPosts = async () => {
        try {
            setLoading(true);
            const response = await fetch("/api/admin/posts");
            if (!response.ok) throw new Error("Failed to fetch posts.");
            const data = await response.json();
            setPosts(data);
        } catch (err) {
            console.error("Error:", err);
            setError("Could not load posts.");
        } finally {
            setLoading(false);
        }
    };
    const handlePostClick = async (postId) => {
        setSelectedPostId(postId);
        const response = await fetch(`/api/admin/posts?action=comments&postId=${postId}`);
        const comments = await response.json();
        setSelectedComments(comments);
    };
    // Xóa bài viết từ backend
    const handleDeletePost = async (postId) => {
        try {
            console.log("Attempting to delete post with ID:", postId);

            const response = await fetch("/api/admin/posts", {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ id: postId.toString() }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message);
            }

            const data = await response.json();
            alert(data.message);

            // Cập nhật danh sách bài viết sau khi xóa thành công
            setPosts((prevPosts) => prevPosts.filter((post) => post.id !== postId));
            setShowDeleteModal(false); // Ẩn modal sau khi xóa thành công
        } catch (error) {
            console.error("Error:", error);
            alert("Failed to delete post.");
        }
    };

    // Hiện modal xác nhận delete
    const confirmDelete = (postId) => {
        setDeletePostId(postId);
        setShowDeleteModal(true);
    };

    const handleCancelDelete = () => {
        setShowDeleteModal(false);
    };

    useEffect(() => {
        fetchPosts();
    }, []);

    return (
        <div className={styles["manage-posts-container"]}>
            <h1 className={styles.title}>Manage Posts</h1>
            {loading && <p className={styles.loading}>Loading posts...</p>}
            {error && <p className={styles.error}>{error}</p>}

            <table className={styles["post-table"]}>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Content</th>
                        <th>Image</th>
                        <th>Author</th>
                        <th>Timestamp</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {posts.map((post, index) => (
                        <tr
                            key={post.id}
                            onClick={() => handlePostClick(post.id)}
                            className={styles["clickable-row"]}
                        >
                            <td>{index + 1}</td>
                            <td>{post.content}</td>
                            <td>
                                {post.postImage && (
                                    <img src={post.postImage} alt="Post" className={styles["post-image"]} />
                                )}
                            </td>
                            <td>{post.userName}</td>
                            <td className={styles.timestamp}>
                                {new Date(post.postTimestamp).toLocaleString()}
                            </td>
                            <td>
                                <button onClick={() => confirmDelete(post.id)} className={styles["delete-btn"]}>
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {showDeleteModal && (
                <div className={styles.modal}>
                    <div className={styles["modal-content"]}>
                        <p>Are you sure you want to delete this post?</p>
                        <button onClick={() => handleDeletePost(deletePostId)} className={styles["edit-btn"]}>
                            Confirm
                        </button>
                        <button onClick={handleCancelDelete} className={styles["delete-btn"]}>
                            Cancel
                        </button>
                    </div>
                </div>
            )}

            {selectedPostId && (
                <div className={styles["comments-section"]}>
                    <h2>Bình luận cho bài viết {selectedPostId}</h2>
                    {selectedComments.length > 0 ? (
                        <ul className={styles["comments-list"]}>
                            {selectedComments.map((comment) => (
                                <li key={comment.id}>
                                    <p>
                                        <strong>{comment.userName}</strong>: {comment.content}
                                    </p>
                                    <span className={styles["timestamp"]}>
                                        {comment.timestamp
                                            ? new Date(
                                                comment.timestamp.seconds * 1000 +
                                                (comment.timestamp.nanoseconds || 0) / 1e6
                                            ).toLocaleString()
                                            : "Không xác định"}
                                    </span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>Không tìm thấy bình luận cho bài viết này.</p>
                    )}
                </div>
            )}
        </div>
    );
};

export default ManagePosts;
