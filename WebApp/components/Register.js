// register.js
import React, { useState } from 'react';
import axios from 'axios';
import { useRouter } from 'next/router';

const Register = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [image, setImage] = useState(null);
    const router = useRouter();

    const handleImageChange = (e) => {
        setImage(e.target.files[0]);
    };

    const handleRegister = async () => {
        const formData = new FormData();
        formData.append('name', name);
        formData.append('email', email);
        formData.append('password', password);
        formData.append('image', image);

        try {
            const response = await axios.post('/api/signup', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            alert(response.data.message);
            router.push('/login');  // Điều hướng trở lại trang đăng nhập
        } catch (error) {
            console.error(error);
            alert('Đăng ký thất bại');
        }
    };

    return (
        <div>
            <h2>Đăng ký</h2>
            <input type="text" placeholder="Tên" onChange={(e) => setName(e.target.value)} />
            <input type="email" placeholder="Email" onChange={(e) => setEmail(e.target.value)} />
            <input type="password" placeholder="Mật khẩu" onChange={(e) => setPassword(e.target.value)} />
            <input type="file" onChange={handleImageChange} />
            <button onClick={handleRegister}>Đăng ký</button>
        </div>
    );
};

export default Register;
