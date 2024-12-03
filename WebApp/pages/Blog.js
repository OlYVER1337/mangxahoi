import React, { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import Post from "../components/Post";
import Navbar from "../components/Navbar";
import RightSidebar from "../components/RightSidebar";
import WhatsOnYourMind from "../components/WhatOnYourMind";
import { collection, onSnapshot, query, orderBy, where } from "firebase/firestore";
import { db } from "../firebase";

const Blog = () => {
  const { data: session } = useSession();
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    if (session) {
      const q = query(
        collection(db, "posts"),
        where("userId", "==", session.user.id),
        orderBy("postTimestamp", "desc") // Sắp xếp theo ngày đăng mới nhất
      );
      const unsubscribe = onSnapshot(q, (snapshot) => {
        setPosts(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
      });

      return () => unsubscribe();
    } else {
      setPosts([]);
    }
  }, [session]);

  return (
    <main>
      <Navbar />
      <RightSidebar />
      <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
        <div className="bg-white shadow-lg rounded-lg p-6 w-full max-w-[600px]">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <img src={session?.user?.image} alt="dp" className="w-12 h-12 rounded-full" />
              <h1 className="ml-4 text-xl font-semibold">{session?.user?.name}</h1>
            </div>
            <a href="EditProfile" className="bg-primary text-white py-2 px-4 rounded-lg mt-4">
              Edit Profile
            </a>
          </div>
        </div>
        <div className="flex flex-col w-full max-w-[600px]">
          <WhatsOnYourMind />
        </div>
        <div className="flex flex-col w-full max-w-[600px]">
          {posts.map((post) => (
            <Post key={post.id} data={post} id={post.id} className="mb-4" />
          ))}
        </div>
      </div>
    </main>
  );
};

export default Blog;