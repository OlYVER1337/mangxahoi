import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { db } from '../firebase'; // Đảm bảo bạn đã import db từ firebase config
import { collection, getDocs, query, where, orderBy } from 'firebase/firestore';

// Hàm để lấy doc.id của người dùng từ email
const getUserIdByEmail = async (email) => {
    const usersRef = collection(db, 'users');
    const userQuery = query(usersRef, where('email', '==', email));
    const userSnapshot = await getDocs(userQuery);

    if (!userSnapshot.empty) {
        const userDoc = userSnapshot.docs[0]; // Lấy doc đầu tiên trong kết quả
        return userDoc.id; // Trả về doc.id của người dùng
    } else {
        throw new Error('User not found');
    }
};

const Messenger = () => {
    const { data: session } = useSession(); // Lấy session để lấy thông tin người dùng
    const [conversations, setConversations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedUserId, setSelectedUserId] = useState(null); // State để lưu ID người được chọn
    const [messages, setMessages] = useState([]); // State để lưu tin nhắn
    const [currentUserId, setCurrentUserId] = useState(null); // Lưu ID của người dùng hiện tại

    // Lấy danh sách cuộc hội thoại
    useEffect(() => {
        const fetchConversations = async () => {
            if (session?.user?.email) {
                try {
                    const userId = await getUserIdByEmail(session.user.email);
                    setCurrentUserId(userId); // Lưu ID của người dùng hiện tại
                    const response = await fetch(`http://localhost:5000/api/conversations?userId=${userId}`);
                    const data = await response.json();
                    setConversations(data);
                } catch (error) {
                    console.error('Lỗi khi lấy cuộc trò chuyện:', error);
                } finally {
                    setLoading(false);
                }
            }
        };

        fetchConversations();
    }, [session?.user?.email]);

    // Hàm xử lý khi nhấn vào một cuộc hội thoại
    const handleConversationClick = async (conversation) => {
        try {
            let selectedUser;

            // Kiểm tra người dùng hiện tại là người gửi hay người nhận trong cuộc trò chuyện
            if (conversation.senderID === currentUserId) {
                selectedUser = conversation.receiverID; // Nếu là người gửi, lấy người nhận
            } else if (conversation.receiverID === currentUserId) {
                selectedUser = conversation.senderID; // Nếu là người nhận, lấy người gửi
            }

            setSelectedUserId(selectedUser); // Lưu ID của người được chọn

            // Lấy tin nhắn giữa hai người dùng từ Firestore
            const chatRef = collection(db, 'chat');
            const chatQuery = query(
                chatRef,
                where('senderID', 'in', [currentUserId, selectedUser]),
                where('receiverID', 'in', [currentUserId, selectedUser]),
                orderBy('timestamp', 'asc') // Sắp xếp theo thời gian
            );
            const chatSnapshot = await getDocs(chatQuery);
            const chatMessages = chatSnapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));

            setMessages(chatMessages); // Lưu danh sách tin nhắn
        } catch (error) {
            console.error("Lỗi khi lấy tin nhắn:", error);
        }
    };

    if (loading) {
        return <div>Loading conversations...</div>;
    }

    return (
        <div className="flex h-screen">
            {/* Sidebar chứa danh sách cuộc hội thoại */}
            <div className="w-1/3 border-r p-4 overflow-y-auto">
                <h2 className="text-xl font-semibold mb-4">Conversations</h2>
                <ul>
                    {conversations.map((conversation) => {
                        const isCurrentUserSender = conversation.senderID === currentUserId;

                        // Chọn ảnh tùy thuộc vào người gửi/nhận
                        const image = isCurrentUserSender
                            ? conversation.receiverImage
                            : conversation.senderImage;

                        return (
                            <li
                                key={conversation.id}
                                className="flex items-center space-x-4 mb-4 cursor-pointer"
                                onClick={() => handleConversationClick(conversation)}
                            >
                                <img src={image} alt="Avatar" className="w-12 h-12 rounded-full" />
                                <div>
                                    <p className="font-semibold">
                                        {isCurrentUserSender
                                            ? conversation.receiverName
                                            : conversation.senderName}
                                    </p>
                                    <p>{conversation.lastMessage}</p>
                                </div>
                            </li>
                        );
                    })}
                </ul>
            </div>

            {/* Khu vực hiển thị tin nhắn */}
            <div className="w-2/3 p-4 overflow-y-auto">
                <h2 className="text-xl font-semibold mb-4">Messages</h2>
                {selectedUserId ? (
                    <ul>
                        {messages.map((message) => (
                            <li
                                key={message.id}
                                className={`mb-4 p-2 rounded-lg ${
                                    message.senderID === currentUserId
                                        ? 'bg-blue-100 text-right'
                                        : 'bg-gray-100 text-left'
                                }`}
                            >
                                {message.type === 'media' ? (
                                    <img
                                        src={message.file}
                                        alt="Media"
                                        className="max-w-full rounded"
                                    />
                                ) : (
                                    <p>{message.message}</p>
                                )}
                                <span className="text-xs text-gray-500">
                                    {new Date(message.timestamp?.seconds * 1000).toLocaleString()}
                                </span>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p>Select a conversation to view messages</p>
                )}
            </div>
        </div>
    );
};

export default Messenger;
