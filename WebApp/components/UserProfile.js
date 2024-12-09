import { useState, useEffect } from 'react';
import { PiNotePencil } from 'react-icons/pi';
import { doc, updateDoc, onSnapshot, query, collection, where, getDocs, writeBatch } from 'firebase/firestore';
import { db } from '../firebase';
import { useUser } from './UserContext';

const UserProfile = ({ session, update }) => {
    const { setUser } = useUser();
    const [isEditing, setIsEditing] = useState(false);
    const [newName, setNewName] = useState(session?.user?.name);
    const [isBioEditing, setIsBioEditing] = useState(false);
    const [newBio, setNewBio] = useState(session?.user?.bio || '');
    const [isSaving, setIsSaving] = useState(false); // Trạng thái lưu dữ liệu

    useEffect(() => {
        if (session && session.user) {
            const unsubscribe = onSnapshot(doc(db, "users", session.user.id), (doc) => {
                const userData = doc.data();
                if (userData) {
                    setNewName(userData.name); // Update local state with new name
                    setNewBio(userData.bio || ''); // Update local state with new bio
                }
            });

            return () => unsubscribe(); // Clean up the listener on unmount
        }
    }, [session]);

    const handleNameChange = (newName) => {
        setNewName(newName);
        setUser({ ...session.user, name: newName });
    };

    const handleSaveChanges = async () => {
        setIsSaving(true);
        const updates = {};

        if (newName.trim() && newName !== session.user.name) {
            updates.name = newName;
            handleNameChange(newName);
            
            // Cập nhật tên trong tất cả các bài viết của người dùng
            const postsQuery = query(
                collection(db, "posts"),
                where("userId", "==", session.user.id)
            );

            const postsSnapshot = await getDocs(postsQuery);
            const batch = writeBatch(db); // Sử dụng batch để thực hiện nhiều cập nhật

            postsSnapshot.forEach((doc) => {
                batch.update(doc.ref, { userName: newName }); // Cập nhật tên người dùng trong bài viết
            });

            await batch.commit(); // Thực hiện tất cả các cập nhật
        }
        
        if (newBio.trim() !== session.user.bio) {
            updates.bio = newBio;
        }

        if (Object.keys(updates).length > 0) {
            await updateDoc(doc(db, "users", session.user.id), updates);
            await update({ user: { ...session.user, ...updates } });
        }

        setIsSaving(false);
        setIsEditing(false);
        setIsBioEditing(false);
    };

    return (
        <div className="bg-white shadow-lg rounded-lg p-6 w-full max-w-[600px]">
            <div className="flex flex-col space-y-4">
                {/* Phần avatar và tên */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center">
                        <img 
                            src={session?.user?.image} 
                            alt="dp" 
                            className="w-16 h-16 rounded-full object-cover"
                        />
                        <div className="ml-4">
                            {isEditing ? (
                                <input
                                    type="text"
                                    value={newName}
                                    onChange={(e) => setNewName(e.target.value)}
                                    onBlur={handleSaveChanges}
                                    className="text-xl font-semibold border-b focus:outline-none focus:border-primary"
                                    autoFocus
                                />
                            ) : (
                                <div className="flex items-center">
                                    <h1 className="text-xl font-semibold">{newName}</h1>
                                    <PiNotePencil
                                        className="ml-2 cursor-pointer text-gray-600 hover:text-primary"
                                        onClick={() => setIsEditing(true)}
                                    />
                                </div>
                            )}
                            <p className="text-gray-500">{session?.user?.email}</p>
                        </div>
                    </div>
                </div>

                {/* Phần Bio */}
                <div className="mt-4">
                    <div className="flex items-center mb-2">
                        <h2 className="font-semibold">Giới thiệu</h2>
                        <PiNotePencil
                            className="ml-2 cursor-pointer text-gray-600 hover:text-primary"
                            onClick={() => setIsBioEditing(true)}
                        />
                    </div>
                    {isBioEditing ? (
                        <textarea
                            value={newBio}
                            onChange={(e) => setNewBio(e.target.value)}
                            onBlur={handleSaveChanges}
                            className="w-full p-2 border rounded-lg focus:outline-none focus:border-primary"
                            rows="3"
                            autoFocus
                        />
                    ) : (
                        <p className="text-gray-700">{newBio || 'Thêm giới thiệu về bản thân...'}</p>
                    )}
                </div>

                {/* Các nút thao tác */}
                <div className="flex justify-end space-x-3 mt-4">
                    <a 
                        href="/change-password" 
                        className="bg-gray-100 text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-200 transition"
                    >
                        Đổi mật khẩu
                    </a>
                    <button 
                        className={`py-2 px-4 rounded-lg transition ${isSaving ? 'bg-gray-300 text-gray-500' : 'bg-primary text-white hover:bg-primary-dark'}`}
                        onClick={handleSaveChanges}
                        disabled={isSaving}
                    >
                        {isSaving ? 'Đang lưu...' : 'Save change'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default UserProfile;