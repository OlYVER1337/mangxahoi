import jwt from 'jsonwebtoken';

export default async function handler(req, res) {
    if (req.method === 'POST') {
        const token = req.headers['authorization']?.split(' ')[1]; // Lấy token từ header

        if (!token) {
            return res.status(401).json({ message: 'Không có token' });
        }

        try {
            // Xác thực token với JWT
            const decoded = jwt.verify(token, process.env.JWT_SECRET); // Kiểm tra token hợp lệ

            res.status(200).json({ success: true, user: decoded });
        } catch (error) {
            // Nếu token không hợp lệ, trả về lỗi
            console.error('Lỗi xác thực token:', error);
            res.status(401).json({ message: 'Token không hợp lệ' });
        }
    } else {
        res.status(405).json({ message: 'Method not allowed' });
    }
}
