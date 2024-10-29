// src/api/userApi.js
import { db } from '../firebaseConfig';
import { collection, getDocs, addDoc, doc, deleteDoc } from 'firebase/firestore';

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
