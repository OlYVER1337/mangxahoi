// server.js
const express = require('express');
const app = express();
const cors = require('cors');
const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const firebaseAdmin = require('firebase-admin');

// Firebase config
const serviceAccount = require('./path-to-your-firebase-adminsdk.json');
firebaseAdmin.initializeApp({
    credential: firebaseAdmin.credential.cert(serviceAccount)
});
const db = getFirestore();

app.use(cors());
app.use(express.json());

// Endpoint đăng nhập
app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const usersRef = db.collection('users');
        const snapshot = await usersRef.where('email', '==', email).get();

        if (snapshot.empty) {
            return res.status(400).json({ message: 'Email không tồn tại' });
        }

        let isAdmin = false;
        let validCredentials = false;

        snapshot.forEach(doc => {
            const userData = doc.data();
            if (userData.password === password) {
                validCredentials = true;
                isAdmin = userData.isAdmin || false;
            }
        });

        if (validCredentials) {
            res.json({ isAdmin });
        } else {
            res.status(400).json({ message: 'Sai mật khẩu' });
        }
    } catch (error) {
        console.error('Lỗi khi xác thực người dùng:', error);
        res.status(500).json({ message: 'Lỗi máy chủ' });
    }
});

// Endpoint lấy số lượng người dùng
app.get('/api/user-count', async (req, res) => {
    try {
        const usersRef = db.collection('users');
        const snapshot = await usersRef.get();
        res.json({ userCount: snapshot.size });
    } catch (error) {
        console.error('Lỗi khi lấy số lượng người dùng:', error);
        res.status(500).json({ message: 'Lỗi máy chủ' });
    }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
