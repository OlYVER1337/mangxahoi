// src/api/firebaseApi.js
import axios from "axios";

export const sendNotification = async (message) => {
    try {
        const response = await axios.post("http://localhost:5000/api/FirebaseAdmin/sendNotification", message);
        return response.data;
    } catch (error) {
        console.error("Error sending notification:", error);
    }
};
