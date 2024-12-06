import { useEffect, useState } from "react";

const ManageUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // Lấy danh sách người dùng từ API
    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await fetch("/api/admin/users"); // Fetch API
            if (!response.ok) {
                throw new Error("Failed to fetch users.");
            }
            const data = await response.json();
            setUsers(data);
        } catch (err) {
            console.error("Lỗi khi lấy danh sách người dùng:", err);
            setError("Không thể tải danh sách người dùng.");
        } finally {
            setLoading(false);
        }
    };

    // Xóa người dùng
    const handleDelete = async (userId) => {
        const confirm = window.confirm("Bạn có chắc muốn xóa người dùng này?");
        if (confirm) {
            try {
                const response = await fetch("/api/admin/users", {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ id: userId }),
                });

                if (!response.ok) {
                    throw new Error("Failed to delete user.");
                }

                alert("Xóa người dùng thành công!");
                fetchUsers(); // Refresh danh sách người dùng
            } catch (err) {
                console.error("Lỗi khi xóa người dùng:", err);
                alert("Không thể xóa người dùng.");
            }
        }
    };

    // Thêm vai trò mới
    const handleAddRole = async (userId, newRole) => {
        try {
            const response = await fetch("/api/admin/users", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ id: userId, newRole }),
            });

            if (!response.ok) {
                throw new Error("Failed to add role.");
            }

            alert("Thêm vai trò thành công!");
            fetchUsers(); // Refresh danh sách người dùng
        } catch (err) {
            console.error("Lỗi khi thêm vai trò:", err);
            alert("Không thể thêm vai trò.");
        }
    };

    // Xóa vai trò
    const handleRemoveRole = async (userId, roleToRemove) => {
        try {
            const response = await fetch("/api/admin/users", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ id: userId, removeRole: roleToRemove }),
            });

            if (!response.ok) {
                throw new Error("Failed to remove role.");
            }

            alert("Xóa vai trò thành công!");
            fetchUsers(); // Refresh danh sách người dùng
        } catch (err) {
            console.error("Lỗi khi xóa vai trò:", err);
            alert("Không thể xóa vai trò.");
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    if (loading) return <p>Đang tải danh sách người dùng...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div style={{ padding: "20px", fontFamily: "Arial, sans-serif" }}>
            <h1 style={{ textAlign: "center", marginBottom: "20px" }}>Quản Lý Người Dùng</h1>

            <table
                border="1"
                cellPadding="10"
                style={{ width: "100%", marginTop: "20px", borderCollapse: "collapse" }}
            >
                <thead>
                    <tr style={{ backgroundColor: "#f4f4f4" }}>
                        <th>#</th>
                        <th>Tên</th>
                        <th>Email</th>
                        <th>Vai Trò</th>
                        <th>Hành Động</th>
                    </tr>
                </thead>
                <tbody>
                    {users.length > 0 ? (
                        users.map((user, index) => (
                            <tr key={user.id} style={{ textAlign: "center" }}>
                                <td>{index + 1}</td>
                                <td>{user.name}</td>
                                <td>{user.email}</td>
                                <td>
                                    <div style={{ display: "flex", flexWrap: "wrap", gap: "5px" }}>
                                        {(Array.isArray(user.role) ? user.role : []).map((role, idx) => (
                                            <span
                                                key={idx}
                                                style={{
                                                    padding: "5px 10px",
                                                    backgroundColor: "#d9edf7",
                                                    borderRadius: "4px",
                                                    cursor: "pointer",
                                                }}
                                                onClick={() => handleRemoveRole(user.id, role)}
                                            >
                                                {role} ❌
                                            </span>
                                        ))}
                                        <button
                                            onClick={() => {
                                                const newRole = prompt("Nhập vai trò mới:");
                                                if (newRole) handleAddRole(user.id, newRole);
                                            }}
                                            style={{
                                                padding: "5px",
                                                border: "1px dashed #ddd",
                                                borderRadius: "4px",
                                                backgroundColor: "white",
                                                cursor: "pointer",
                                            }}
                                        >
                                            ➕
                                        </button>
                                    </div>
                                </td>

                                <td>
                                    <button
                                        onClick={() => handleDelete(user.id)}
                                        style={{
                                            padding: "5px 10px",
                                            backgroundColor: "#ff4d4d",
                                            color: "white",
                                            border: "none",
                                            borderRadius: "4px",
                                            cursor: "pointer",
                                        }}
                                    >
                                        Xóa
                                    </button>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="5">Không có người dùng nào.</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default ManageUsers;
