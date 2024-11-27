import NextAuth from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";
import GoogleProvider from "next-auth/providers/google";

export const authOptions = {
    providers: [
        // Đăng nhập với Google
        GoogleProvider({
            clientId: process.env.GOOGLE_CLIENT_ID,
            clientSecret: process.env.GOOGLE_CLIENT_SECRET,
        }),
        // Đăng nhập với email và mật khẩu
        CredentialsProvider({
            name: "Credentials",
            credentials: {
                email: { label: "Email", type: "email" },
                password: { label: "Password", type: "password" },
            },
            async authorize(credentials) {
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
                        id: user.id,
                        name: user.name,
                        email: user.email,
                        image: user.image || null,
                    };
                }
                return null; // Nếu đăng nhập thất bại
            },
        }),
    ],
    callbacks: {
        async session({ session, user }) {
            if (user) {
                session.user = {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    image: user.image || null,
                };
            }
            return session;
        },
    },
    secret: process.env.NEXTAUTH_SECRET,
    pages: {
        signIn: "/auth/signin", // Tùy chỉnh trang đăng nhập
    },
};

export default NextAuth(authOptions);
