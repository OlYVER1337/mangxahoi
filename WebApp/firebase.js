// Import the functions you need from the SDKs you need
import { initializeApp, getApp, getApps } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";
import { getAuth, GoogleAuthProvider, signInWithPopup, createUserWithEmailAndPassword, signInWithEmailAndPassword } from 'firebase/auth';



// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyBMrt_mmzraf3AYKla38jhbzY4vdQiSV58",  
    authDomain: "easychat-7788b.firebaseapp.com",  
    projectId: "easychat-7788b",  
    storageBucket: "easychat-7788b.appspot.com",  
    messagingSenderId: "516920636445",  
    appId: "1:516920636445:web:478ebec6dea1fe705202f4"  
};

// Initialize Firebase
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
const db = getFirestore(app); // Đảm bảo khởi tạo db từ app
const storage = getStorage(app); // Đảm bảo khởi tạo storage từ app
const auth = getAuth(app)
const provider = new GoogleAuthProvider();
export const signInWithEmail = signInWithEmailAndPassword;
export const registerWithEmail = createUserWithEmailAndPassword;
export const signInWithGoogle = signInWithPopup;
export default app;
export { auth, provider, db, storage }; // Xuất db và storage để sử dụng trong các tệp khác
