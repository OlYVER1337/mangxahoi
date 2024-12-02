import React, { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import Post from "../components/Post"; // Đảm bảo đường dẫn này là chính xác
import Navbar from "../components/Navbar";
import { collection, onSnapshot, query, orderBy, where } from "firebase/firestore";
import { db } from "../firebase";

const Blog = () => {
  const { data: session } = useSession();
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    if (session) {
      const q = query(
        collection(db, "posts"),
        where("userId", "==", session.user.id), // Lọc bài đăng theo userId
        orderBy("postTimestamp", "desc")
      );
      const unsubscribe = onSnapshot(q, (snapshot) => {
        setPosts(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
      });

      return () => unsubscribe();
    }
  }, [session]);

  return (
    <main>
      <Navbar />
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white shadow-lg rounded-lg p-6 w-full max-w-[600px]">
        <div className="flex items-center mb-4">
          <img
            className="w-12 h-12 rounded-full"
            src={session?.user?.image}
            alt="dp"
          />
          <h1 className="ml-4 text-xl font-semibold">{session?.user?.name}</h1>
        </div>
        <button className="bg-primary text-white py-2 px-4 rounded-lg mb-4">
          Edit Profile
        </button>
        <div>
          {posts.map((post) => (
            <Post key={post.id} data={post} id={post.id} />
          ))}
        </div>
      </div>
    </div>
    </main>
  );
};

export default Blog;