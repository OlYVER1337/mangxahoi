import React, { useState } from 'react';
import { signIn } from 'next-auth/react';
import { auth, provider } from '../firebase';
import { signInWithPopup } from 'firebase/auth';
import { useRouter } from 'next/router';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const router = useRouter();

    const handleGoogleSignIn = async () => {
        try {
            const result = await signInWithPopup(auth, provider);
            const user = result.user;

            await signIn('credentials', {
                email: user.email,
                password: 'google_auth',
                redirect: false,
            });

            router.push('/');
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEmailSignIn = async (e) => {
        e.preventDefault();
        try {
            const res = await signIn('credentials', {
                email,
                password,
                redirect: false,
            });
            if (res.ok) {
                router.push('/');
            } else {
                throw new Error('Email hoặc mật khẩu không đúng');
            }
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '0 auto', textAlign: 'center' }}>
            <h2>Đăng nhập</h2>
            <form onSubmit={handleEmailSignIn}>
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    style={{ display: 'block', width: '100%', marginBottom: '10px' }}
                />
                <input
                    type="password"
                    placeholder="Mật khẩu"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{ display: 'block', width: '100%', marginBottom: '10px' }}
                />
                <button type="submit" style={{ display: 'block', width: '100%' }}>
                    Đăng nhập
                </button>
            </form>
            <button onClick={handleGoogleSignIn} style={{ display: 'block', width: '100%', marginTop: '10px' }}>
                Đăng nhập bằng Google
            </button>
            {error && <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>}
        </div>
    );
}
