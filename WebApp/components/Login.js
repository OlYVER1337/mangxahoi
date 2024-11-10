import React, { useState } from 'react';
import { signIn } from 'next-auth/react';
import { FaGoogle } from 'react-icons/fa';
import { useRouter } from 'next/router';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const router = useRouter();

    const handleEmailLogin = async (e) => {
        e.preventDefault();
        const response = await signIn('credentials', {
            redirect: false,
            email,
            password,
        });

        if (response?.ok) {
            router.push('/');
        } else {
            setErrorMessage('Email hoặc mật khẩu không đúng');
        }
    };

    const handleGoogleLogin = () => {
        signIn('google', { callbackUrl: '/' })
            .then((response) => {
                console.log('Google Login successful', response);
            })
            .catch((error) => {
                console.error('Google Login failed', error);
            });
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center">
            <div className="bg-white shadow-lg rounded-lg overflow-hidden w-full max-w-md">
                <div className="bg-primary py-6 text-center text-white font-bold text-2xl">
                    Đăng Nhập
                </div>
                <div className="p-6">
                    {errorMessage && <p className="text-red-500 mb-4 text-center">{errorMessage}</p>}
                    <form onSubmit={handleEmailLogin}>
                        <div className="mb-4">
                            <label className="block text-gray-700 font-medium mb-2">Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full p-3 border border-gray-300 rounded focus:outline-none focus:border-primary"
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-gray-700 font-medium mb-2">Mật khẩu</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full p-3 border border-gray-300 rounded focus:outline-none focus:border-primary"
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            className="w-full bg-primary hover:bg-primary-dark text-white font-medium p-3 rounded transition duration-200"
                        >
                            Đăng Nhập
                        </button>
                    </form>

                    <div className="my-6 flex items-center">
                        <hr className="flex-grow border-gray-300" />
                        <span className="px-4 text-gray-500">Hoặc</span>
                        <hr className="flex-grow border-gray-300" />
                    </div>

                    <div
                        onClick={handleGoogleLogin}
                        className="flex justify-center items-center bg-red-600 text-white font-medium p-3 rounded cursor-pointer hover:bg-red-700 transition duration-200"
                    >
                        <FaGoogle className="mr-2" />
                        Đăng nhập với Google
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
