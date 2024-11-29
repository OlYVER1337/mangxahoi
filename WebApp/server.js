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

app.post('/api/getMessages', async (req, res) => {
    try {
        const { currentUserId, selectedUserId } = req.body;

        if (!currentUserId || !selectedUserId) {
            return res.status(400).json({ error: 'currentUserId và selectedUserId là bắt buộc' });
        }

        const chatRef = db.collection('chat');
        const querySnapshot = await chatRef
            .where('senderID', 'in', [currentUserId, selectedUserId])
            .where('receiverID', 'in', [currentUserId, selectedUserId])
            .orderBy('timestamp', 'asc') // Sắp xếp theo thời gian
            .get();

        const messages = querySnapshot.docs
            .filter(doc => {
                const data = doc.data();
                return (
                    (data.senderID === currentUserId && data.receiverID === selectedUserId) ||
                    (data.senderID === selectedUserId && data.receiverID === currentUserId)
                );
            })
            .map(doc => ({
                id: doc.id,
                ...doc.data(),
            }));

        return res.json({ messages });
    } catch (error) {
        console.error('Lỗi khi lấy tin nhắn:', error);
        res.status(500).json({ error: 'Lỗi khi lấy tin nhắn' });
    }
});

// Gửi tin nhắn mới
app.post('/api/sendMessage', async (req, res) => {
    try {
        const { senderID, receiverID, message, type, file } = req.body; // Nhận các tham số gửi từ frontend

        // Kiểm tra nếu tin nhắn là file (ảnh/video)
        let fileUrl = null;
        if (type === 'media' && file) {
            // Upload file lên Firebase Storage (giả sử file là base64 hoặc file URL)
            const buffer = Buffer.from(file, 'base64');
            const fileName = `media/${Date.now()}.jpg`; // Đặt tên cho file (có thể là ảnh hoặc video)
            const fileUpload = storage.bucket().file(fileName);

            await fileUpload.save(buffer, {
                metadata: { contentType: 'image/jpeg' }, // Thay đổi loại file nếu cần
                public: true,
            });

            // Lấy URL công khai của file đã tải lên
            fileUrl = `https://storage.googleapis.com/${fileUpload.bucket.name}/${fileUpload.name}`;
        }

        // Tạo đối tượng tin nhắn mới
        const newMessage = {
            senderID,
            receiverID,
            message,
            type,
            file: fileUrl, // Nếu có file, lưu URL file vào đây
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
        };

        // Lưu tin nhắn vào Firestore (collection 'chat')
        const messageRef = await db.collection('chat').add(newMessage);

        // Cập nhật tin nhắn cuối cùng trong cuộc trò chuyện
        const conversationRef = db.collection('conversations');
        const conversationSnapshot = await conversationRef
            .where('senderID', '==', senderID)
            .where('receiverID', '==', receiverID)
            .get();

        if (!conversationSnapshot.empty) {
            // Nếu đã có cuộc trò chuyện, cập nhật tin nhắn cuối cùng
            conversationSnapshot.forEach(doc => {
                doc.ref.update({
                    lastMessage: message,
                    timestamp: admin.firestore.FieldValue.serverTimestamp(),
                });
            });
        }

        // Trả về thông báo thành công
        res.status(200).json({ success: true, message: 'Tin nhắn đã được gửi thành công!', messageId: messageRef.id });
    } catch (error) {
        console.error('Lỗi khi gửi tin nhắn:', error);
        res.status(500).json({ success: false, error: 'Đã xảy ra lỗi khi gửi tin nhắn' });
    }
});




app.get('/api/users', async (req, res) => {
    const { email } = req.query;

    if (!email) {
        return res.status(400).json({ error: 'Email is required' });
    }

    try {
        const snapshot = await db.collection('users').where('email', '==', email).get();

        if (snapshot.empty) {
            return res.status(404).json({ error: 'User not found' });
        }

        let userId;
        snapshot.forEach((doc) => {
            userId = doc.id; // Lấy userId từ document ID
        });

        res.status(200).json({ userId });
    } catch (error) {
        console.error('Error fetching user by email:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});


app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ message: 'Email và mật khẩu là bắt buộc.' });
    }

    try {
        // Truy vấn Firestore để tìm user theo email
        const snapshot = await db.collection('users').where('email', '==', email).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'Không tìm thấy người dùng với email này.' });
        }

        const userDoc = snapshot.docs[0];
        const user = userDoc.data();

        // Kiểm tra mật khẩu
        if (user.password !== password) {
            return res.status(401).json({ message: 'Mật khẩu không đúng.' });
        }

        // Trả về thông tin người dùng
        return res.status(200).json({
            message: 'Đăng nhập thành công.',
            user: {
                id: userDoc.id,
                name: user.name,
                email: user.email,
                image: user.image || null,
            },
        });
    } catch (error) {
        console.error('Lỗi trong quá trình đăng nhập:', error);
        return res.status(500).json({ error: 'Lỗi trong quá trình đăng nhập.', details: error.message });
    }
});


module.exports = router;
// Bắt đầu server
app.listen(5000, () => console.log("Server started on port 5000"));