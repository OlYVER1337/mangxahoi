import React, { useEffect, useState } from "react";
import { useSession, signIn } from "next-auth/react";
import Post from "../components/Post";
import Navbar from "../components/Navbar";
import RightSidebar from "../components/RightSidebar";
import WhatsOnYourMind from "../components/WhatOnYourMind";
import Login from "../components/Login";
import { collection, onSnapshot, query, orderBy, where } from "firebase/firestore";
import { db } from "../firebase";
import UserProfile from "../components/UserProfile";

const Blog = () => {
    const { data: session, update } = useSession(); // Add update function
    const [posts, setPosts] = useState([]);

    if (!session) return <Login />;

    useEffect(() => {
        if (session) {
            const q = query(
                collection(db, "posts"),
                where("userId", "==", session.user.id),
                orderBy("postTimestamp", "desc")
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
        <main className="min-h-screen bg-gray-100">
            <Navbar />
            <div className="flex justify-center gap-4 px-4 mt-4">
                <div className="w-full max-w-[600px]">
                    <UserProfile session={session} update={update} />
                    <div className="mt-4">
                        <WhatsOnYourMind />
                    </div>
                    <div className="mt-4 space-y-4">
                        {posts.map((post) => (
                            <Post 
                                key={post.id}
                                data={post}
                                id={post.id}
                                userName={session.user.name}
                            />
                        ))}
                    </div>
                </div>
                <div className="hidden lg:block">
                    <RightSidebar />
                </div>
            </div>
        </main>
    );
};

export default Blog;