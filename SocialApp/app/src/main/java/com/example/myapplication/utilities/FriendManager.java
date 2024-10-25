package com.example.myapplication.utilities;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.List;

public class FriendManager {
    private FirebaseFirestore db;

    public FriendManager() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> sendFriendRequest(String senderId, String receiverId) {
        DocumentReference senderRef = db.collection(Constants.KEY_COLLECTION_USERS).document(senderId);
        DocumentReference receiverRef = db.collection(Constants.KEY_COLLECTION_USERS).document(receiverId);

        com.google.firebase.Timestamp timestamp = com.google.firebase.Timestamp.now();

        return db.runTransaction(transaction -> {
            transaction.update(senderRef, Constants.KEY_SENT_FRIEND_REQUESTS, FieldValue.arrayUnion(receiverId));
            transaction.update(receiverRef, Constants.KEY_FRIEND_REQUESTS, FieldValue.arrayUnion(senderId));
            transaction.update(receiverRef, Constants.KEY_FRIEND_REQUEST_TIMESTAMPS + "." + senderId, timestamp);
            return null;
        });
    }




    public Task<Void> acceptFriendRequest(String userId, String friendId) {
        DocumentReference userRef = db.collection(Constants.KEY_COLLECTION_USERS).document(userId);
        DocumentReference friendRef = db.collection(Constants.KEY_COLLECTION_USERS).document(friendId);

        return db.runTransaction(transaction -> {
            transaction.update(userRef, Constants.KEY_FRIENDS, FieldValue.arrayUnion(friendId));
            transaction.update(userRef, Constants.KEY_FRIEND_REQUESTS, FieldValue.arrayRemove(friendId));
            transaction.update(friendRef, Constants.KEY_FRIENDS, FieldValue.arrayUnion(userId));
            transaction.update(friendRef, Constants.KEY_SENT_FRIEND_REQUESTS, FieldValue.arrayRemove(userId));
            return null;
        });
    }

    public Task<Void> declineFriendRequest(String userId, String friendId) {
        DocumentReference userRef = db.collection(Constants.KEY_COLLECTION_USERS).document(userId);
        DocumentReference friendRef = db.collection(Constants.KEY_COLLECTION_USERS).document(friendId);

        return db.runTransaction(transaction -> {
            transaction.update(userRef, Constants.KEY_FRIEND_REQUESTS, FieldValue.arrayRemove(friendId));
            transaction.update(friendRef, Constants.KEY_SENT_FRIEND_REQUESTS, FieldValue.arrayRemove(userId));
            return null;
        });
    }

    public Task<Void> removeFriend(String userId, String friendId) {
        DocumentReference userRef = db.collection(Constants.KEY_COLLECTION_USERS).document(userId);
        DocumentReference friendRef = db.collection(Constants.KEY_COLLECTION_USERS).document(friendId);

        return db.runTransaction(transaction -> {
            transaction.update(userRef, Constants.KEY_FRIENDS, FieldValue.arrayRemove(friendId));
            transaction.update(friendRef, Constants.KEY_FRIENDS, FieldValue.arrayRemove(userId));
            return null;
        });
    }

    public Task<Void> cancelFriendRequest(String senderId, String receiverId) {
        DocumentReference senderRef = db.collection(Constants.KEY_COLLECTION_USERS).document(senderId);
        DocumentReference receiverRef = db.collection(Constants.KEY_COLLECTION_USERS).document(receiverId);

        return db.runTransaction(transaction -> {
            transaction.update(senderRef, Constants.KEY_SENT_FRIEND_REQUESTS, FieldValue.arrayRemove(receiverId));
            transaction.update(receiverRef, Constants.KEY_FRIEND_REQUESTS, FieldValue.arrayRemove(senderId));
            return null;
        });
    }

    public Task<Boolean> checkPendingRequest(String currentUserId, String targetUserId) {
        return db.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            List<String> sentRequests = (List<String>) document.get(Constants.KEY_SENT_FRIEND_REQUESTS);
                            return sentRequests != null && sentRequests.contains(targetUserId);
                        }
                    }
                    return false;
                });
    }
}
