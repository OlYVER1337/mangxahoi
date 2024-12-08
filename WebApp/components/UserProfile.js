import { useState } from 'react';
import { PiNotePencil } from 'react-icons/pi';
import { doc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase';

const UserProfile = ({ session, update }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [newName, setNewName] = useState(session?.user?.name);
    const [isBioEditing, setIsBioEditing] = useState(false);
    const [newBio, setNewBio] = useState(session?.user?.bio || '');

    const handleNameChange = async () => {
        if (newName.trim() && newName !== session.user.name) {
            await updateDoc(doc(db, "users", session.user.id), {
                name: newName
            });
            await update({ user: { ...session.user, name: newName } });
            setIsEditing(false);
        }
    };

    const handleBioChange = async () => {
        if (newBio.trim() !== session.user.bio) {
            await updateDoc(doc(db, "users", session.user.id), {
                bio: newBio
            });
            await update({ user: { ...session.user, bio: newBio } });
            setIsBioEditing(false);
        }
    };

    const handleKeyDown = (e, handler) => {
        if (e.key === 'Enter') {
            handler();
        }
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
                                    onBlur={handleNameChange}
                                    onKeyDown={(e) => handleKeyDown(e, handleNameChange)}
                                    className="text-xl font-semibold border-b focus:outline-none focus:border-primary"
                                    autoFocus
                                />
                            ) : (
                                <div className="flex items-center">
                                    <h1 className="text-xl font-semibold">{session?.user?.name}</h1>
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
                            onBlur={handleBioChange}
                            className="w-full p-2 border rounded-lg focus:outline-none focus:border-primary"
                            rows="3"
                            autoFocus
                        />
                    ) : (
                        <p className="text-gray-700">{session?.user?.bio || 'Thêm giới thiệu về bản thân...'}</p>
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
                    <a 
                        href="/edit-profile" 
                        className="bg-primary text-white py-2 px-4 rounded-lg hover:bg-primary-dark transition"
                    >
                        Chỉnh sửa hồ sơ
                    </a>
                </div>
            </div>
        </div>
    );
};

export default UserProfile;