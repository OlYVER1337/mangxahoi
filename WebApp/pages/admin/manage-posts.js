import { useState, useEffect } from "react";
import styles from "./ManagePosts.module.css";

const ManagePosts = ({ currentUser }) => {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [isEditing, setIsEditing] = useState(false);
    const [editingPost, setEditingPost] = useState(null);
    const [selectedComments, setSelectedComments] = useState([]); // State for comments
    const [selectedPostId, setSelectedPostId] = useState(null); // State for the selected post

    const hasEditPermission =
        currentUser?.role === "Admin" || currentUser?.actions?.includes("edit_posts");

    const fetchPosts = async () => {
        try {
            setLoading(true);
            const response = await fetch("/api/admin/post");
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

    const fetchComments = async (postId) => {
        try {
            const response = await fetch(`/api/admin/post?action=comments&postId=${postId}`);
            if (!response.ok) {
                throw new Error("Failed to fetch comments.");
            }
            const comments = await response.json();
            return comments;
        } catch (error) {
            console.error("Error fetching comments:", error);
            return [];
        }
    };

    const handlePostClick = async (postId) => {
        setSelectedPostId(postId);
        const comments = await fetchComments(postId);
        setSelectedComments(comments);
    };

    const handleSavePost = async (post) => {
        try {
            const method = post.id ? "PUT" : "POST";
            const response = await fetch(`/api/admin/post`, {
                method,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(post),
            });
            if (!response.ok) throw new Error("Failed to save post.");
            alert(post.id ? "Post updated successfully!" : "Post added successfully!");
            setIsEditing(false);
            setEditingPost(null);
            fetchPosts();
        } catch (err) {
            console.error("Error:", err);
            alert("Failed to save post.");
        }
    };

    const handleDeletePost = async (postId) => {
        const confirm = window.confirm("Are you sure you want to delete this post?");
        if (confirm) {
            try {
                const response = await fetch(`/api/admin/posts/${postId}`, {
                    method: "DELETE",
                });
                if (!response.ok) throw new Error("Failed to delete post.");
                alert("Post deleted successfully!");
                fetchPosts();
            } catch (err) {
                console.error("Error:", err);
                alert("Failed to delete post.");
            }
        }
    };

    useEffect(() => {
        fetchPosts();
    }, []);

    return (
        <div className={styles["manage-posts-container"]}>
            <h1 className={styles.title}>Post Management</h1>

            {loading && <p className={styles.loading}>Loading posts...</p>}
            {error && <p className={styles.error}>{error}</p>}

            <table className={styles["post-table"]}>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Content</th>
                        <th>Image</th>
                        <th>Posted By</th>
                        <th>Timestamp</th>
                        {hasEditPermission && <th>Manage</th>}
                    </tr>
                </thead>
                <tbody>
                    {posts.map((post, idx) => (
                        <tr
                            key={post.id}
                            onClick={() => handlePostClick(post.id)}
                            className={styles["clickable-row"]}
                        >
                            <td>{idx + 1}</td>
                            <td>{post.content}</td>
                            <td>
                                {post.postImage ? (
                                    <img
                                        src={post.postImage}
                                        alt="Post"
                                        className={styles["post-image"]}
                                    />
                                ) : (
                                    "No Image"
                                )}
                            </td>
                            <td>{post.userName}</td>
                            <td>
                                {post.postTimestamp
                                    ? new Date(post.postTimestamp.seconds * 1000 + post.postTimestamp.nanoseconds / 1e6).toLocaleString()
                                    : "Unknown Time"}
                            </td>
                            {hasEditPermission && (
                                <td>
                                    <button
                                        className={styles["edit-btn"]}
                                        onClick={() => console.log("Edit Clicked")}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        className={styles["delete-btn"]}
                                        onClick={() => handleDeletePost(post.id)}
                                    >
                                        Delete
                                    </button>
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>

            {selectedPostId && (
                <div className={styles["comments-section"]}>
                    <h2>Comments for Post {selectedPostId}</h2>
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
                                                comment.timestamp.seconds * 1000 + comment.timestamp.nanoseconds / 1e6
                                            ).toLocaleString()
                                            : "Unknown Time"}
                                    </span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>No comments found for this post.</p>
                    )}
                </div>
            )}
        </div>
    );
};

export default ManagePosts;
