const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const cors = require('cors');
const multer = require('multer');
const fs = require('fs');
const { Storage } = require('@google-cloud/storage'); // Import Storage
const projectId = 'easychat-7788b';

// Cấu hình Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId: 'easychat-7788b',
});

const db = admin.firestore();
const storage = new Storage();
const bucket = storage.bucket('easychat-7788b.appspot.com');

const app = express();
app.use(cors());
app.use(bodyParser.json());

// Tạo multer để xử lý ảnh tải lên
const upload = multer({ dest: 'uploads/' });

// API lấy danh sách bài viết
app.get('/api/posts', async (req, res) => {
    try {
        const snapshot = await db.collection('posts').orderBy('createdAt', 'desc').get();
        const posts = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        res.status(200).json(posts);
    } catch (error) {
        res.status(500).json({ error: 'Error fetching posts', details: error });
    }
});

app.post('/api/signup', async (req, res) => {
    const { email, password, name } = req.body;

    // Kiểm tra các giá trị nhận được
    if (!email || !password || !name) {
        return res.status(400).json({ message: 'Vui lòng điền đầy đủ thông tin' });
    }

    try {
        // Tạo người dùng trong Firestore
        const userRef = admin.firestore().collection('users').doc(email);
        const userData = {
            email,
            password,  // Cần mã hóa mật khẩu trước khi lưu vào Firestore
            name,
        };

        await userRef.set(userData);
        res.status(200).json({ message: 'Đăng ký thành công' });
    } catch (error) {
        console.error('Lỗi khi đăng ký:', error);
        res.status(500).json({ message: 'Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại' });
    }
});

// API đăng nhập người dùng
app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;

    try {
        const snapshot = await db.collection('users').where('email', '==', email).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'User not found' });
        }

        const user = snapshot.docs[0].data();
        const userId = snapshot.docs[0].id;

        // Kiểm tra mật khẩu
        if (user.password !== password) {
            return res.status(401).json({ message: 'Incorrect password' });
        }

        res.status(200).json({
            message: 'Login successful',
            user: {
                id: userId,
                name: user.name,
                email: user.email,
                image: user.image
            }
        });
    } catch (error) {
        res.status(500).json({ error: 'Error logging in', details: error });
    }
});

// Bắt đầu server
app.listen(5000, () => console.log("Server started on port 5000"));
