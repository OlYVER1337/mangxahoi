package com.example.myapplication.utilities;

import java.util.HashMap;

public class Constants {
    // User related constants
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_USER_IMAGE = "userImage";

    // Chat related constants
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_TYPE = "type";
    public static final String KEY_FILE = "file";
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    // Post related constants
    public static final String KEY_COLLECTION_POSTS = "posts";
    public static final String KEY_POST_CONTENT = "content";
    public static final String KEY_POST_IMAGE = "postImage";
    public static final String KEY_POST_TIMESTAMP = "postTimestamp";
    public static final String KEY_POST_LIKES = "likes";
    public static final String KEY_POST_COMMENTS = "comments";

    // Friend related constants
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_FRIEND_REQUESTS = "friendRequests";
    public static final String KEY_SENT_FRIEND_REQUESTS = "sentFriendRequests";
    public static final String KEY_FRIEND_REQUEST_TIMESTAMPS = "friendRequestTimestamps";

    public static final String KEY_COLLECTION_FRIENDSHIPS = "friendships";
    public static final String KEY_COLLECTION_FRIEND_REQUESTS = "friendRequests";
    public static final String KEY_FRIEND_ID = "friendId";
    public static final String KEY_REQUEST_STATUS = "requestStatus";
    public static final String KEY_FRIEND_COUNT = "friendCount";
    public static final String KEY_LAST_FRIEND_UPDATE = "lastFriendUpdate";
    public static final String REQUEST_STATUS_PENDING = "pending";
    public static final String REQUEST_STATUS_ACCEPTED = "accepted";
    public static final String REQUEST_STATUS_REJECTED = "rejected";

    // Remote message related constants
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String REMOTE_MSG_NOTIFICATION = "notification";

    // Other constants
    public static final String KEY_PASS_APP = "evuq hwod ratx duco";
    public static final String KEY_EMAIL_APP = "minh1234564567@gmail.com";
    public static final String KEY_SHARED_PREF = "sharedPreferences";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAeKmOK1Y:APA91bGxHvxBkDjjKTVvBHSYZBDFtZbTEVXDEUTzDMnYQFKVgCZEqMGGpZNlzJmZGRVLRBvWZRFzXXZCDWJGNMJOuZwxmWuKwNZrEzGvhMBVZwrBxvRxVXQmvVxwCqCBnQPHxGbHGMGt"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
