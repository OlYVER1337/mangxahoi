import { useState, useEffect, useRef } from 'react';
import { useSession } from 'next-auth/react';
import { db, storage } from '../firebase'; // Đảm bảo bạn đã cấu hình Firestore và Storage
import { collection, getDocs, query, where, orderBy, addDoc, serverTimestamp, onSnapshot, updateDoc, doc } from 'firebase/firestore';
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
    const messagesEndRef = useRef(null); // Ref để cuộn xuống cuối danh sách tin nhắn

    // Hàm cuộn xuống cuối danh sách tin nhắn
    const scrollToBottom = () => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    };

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
    const handleConversationClick = (conversation) => {
        let selectedUser;

        // Kiểm tra người dùng hiện tại là người gửi hay người nhận trong cuộc trò chuyện
        if (conversation.senderID === currentUserId) {
            selectedUser = conversation.receiverID;
        } else if (conversation.receiverID === currentUserId) {
            selectedUser = conversation.senderID;
        }

        setSelectedUserId(selectedUser); // Lưu ID của người được chọn

        // Lắng nghe tin nhắn giữa hai người dùng từ Firestore theo thời gian thực
        const chatRef = collection(db, 'chat');
        const chatQuery = query(
            chatRef,
            where('senderID', 'in', [currentUserId, selectedUser]),
            where('receiverID', 'in', [currentUserId, selectedUser]),
            orderBy('timestamp', 'asc')
        );

        // Bắt đầu listener Firestore
        const unsubscribe = onSnapshot(chatQuery, (snapshot) => {
            const chatMessages = snapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));

            setMessages(chatMessages); // Cập nhật danh sách tin nhắn
            scrollToBottom(); // Cuộn xuống cuối khi nhận tin nhắn mới
        });

        // Dọn dẹp listener khi người dùng đổi cuộc hội thoại hoặc component bị unmount
        return () => unsubscribe();
    };

    // Gọi scrollToBottom mỗi khi danh sách tin nhắn thay đổi
    useEffect(() => {
        scrollToBottom();
    }, [messages]);

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

            // Tìm docID của cuộc hội thoại hiện tại
            const conversationDoc = conversations.find(
                (c) =>
                    (c.senderID === currentUserId && c.receiverID === selectedUserId) ||
                    (c.receiverID === currentUserId && c.senderID === selectedUserId)
            );

            if (conversationDoc?.id) {
                const conversationRef = doc(db, 'conversations', conversationDoc.id);
                await updateDoc(conversationRef, {
                    lastMessage: newMessage,
                    lastUpdated: serverTimestamp(), // Cập nhật thời gian cuối cùng
                });
            }

            setNewMessage(''); // Xóa tin nhắn sau khi gửi
        } catch (error) {
            console.error('Lỗi khi gửi tin nhắn hoặc cập nhật lastMessage:', error);
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
        } catch (error) {
            console.error('Lỗi khi tải file:', error);
        }
    };

    if (loading) {
        return <div>Loading conversations...</div>;
    }

    return (
        <div className="flex h-screen">
            {/* Sidebar Conversations */}
            <div className="w-1/4 border-r p-4 overflow-y-auto">
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

            {/* Main Chat Area */}
            <div className="w-1/2 flex flex-col h-full">
                <div className="flex-1 overflow-y-auto p-4">
                    <h2 className="text-xl font-semibold mb-4">Messages</h2>
                    {selectedUserId ? (
                        <ul>
                            {messages.map((message) => (
                                <li
                                    key={message.id}
                                    className={`flex items-center mb-4 ${message.senderID === currentUserId ? 'justify-end' : 'justify-start'
                                        }`}
                                >
                                    {message.senderID !== currentUserId && (
                                        <img
                                            src={conversations.find(c => c.senderID === message.senderID)?.senderImage || ''}
                                            alt="Avatar"
                                            className="w-8 h-8 rounded-full mr-2"
                                        />
                                    )}
                                    <div
                                        className={`p-2 rounded-lg max-w-xs break-words ${message.senderID === currentUserId
                                                ? 'bg-blue-500 text-white'
                                                : 'bg-blue-100 text-black'
                                            }`}
                                    >
                                        {message.type === 'media' ? (
                                            message.file.includes('video') ? (
                                                <video
                                                    controls
                                                    className="max-w-[200px] max-h-[200px] rounded"
                                                    src={message.file}
                                                />
                                            ) : (
                                                <img
                                                    src={message.file}
                                                    alt="Media"
                                                    className="max-w-[200px] max-h-[200px] rounded"
                                                />
                                            )
                                        ) : (
                                            <p>{message.message}</p>
                                        )}
                                    </div>
                                </li>
                            ))}
                            <div ref={messagesEndRef} />
                        </ul>
                    ) : (
                        <div className="text-center">Select a conversation to view messages</div>
                    )}
                </div>

                {/* Thanh nhập tin nhắn */}
                <form onSubmit={handleSendMessage} className="flex items-center p-4 border-t bg-white">
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
            </div>

            {/* Right Sidebar (Settings Placeholder) */}
            <div className="w-1/4 border-l p-4">
                <h2 className="text-xl font-semibold mb-4">Settings</h2>
                <div className="text-gray-500">Settings area placeholder. You can add components here later.</div>
            </div>
        </div>

    );
};

export default Messenger;
