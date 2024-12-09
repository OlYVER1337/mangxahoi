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
        // Xử lý thông tin JWT
        async jwt({ token, user, account }) {
            if (user) {
                token.id = user.id;
                token.name = user.name;
                token.email = user.email;
                token.image = user.image || null;
                token.bio = user.bio || null;
            }
            return token;
        },
        // Xử lý thông tin Session
        async session({ session, token }) {
            session.user = {
                id: token.id,
                name: token.name,
                email: token.email,
                image: token.image,
                bio: token.bio,
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
