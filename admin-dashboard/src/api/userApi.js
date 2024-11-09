// src/api/userApi.js
import { useRef } from 'react';
import { db } from '../firebaseConfig';
import { collection, getDocs, addDoc, doc, deleteDoc, updateDoc, query, where } from 'firebase/firestore';


// Lấy danh sách người dùng
export const getUsers = async () => {
    const usersCollection = collection(db, 'users'); // Tên collection là 'users'
    const userSnapshot = await getDocs(usersCollection);
    const userList = userSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    return userList;
};

// Thêm người dùng
export const addUser = async (user) => {
    const usersCollection = collection(db, 'users'); // Tên collection là 'users'
    const docRef = await addDoc(usersCollection, user);
    return docRef.id; // Trả về ID của người dùng vừa thêm
};

// Xóa người dùng
export const deleteUser = async (userId) => {
    const userDoc = doc(db, 'users', userId); // Tên collection là 'users'
    await deleteDoc(userDoc);
};
// Thêm quyền admin cho người dùng
export const makeAdmin = async (userId) => {
    const userRef = doc(db, "users", userId);
    await updateDoc(userRef, { isAdmin: true });
};
//Tước quyền admin 
export const revokeAdmin = async (userId) => {
    const userRef = doc(db, "users", userId);
    await updateDoc(userRef, { isAdmin: false });
};
//Cập nhật thông tin người dùng
export const updateUser = async (userId, updatedUser) => {
    const userRef = doc(db, "users", userId);
    await updateDoc(userRef, updatedUser);
};
//Cập nhật mật khẩu cho người dùng
export const updatePassword = async (userId, newPassword) => {
    const userDoc = doc(db, 'users', userId);
    await updateDoc(userDoc, { password: newPassword });
};
export const getUserByEmail = async (email) => {
    const userQuery = query(collection(db, "users"), where("email", "==", email));
    const querySnapshot = await getDocs(userQuery);
    return querySnapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }))[0];
};