// AddUserForm.js
import React, { useState } from 'react';
import { TextField, Button } from '@mui/material';
import { addUser } from '../api/userApi';

const AddUserForm = ({ onUserAdded }) => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        const user = { name, email };
        const userId = await addUser(user);
        if (userId) {
            onUserAdded({ id: userId, ...user });
            setName('');
            setEmail('');
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <TextField
                label="Tên"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
            />
            <TextField
                label="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
            />
            <Button type="submit" variant="contained" color="primary">
                Thêm người dùng
            </Button>
        </form>
    );
};

// Đảm bảo xuất component này
export default AddUserForm;
