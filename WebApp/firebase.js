// Import the functions you need from the SDKs you need
import { initializeApp, getApp, getApps } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

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
const db = getFirestore();
const storage = getStorage();

export default app;
export { db, storage };
