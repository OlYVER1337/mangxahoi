// src/firebaseConfig.js
import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
    apiKey: "AIzaSyA3yp2aCdWX_avZbsXrVFyDTgKrq_DZqlw",
    authDomain: "easychat-7788b.firebaseapp.com",
    projectId: "easychat-7788b",
    storageBucket: "easychat-7788b.appspot.com",
    messagingSenderId: "516920636445",
    appId: "1:516920636445:web:478ebec6dea1fe705202f4"
};


const app = initializeApp(firebaseConfig);

export const db = getFirestore(app);
export const auth = getAuth(app);
