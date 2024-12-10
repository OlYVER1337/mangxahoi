import { useEffect, useState } from "react";
import styles from "./ManageUsers.module.css";
import { useRouter } from "next/router";

const ManageUsers = () => {
    const router = useRouter();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [isEditing, setIsEditing] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteUserId, setDeleteUserId] = useState(null);
    const [adminInfo, setAdminInfo] = useState(null);
    const roles = ["User", "Editor", "Admin"];
    const availableActions = ["view_users", "edit_users", "edit_posts", "delete_posts"];

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await fetch("/api/admin/users");
            if (!response.ok) throw new Error("Failed to fetch users.");
            const data = await response.json();
            setUsers(data);
        } catch (err) {
            console.error("Error:", err);
            setError("Could not load users.");
        } finally {
            setLoading(false);
        }
    };

    const handleSaveUser = async (user) => {
        try {
            const method = user.id ? "PUT" : "POST";
            const response = await fetch(`/api/admin/users`, {
                method,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(user),
            });
            if (!response.ok) throw new Error("Failed to save user.");
            const savedUser = await response.json();
            alert(user.id ? "User updated successfully!" : "User added successfully!");
            if (method === "PUT") {
                setUsers((prev) => prev.map((u) => (u.id === savedUser.id ? savedUser : u)));
            } else {
                setUsers((prev) => [...prev, savedUser]);
            }
            setIsEditing(false);
            setCurrentUser(null);
        } catch (err) {
            console.error("Error:", err);
            alert("Failed to save user.");
        }
    };

    const confirmDelete = (userId) => {
        setDeleteUserId(userId);
        setShowDeleteModal(true);
    };

    const handleDeleteUser = async () => {
        try {
            const response = await fetch(`/api/admin/users`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ id: deleteUserId }), // Gửi ID trong body
            });

            if (!response.ok) throw new Error("Failed to delete user.");
            alert("User deleted successfully!");
            setUsers((prev) => prev.filter((user) => user.id !== deleteUserId));
        } catch (err) {
            console.error("Error:", err);
            alert("Failed to delete user.");
        } finally {
            setShowDeleteModal(false);
            setDeleteUserId(null);
        }
    };


    const handleEditUser = (user) => {
        setIsEditing(true);
        setCurrentUser({
            id: user.id || null,
            name: user.name || "",
            email: user.email || "",
            role: user.role || "User",
            actions: user.actions || [],
        });
    };

    const handleCloseForm = () => {
        setIsEditing(false);
        setCurrentUser(null);
    };

    useEffect(() => {
        fetchUsers();
        const user = sessionStorage.getItem("user");
        if (!user) {
            router.push("/admin/login");
        } else {
            const userData = JSON.parse(user);
            if (userData.role !== "Admin" && userData.role !== "Editor") {
                alert("Bạn không có quyền truy cập!");
                router.push("/admin/login");
            } else {
                setAdminInfo(userData);
            }
        }
        setLoading(false);
    }, [router]);

    return (
        <div className={styles.manageUsersContainer}>
            <h1 className={styles.title}>User Management</h1>
            <button className={styles.addUserBtn} onClick={() => handleEditUser()}>Add User</button>

            {loading && <p className={styles.loading}>Loading users...</p>}
            {error && <p className={styles.error}>{error}</p>}

            <table className={styles.userTable}>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Actions</th>
                        <th>Manage</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map((user, idx) => (
                        <tr key={user.id}>
                            <td>{idx + 1}</td>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>{user.role}</td>
                            <td>{(user.actions || []).join(", ")}</td>
                            <td>
                                <button className={styles.editBtn} onClick={() => handleEditUser(user)}>Edit</button>
                                <button className={styles.deleteBtn} onClick={() => confirmDelete(user.id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {isEditing && (
                <div className={styles.editForm}>
                    <h2>{currentUser.id ? "Edit User" : "Add User"}</h2>
                    <form
                        onSubmit={(e) => {
                            e.preventDefault();
                            handleSaveUser(currentUser);
                        }}
                    >
                        <label>
                            Name:
                            <input
                                type="text"
                                value={currentUser.name}
                                onChange={(e) => setCurrentUser({ ...currentUser, name: e.target.value })}
                                required
                            />
                        </label>
                        <label>
                            Email:
                            <input
                                type="email"
                                value={currentUser.email}
                                onChange={(e) => setCurrentUser({ ...currentUser, email: e.target.value })}
                                required
                            />
                        </label>
                        <label>
                            Role:
                            <select
                                value={currentUser.role}
                                onChange={(e) => setCurrentUser({ ...currentUser, role: e.target.value })}
                            >
                                {roles.map((role) => (
                                    <option key={role} value={role}>
                                        {role}
                                    </option>
                                ))}
                            </select>
                        </label>
                        <label>
                            Actions:
                            {availableActions.map((action) => (
                                <label key={action}>
                                    <input
                                        type="checkbox"
                                        checked={currentUser.actions?.includes(action)}
                                        onChange={(e) => {
                                            const updatedActions = e.target.checked
                                                ? [...(currentUser.actions || []), action]
                                                : currentUser.actions.filter((a) => a !== action);
                                            setCurrentUser({ ...currentUser, actions: updatedActions });
                                        }}
                                    />
                                    {action}
                                </label>
                            ))}
                        </label>
                        <button type="submit" className={styles.saveBtn}>Save</button>
                        <button type="button" className={styles.cancelBtn} onClick={handleCloseForm}>Cancel</button>
                    </form>
                </div>
            )}

            {showDeleteModal && (
                <div className={styles.modal}>
                    <p>Bạn có chắc chắn muốn xóa người dùng này không?</p>
                    <button onClick={handleDeleteUser} className={styles.confirmBtn}>Xác nhận</button>
                    <button onClick={() => setShowDeleteModal(false)} className={styles.cancelBtn}>Hủy</button>
                </div>
            )}
        </div>
    );
};

export default ManageUsers;
