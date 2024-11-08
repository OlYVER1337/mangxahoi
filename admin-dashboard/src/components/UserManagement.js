// src/components/UserManagement.js
import React, { useEffect, useState } from 'react';
import { Table, TableBody, TableCell, TableHead, TableRow, Paper, Button, TextField, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { getUsers, makeAdmin, revokeAdmin, deleteUser, updateUser, updatePassword } from '../api/userApi'; // Cập nhật import
import AddUserForm from './AddUserForm';

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [newPassword, setNewPassword] = useState(''); // Thêm state cho mật khẩu mới

    useEffect(() => {
        const fetchUsers = async () => {
            const userList = await getUsers();
            setUsers(userList);
        };
        fetchUsers();
    }, []);

    const handleDelete = async (userId) => {
        await deleteUser(userId);
        setUsers(users.filter(user => user.id !== userId));
    };

    const handleMakeAdmin = async (userId) => {
        await makeAdmin(userId);
        setUsers(users.map(user => user.id === userId ? { ...user, isAdmin: true } : user));
    };

    const handleRevokeAdmin = async (userId) => {
        await revokeAdmin(userId);
        setUsers(users.map(user => user.id === userId ? { ...user, isAdmin: false } : user));
    };

    const handleUserAdded = (newUser) => {
        setUsers([...users, newUser]);
    };

    const openEditDialog = (user) => {
        setEditingUser(user);
        setNewPassword(''); // Đặt mật khẩu mới trống khi mở dialog
        setEditDialogOpen(true);
    };

    const closeEditDialog = () => {
        setEditDialogOpen(false);
        setEditingUser(null);
        setNewPassword(''); // Đặt mật khẩu mới trống khi đóng dialog
    };

    const handleEditChange = (e) => {
        setEditingUser({ ...editingUser, [e.target.name]: e.target.value });
    };

    const handleEditSave = async () => {
        if (editingUser) {
            // Cập nhật thông tin người dùng
            await updateUser(editingUser.id, { name: editingUser.name, email: editingUser.email });

            // Cập nhật mật khẩu nếu có
            if (newPassword) {
                await updatePassword(editingUser.id, newPassword);
            }

            setUsers(users.map(user => user.id === editingUser.id ? editingUser : user));
        }
        closeEditDialog();
    };

    return (
        <div>
            <AddUserForm onUserAdded={handleUserAdded} />
            <Paper>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Tên</TableCell>
                            <TableCell>Email</TableCell>
                            <TableCell>Admin</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell>{user.id}</TableCell>
                                <TableCell>{user.name}</TableCell>
                                <TableCell>{user.email}</TableCell>
                                <TableCell>{user.isAdmin ? "Yes" : "No"}</TableCell>
                                <TableCell>
                                    {user.isAdmin ? (
                                        <Button onClick={() => handleRevokeAdmin(user.id)} color="warning">
                                            Tước quyền Admin
                                        </Button>
                                    ) : (
                                        <Button onClick={() => handleMakeAdmin(user.id)} color="primary">
                                            Thêm Admin
                                        </Button>
                                    )}
                                    <Button onClick={() => openEditDialog(user)} color="info">
                                        Sửa
                                    </Button>
                                    <Button onClick={() => handleDelete(user.id)} color="secondary">
                                        Xóa
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Paper>

            {/* Edit User Dialog */}
            <Dialog open={editDialogOpen} onClose={closeEditDialog}>
                <DialogTitle>Chỉnh sửa thông tin người dùng</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Tên"
                        name="name"
                        value={editingUser?.name || ''}
                        onChange={handleEditChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        label="Email"
                        name="email"
                        value={editingUser?.email || ''}
                        onChange={handleEditChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        label="Mật khẩu mới" // Thêm trường nhập cho mật khẩu mới
                        type="password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        fullWidth
                        margin="dense"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeEditDialog} color="secondary">
                        Hủy
                    </Button>
                    <Button onClick={handleEditSave} color="primary">
                        Lưu
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
};

export default UserList;
