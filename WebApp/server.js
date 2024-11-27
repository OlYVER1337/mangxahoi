const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const cors = require('cors');
const multer = require('multer');
const fs = require('fs');
const { Storage } = require('@google-cloud/storage'); // Import Storage
const projectId = 'easychat-7788b';
const router = express.Router();



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
        const userRef = admin.firestore().collection('users').doc();
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
// Lấy danh sách các cuộc trò chuyện của user hiện tại
// Trong server.js
app.get('/api/conversations', async (req, res) => {
    const { userId } = req.query;

    if (!userId) {
        return res.status(400).json({ error: 'User ID is required' });
    }

    try {
        const conversationsRef = db.collection('conversations');
        const sentQuery = conversationsRef.where('senderID', '==', userId).get();
        const receivedQuery = conversationsRef.where('receiverID', '==', userId).get();

        const [sentSnapshots, receivedSnapshots] = await Promise.all([sentQuery, receivedQuery]);

        const conversations = [];

        sentSnapshots.forEach((doc) => {
            conversations.push({ id: doc.id, ...doc.data() });
        });

        receivedSnapshots.forEach((doc) => {
            conversations.push({ id: doc.id, ...doc.data() });
        });

        console.log("Conversations:", conversations); // Kiểm tra dữ liệu trả về
        res.status(200).json(conversations);
    } catch (error) {
        console.error('Error fetching conversations:', error);
        res.status(500).json({ error: 'Failed to fetch conversations.' });
    }
});


// Lấy tin nhắn từ một cuộc trò chuyện cụ thể
router.get('/messages', async (req, res) => {
    const { conversationId } = req.query;

    try {
        const messagesRef = db.collection('chat').doc(conversationId).collection('messages');
        const messagesSnapshot = await messagesRef.orderBy('timestamp').get();

        const messages = [];
        messagesSnapshot.forEach((doc) => {
            messages.push({ id: doc.id, ...doc.data() });
        });

        res.status(200).json({ messages });
    } catch (error) {
        console.error('Error fetching messages:', error);
        res.status(500).json({ error: 'Failed to fetch messages.' });
    }
});

// Gửi tin nhắn mới
router.post('/messages', async (req, res) => {
    const { conversationId, senderID, receiverID, message, type, timestamp } = req.body;

    try {
        const messagesRef = db.collection('chat').doc(conversationId).collection('messages');
        const newMessage = { senderID, receiverID, message, type, timestamp };

        await messagesRef.add(newMessage);

        res.status(201).json({ message: 'Message sent successfully.' });
    } catch (error) {
        console.error('Error sending message:', error);
        res.status(500).json({ error: 'Failed to send message.' });
    }
});


// Thêm API để lấy userId dựa trên email
router.get('/api/user-id', async (req, res) => {
    const { email } = req.query;

    if (!email) {
        return res.status(400).json({ error: 'Email is required' });
    }

    try {
        const userRef = db.collection('users');
        const snapshot = await userRef.where('email', '==', email).get();

        if (snapshot.empty) {
            return res.status(404).json({ error: 'User not found' });
        }

        const userDoc = snapshot.docs[0];  // Lấy document đầu tiên nếu có
        res.status(200).json({ userId: userDoc.id });  // Trả về userId của người dùng
    } catch (error) {
        console.error('Error fetching userId:', error);
        res.status(500).json({ error: 'Failed to fetch userId' });
    }
});



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

        // Tạo JWT
        const token = jwt.sign(
            { id: userId, name: user.name, email: user.email },
            SECRET_KEY,
            { expiresIn: '1h' } // Thời gian hết hạn
        );

        res.status(200).json({
            message: 'Login successful',
            token, // Trả token về cho frontend
            user: {
                id: userId,
                name: user.name,
                email: user.email,
                image: user.image || null,
            },
        });
    } catch (error) {
        res.status(500).json({ error: 'Error logging in', details: error });
    }
});

module.exports = router;
// Bắt đầu server
app.listen(5000, () => console.log("Server started on port 5000"));

