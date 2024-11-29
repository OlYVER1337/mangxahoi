export const getUserIdByEmail = async (email) => {
    try {
        const response = await fetch(`http://localhost:5000/api/users?email=${email}`);
        const data = await response.json();
        return data.userId; // Đảm bảo API trả về userId
    } catch (error) {
        console.error('Lỗi khi lấy userId:', error);
        throw error;
    }
};
