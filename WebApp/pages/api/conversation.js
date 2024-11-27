import { db } from '../../firebase'; // Kết nối Firebase Firestore

export default async function handler(req, res) {
    const { userId } = req.query;

    if (!userId) {
        return res.status(400).json({ error: 'User ID is required' });
    }

    try {
        const conversationsRef = db.collection('conversations');
        const sentQuery = conversationsRef.where('senderId', '==', userId).get();
        const receivedQuery = conversationsRef.where('receiverId', '==', userId).get();

        const [sentSnapshots, receivedSnapshots] = await Promise.all([sentQuery, receivedQuery]);

        const conversations = [];

        sentSnapshots.forEach((doc) => {
            conversations.push({ id: doc.id, ...doc.data() });
        });

        receivedSnapshots.forEach((doc) => {
            conversations.push({ id: doc.id, ...doc.data() });
        });

        console.log('Conversations:', conversations); // Kiểm tra dữ liệu trả về từ Firestore
        res.status(200).json(conversations);
    } catch (error) {
        console.error('Error fetching conversations:', error);
        res.status(500).json({ error: 'Failed to fetch conversations.' });
    }
}
