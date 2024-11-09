import React, { useState } from 'react';
import { signIn } from 'next-auth/react';
import { FaFacebook, FaGoogle } from 'react-icons/fa';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();

        const response = await fetch('http://localhost:5000/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();

        if (response.ok) {
            alert('Login successful!');
            console.log('Token:', data.token); // Bạn có thể lưu token để sử dụng sau này
        } else {
            setErrorMessage(data.message || 'Login failed');
        }
    };

    const handleGoogleLogin = () => {
        signIn('google')
            .then((response) => {
                console.log('Google Login successful', response);
                // Bạn có thể gửi ID Token của Google tới API của mình để xác thực
            })
            .catch((error) => {
                console.error('Google Login failed', error);
            });
    };

    return (
        <div className="grid grid-cols-2">
            <div className="bg-primary h-screen grid place-items-center">
                <FaFacebook className="text-white text-[200px]" />
            </div>

            <div className="grid place-items-center bg-black">
                <form onSubmit={handleLogin} className="bg-white p-6 rounded-md shadow-md">
                    <h2 className="text-2xl mb-4">Đăng Nhập</h2>
                    {errorMessage && <p className="text-red-500">{errorMessage}</p>}
                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full p-2 border rounded"
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Mật khẩu</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full p-2 border rounded"
                            required
                        />
                    </div>
                    <button type="submit" className="w-full bg-primary text-white p-2 rounded">
                        Đăng Nhập
                    </button>
                </form>

                <div className="mt-4 flex gap-4">
                    <div
                        className="flex gap-2 bg-[#fff] p-2 items-center rounded-[6px] cursor-pointer"
                        onClick={handleGoogleLogin}
                    >
                        <FaGoogle className="text-[30px]" />
                        Đăng nhập với Google
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
