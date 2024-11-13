const admin = require('firebase-admin');
const path = require('path');

// Khởi tạo Firebase Admin SDK
const serviceAccount = require('./key.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    storageBucket: 'easychat-7788b.appspot.com'
});

// Lấy tham chiếu đến Firebase Storage
const bucket = admin.storage().bucket();

// Đường dẫn đến tệp ảnh cần upload
const filePath = path.join("C:\\Users\\Admin\\Pictures", 'hinh.jpg');  // Sử dụng \\ hoặc / cho Windows
const destination = 'uploads/hinh.jpg'; // Đường dẫn lưu trữ trên Firebase Storage

async function uploadFile() {
    try {
        await bucket.upload(filePath, {
            destination: destination,
            metadata: {
                contentType: 'image/jpeg', // Định dạng nội dung của ảnh
            }
        });
        console.log('Tải lên thành công');
    } catch (error) {
        console.error('Lỗi khi tải lên:', error);
    }
}

uploadFile();
