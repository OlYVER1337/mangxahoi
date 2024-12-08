import React, { useState, useEffect } from "react";

function ManagePosts() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);

    // Fetch bài viết từ API
    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const response = await fetch("/api/posts", {
                    method: "GET",
                });
                const data = await response.json();

                if (response.ok) {
                    setPosts(data);
                } else {
                    console.error("Failed to fetch posts");
                }
            } catch (error) {
                console.error("Error fetching posts:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPosts();
    }, []);

    const handleDeletePost = async (id) => {
        const confirmation = window.confirm("Are you sure you want to delete this post?");
        if (confirmation) {
            try {
                await fetch(`/api/posts`, {
                    method: "DELETE",
                    body: JSON.stringify({ id }),
                    headers: {
                        "Content-Type": "application/json",
                    },
                });
                setPosts(posts.filter(post => post.id !== id));
            } catch (error) {
                console.error("Error deleting post:", error);
            }
        }
    };

    if (loading) {
        return <div>Loading posts...</div>;
    }

    return (
        <div>
            <h1>Manage Posts</h1>
            {posts.length === 0 ? (
                <p>No posts available.</p>
            ) : (
                posts.map((post) => (
                    <div key={post.id}>
                        <h3>{post.userName}</h3>
                        <p>{post.content}</p>
                        {post.postImage && <img src={post.postImage} alt="Post" />}
                        <p>Posted on: {new Date(post.postTimestamp.seconds * 1000).toLocaleString()}</p>
                        <button onClick={() => handleDeletePost(post.id)}>Delete Post</button>
                    </div>
                ))
            )}
        </div>
    );
}

export default ManagePosts;
