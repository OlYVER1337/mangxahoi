import { useState, useEffect, useRef } from 'react';
import { useSession } from 'next-auth/react';
import { db, storage } from '../firebase'; // Đảm bảo bạn đã cấu hình Firestore và Storage
import {
    collection,
    query,
    where,
    orderBy,
    addDoc,
    serverTimestamp,
    onSnapshot,
    updateDoc,
    doc,
    deleteDoc,
} from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { getUserIdByEmail } from '../utils/utils';
import Navbar from "../components/Navbar";

const Messenger = () => {
    const { data: session } = useSession();
    const [conversations, setConversations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [messages, setMessages] = useState([]);
    const [currentUserId, setCurrentUserId] = useState(null);
    const [newMessage, setNewMessage] = useState('');
    const messagesEndRef = useRef(null);
    const [modalImage, setModalImage] = useState(null); // State quản lý ảnh hiển thị trong modal
    const [hoveredConversationId, setHoveredConversationId] = useState(null); // State cho hover
    const [menuVisible, setMenuVisible] = useState(null); // State để hiển thị menu ba chấm

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
                    setCurrentUserId(userId);
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

        if (conversation.senderID === currentUserId) {
            selectedUser = conversation.receiverID;
        } else if (conversation.receiverID === currentUserId) {
            selectedUser = conversation.senderID;
        }

        setSelectedUserId(selectedUser);

        const chatRef = collection(db, 'chat');
        const chatQuery = query(
            chatRef,
            where('senderID', 'in', [currentUserId, selectedUser]),
            where('receiverID', 'in', [currentUserId, selectedUser]),
            orderBy('timestamp', 'asc')
        );

        const unsubscribe = onSnapshot(chatQuery, (snapshot) => {
            const chatMessages = snapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));

            setMessages(chatMessages);
            scrollToBottom();
        });

        return () => unsubscribe();
    };

    // Gọi scrollToBottom mỗi khi danh sách tin nhắn thay đổi
    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!newMessage.trim()) return;

        const messageData = {
            senderID: currentUserId,
            receiverID: selectedUserId,
            message: newMessage,
            timestamp: serverTimestamp(),
            type: 'text',
        };

        try {
            await addDoc(collection(db, 'chat'), messageData);

            const conversationDoc = conversations.find(
                (c) =>
                    (c.senderID === currentUserId && c.receiverID === selectedUserId) ||
                    (c.receiverID === currentUserId && c.senderID === selectedUserId)
            );

            if (conversationDoc?.id) {
                const conversationRef = doc(db, 'conversations', conversationDoc.id);
                await updateDoc(conversationRef, {
                    lastMessage: newMessage,
                    lastUpdated: serverTimestamp(),
                });
            }

            setNewMessage('');
        } catch (error) {
            console.error('Lỗi khi gửi tin nhắn hoặc cập nhật lastMessage:', error);
        }
    };

    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        try {
            const storageRef = ref(storage, `uploads/${file.name}-${Date.now()}`);
            const snapshot = await uploadBytes(storageRef, file);
            const downloadURL = await getDownloadURL(snapshot.ref);

            const messageData = {
                senderID: currentUserId,
                receiverID: selectedUserId,
                message: '',
                file: downloadURL,
                timestamp: serverTimestamp(),
                type: 'media',
            };

            await addDoc(collection(db, 'chat'), messageData);
        } catch (error) {
            console.error('Lỗi khi tải file:', error);
        }
    };

    // Hàm mở modal hiển thị ảnh
    const openImageModal = (imageUrl) => {
        setModalImage(imageUrl);
    };

    // Hàm đóng modal
    const closeModal = () => {
        setModalImage(null);
    };

    // Xử lý xóa cuộc hội thoại
    const handleDeleteConversation = async (conversationId) => {
        try {
            // Xóa hội thoại từ Firestore
            await deleteDoc(doc(db, 'conversations', conversationId));
            // Cập nhật danh sách hội thoại
            setConversations((prev) =>
                prev.filter((conversation) => conversation.id !== conversationId)
            );
        } catch (error) {
            console.error('Lỗi khi xóa hội thoại:', error);
        } finally {
            setMenuVisible(null);
        }
    };

    // Lắng nghe sự kiện click bên ngoài để đóng menu
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (
                !event.target.closest('.menu-container') &&
                !event.target.closest('.menu-button')
            ) {
                setMenuVisible(null);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    if (loading) {
        return <div>Loading conversations... </div>;
    }

    return (
        <div className="flex flex-col h-screen">
            {/* Navbar */}
            <Navbar />

            {/* Main Content */}
            <div className="flex flex-grow">
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
                                    className="flex items-center space-x-4 mb-4 cursor-pointer relative group"
                                    onMouseEnter={() => setHoveredConversationId(conversation.id)}
                                    onMouseLeave={() => setHoveredConversationId(null)}
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
                                    {/* Nút ba chấm */}
                                    {hoveredConversationId === conversation.id && (
                                        <button
                                            className="menu-button absolute right-0 p-3 text-gray-500 hover:text-black bg-gray-200 hover:bg-gray-300 rounded-full text-lg"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                setMenuVisible(conversation.id);
                                            }}
                                        >
                                            ...
                                        </button>
                                    )}
                                    {/* Menu xóa */}
                                    {menuVisible === conversation.id && (
                                        <div className="menu-container absolute top-10 right-0 bg-white border rounded shadow p-2">
                                            <button
                                                className="text-red-500 hover:text-red-700"
                                                onClick={() => handleDeleteConversation(conversation.id)}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    )}
                                </li>
                            );
                        })}
                    </ul>
                </div>

                {/* Main Chat Area */}
                <div className="w-1/2 flex flex-col h-full">
                    {/* Nội dung chat */}
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
                                        <div
                                            className={`p-2 rounded-lg max-w-xs break-words ${message.senderID === currentUserId
                                                ? 'bg-blue-500 text-white'
                                                : 'bg-blue-100 text-black'
                                                }`}
                                        >
                                            {message.type === 'media' && message.file ? (
                                                <img
                                                    src={message.file}
                                                    alt="Media"
                                                    className="max-w-[200px] max-h-[200px] rounded cursor-pointer"
                                                    onClick={() => openImageModal(message.file)}
                                                />
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

            {/* Modal hiển thị ảnh */}
            {modalImage && (
                <div
                    className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
                    onClick={closeModal}
                >
                    <div
                        className="bg-white p-4 rounded-lg"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <img src={modalImage} alt="Modal" className="max-w-full max-h-[80vh] mb-4 rounded-lg" />
                        <a
                            href={modalImage}
                            download
                            className="p-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                        >
                            Download
                        </a>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Messenger;
