import axios from "axios";

const API_BASE_URL = "http://localhost:5000/api"; // URL của server backend

// Hàm thêm quyền admin cho người dùng
export const addAdmin = async (userId) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/add-admin/${userId}`);
        return response.data; // Trả về phản hồi nếu cần dùng
    } catch (error) {
        console.error("Lỗi khi thêm quyền admin:", error);
        throw new Error("Lỗi khi thêm quyền admin.");
    }
};

// Hàm xóa quyền admin của người dùng
export const removeAdmin = async (userId) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/remove-admin/${userId}`);
        return response.data;
    } catch (error) {
        console.error("Lỗi khi xóa quyền admin:", error);
        throw new Error("Lỗi khi xóa quyền admin.");
    }
};

// Hàm kiểm tra quyền admin của người dùng
export const checkAdmin = async (userId) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/check-admin/${userId}`);
        return response.data;
    } catch (error) {
        console.error("Lỗi khi kiểm tra quyền admin:", error);
        throw new Error("Lỗi khi kiểm tra quyền admin.");
    }
};
