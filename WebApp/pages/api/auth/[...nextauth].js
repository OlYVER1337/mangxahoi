import NextAuth from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";
import GoogleProvider from "next-auth/providers/google";
import { db } from "../../../firebase";
import { addDoc, collection, query, where, getDocs } from "firebase/firestore";

export const authOptions = {
    providers: [
        GoogleProvider({
            clientId: process.env.GOOGLE_CLIENT_ID,
            clientSecret: process.env.GOOGLE_CLIENT_SECRET,
        }),
        CredentialsProvider({
            name: "Credentials",
            credentials: {
                email: { label: "Email", type: "email" },
                password: { label: "Password", type: "password" },
            },
            async authorize(credentials) {
                try {
                    const res = await fetch("http://localhost:5000/api/login", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        body: JSON.stringify({
                            email: credentials.email,
                            password: credentials.password,
                        }),
                    });
                    const user = await res.json();
                    if (res.ok && user) {
                        return {
                            id: user.user.id,
                            name: user.user.name,
                            email: user.user.email,
                            image: user.user.image || null,
                        };
                    }
                } catch (error) {
                    console.error("Error authorizing user:", error);
                }
                return null; // Nếu đăng nhập thất bại
            },
        }),
    ],
    callbacks: {
        async jwt({ token, user, account }) {
            if (account?.provider === "google" && user) {
                const usersRef = collection(db, "users");
                // Kiểm tra người dùng đã tồn tại chưa
                const q = query(usersRef, where("email", "==", user.email));
                const querySnapshot = await getDocs(q);
                if (querySnapshot.empty) {
                    // Thêm mới người dùng nếu chưa tồn tại
                    const docRef = await addDoc(usersRef, {
                        name: user.name,
                        email: user.email,
                        image: user.image || null,
                        createdAt: new Date(),
                    });
                    console.log("User added with ID:", docRef.id);
                    token.id = docRef.id; // Lưu docId vào token
                } else {
                    const existingUser = querySnapshot.docs[0];
                    token.id = existingUser.id; // Lấy docId của người dùng đã tồn tại
                }
                // Ghi thông tin từ Google vào token
                token.name = user.name;
                token.email = user.email;
                token.image = user.image || null;
            } else if (!user) {
                // Trường hợp không có thông tin người dùng, lấy từ Firestore
                const usersRef = collection(db, "users");
                const q = query(usersRef, where("email", "==", token.email));
                const querySnapshot = await getDocs(q);
                if (!querySnapshot.empty) {
                    const userDoc = querySnapshot.docs[0].data();
                    token.name = userDoc.name;
                    token.image = userDoc.image || token.image;
                    token.id = querySnapshot.docs[0].id; // Lấy docId
                }
            }
            return token;
        },
        async session({ session, token }) {
            // Cập nhật thông tin vào session từ token
            session.user = {
                id: token.id,
                name: token.name,
                email: token.email,
                image: token.image,
            };
            return session;
        },
    },
    secret: process.env.NEXTAUTH_SECRET,
    pages: {
        signIn: "/auth/signin", // Tùy chỉnh trang đăng nhập
    },
};

export default NextAuth(authOptions);
