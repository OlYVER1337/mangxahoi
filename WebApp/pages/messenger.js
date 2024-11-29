import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { db, storage } from '../firebase'; // Đảm bảo bạn đã cấu hình Firestore và Storage
import { collection, getDocs, query, where, orderBy, addDoc, serverTimestamp } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { getUserIdByEmail } from '../utils/utils'; 

const Messenger = () => {
    const { data: session } = useSession(); // Lấy session để lấy thông tin người dùng
    const [conversations, setConversations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedUserId, setSelectedUserId] = useState(null); // State để lưu ID người được chọn
    const [messages, setMessages] = useState([]); // State để lưu tin nhắn
    const [currentUserId, setCurrentUserId] = useState(null); // Lưu ID của người dùng hiện tại
    const [newMessage, setNewMessage] = useState(''); // State để lưu tin nhắn mới

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

    // Hàm gửi tin nhắn mới (văn bản)
    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!newMessage.trim()) return; // Không gửi tin nhắn trống

        const messageData = {
            senderID: currentUserId,
            receiverID: selectedUserId,
            message: newMessage,
            timestamp: serverTimestamp(),
            type: 'text',
        };

        try {
            // Thêm tin nhắn vào Firestore
            await addDoc(collection(db, 'chat'), messageData);

            // Cập nhật danh sách tin nhắn sau khi gửi
            setMessages((prevMessages) => [
                ...prevMessages,
                { ...messageData, id: new Date().getTime() }, // Thêm tin nhắn vào cuối
            ]);

            setNewMessage(''); // Xóa tin nhắn sau khi gửi
        } catch (error) {
            console.error('Lỗi khi gửi tin nhắn:', error);
        }
    };

    // Hàm xử lý khi chọn file
    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        try {
            const storageRef = ref(storage, `uploads/${file.name}-${Date.now()}`);
            const snapshot = await uploadBytes(storageRef, file);
            const downloadURL = await getDownloadURL(snapshot.ref);

            // Tạo tin nhắn cho file
            const messageData = {
                senderID: currentUserId,
                receiverID: selectedUserId,
                message: '', // Để trống nếu là file
                file: downloadURL,
                timestamp: serverTimestamp(),
                type: 'media',
            };

            // Lưu vào Firestore
            await addDoc(collection(db, 'chat'), messageData);

            // Cập nhật danh sách tin nhắn
            setMessages((prevMessages) => [
                ...prevMessages,
                { ...messageData, id: new Date().getTime() },
            ]);
        } catch (error) {
            console.error('Lỗi khi tải file:', error);
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
                    <>
                        <ul>
                            {messages.map((message) => (
                                <li
                                    key={message.id}
                                    className={`mb-4 p-2 rounded-lg ${message.senderID === currentUserId
                                        ? 'bg-blue-100 text-right ml-auto'
                                        : 'bg-gray-100 text-left mr-auto'
                                        }`}
                                >
                                    {message.type === 'media' ? (
                                        message.file.includes('video') ? (
                                            <video
                                                controls
                                                className="max-w-full rounded"
                                                src={message.file}
                                            />
                                        ) : (
                                            <img
                                                src={message.file}
                                                alt="Media"
                                                className="max-w-full rounded"
                                            />
                                        )
                                    ) : (
                                        <p>{message.message}</p>
                                    )}
                                    <span className="text-xs text-gray-500">
                                        {new Date(message.timestamp?.seconds * 1000).toLocaleString()}
                                    </span>
                                </li>
                            ))}
                        </ul>

                        {/* Thanh nhập tin nhắn */}
                        <form onSubmit={handleSendMessage} className="flex mt-4">
                            <input
                                type="text"
                                value={newMessage}
                                onChange={(e) => setNewMessage(e.target.value)}
                                placeholder="Enter message"
                                className="flex-1 p-2 border rounded-l"
                            />
                            <input
                                type="file"
                                id="fileInput"
                                accept="image/*,video/*"
                                onChange={(e) => handleFileChange(e)}
                                className="hidden"
                            />
                            <label
                                htmlFor="fileInput"
                                className="p-2 bg-gray-300 text-white rounded-l cursor-pointer"
                            >
                                📎
                            </label>
                            <button
                                type="submit"
                                className="p-2 bg-blue-500 text-white rounded-r"
                            >
                                Send
                            </button>
                        </form>
                    </>
                ) : (
                    <div className="text-center">Select a conversation to view messages</div>
                )}
            </div>
        </div>
    );
};

export default Messenger;
