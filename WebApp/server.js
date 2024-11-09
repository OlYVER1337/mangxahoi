const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const cors = require('cors'); // Import cors module

// Cấu hình Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json'); // Đảm bảo thay đổi đường dẫn chính xác
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId: 'easychat-7788b',  // Thay thế bằng project ID của bạn
});

const db = admin.firestore();
const app = express();
const PORT = 5000;

// Middleware để phân tích JSON và CORS
app.use(cors()); // Sử dụng CORS cho phép tất cả các yêu cầu từ mọi nguồn
app.use(bodyParser.json());

// Các route khác
app.get('/api/posts', async (req, res) => {
    try {
        const snapshot = await db.collection('posts').orderBy('createdAt', 'desc').get();
        const posts = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        res.status(200).json(posts);
    } catch (error) {
        res.status(500).json({ error: 'Error fetching posts', details: error });
    }
});

// Đăng ký người dùng
app.post('/api/register', async (req, res) => {
    const { username, email, password } = req.body;

    try {
        // Kiểm tra xem người dùng đã tồn tại chưa
        const userRef = db.collection('users').doc(email);
        const doc = await userRef.get();
        if (doc.exists) {
            return res.status(400).json({ message: 'User already exists!' });
        }

        // Nếu chưa có người dùng, tạo mới
        await userRef.set({
            username,
            email,
            password,
        });

        res.status(201).json({ message: 'User registered successfully!' });
    } catch (error) {
        res.status(500).json({ error: 'Error registering user', details: error });
    }
});

app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;

    try {
        // Truy vấn tất cả người dùng trong Firestore mà có email khớp với giá trị từ client
        const usersCollection = db.collection('users');
        const snapshot = await usersCollection.where('email', '==', email).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'User not found' });
        }

        // Lấy thông tin người dùng từ snapshot (có thể có duy nhất 1 user với email này)
        const user = snapshot.docs[0].data(); // Lấy thông tin người dùng
        const userId = snapshot.docs[0].id; // Document ID của người dùng

        // Kiểm tra mật khẩu
        if (user.password !== password) {
            return res.status(401).json({ message: 'Incorrect password' });
        }

        // Đăng nhập thành công, trả về thông tin người dùng
        res.status(200).json({
            message: 'Login successful',
            user: {
                id: userId,
                name: user.name,
                email: user.email
            }
        });
    } catch (error) {
        res.status(500).json({ error: 'Error logging in', details: error });
    }
});





// Bắt đầu server
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
