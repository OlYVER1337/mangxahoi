import React, { useEffect, useState } from 'react';

function MainPage() {
    const [posts, setPosts] = useState([]);

    useEffect(() => {
        // Gọi API để lấy danh sách bài viết
        fetch('http://localhost:5000/api/posts')
            .then(response => response.json())
            .then(data => setPosts(data))
            .catch(error => console.error('Error fetching posts:', error));
    }, []);

    return (
        <div>
            <h1>Bài viết</h1>
            <ul>
                {posts.map(post => (
                    <li key={post.id}>
                        <h2>{post.title}</h2>
                        <p>{post.content}</p>
                        <small>{new Date(post.createdAt.seconds * 1000).toLocaleString()}</small>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default MainPage;
