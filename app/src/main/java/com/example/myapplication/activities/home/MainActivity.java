package com.example.myapplication.activities.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.activities.account.ChangePasswordActivity;
import com.example.myapplication.activities.account.SignInActivity;
import com.example.myapplication.activities.chat.ChatActivity;
import com.example.myapplication.adapter.RecentConversationsAdapter;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.listeners.ConversionListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {
    private PreferenceManager preferenceManager;
    private ActivityMainBinding binding;
    private List<ChatMessage> conversation;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetail();
        getToken();
        setListeners();
        listenConvertasion();
    }
    private void init() {
        conversation = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversation, this);
        binding.conversationRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void setListeners() {
        binding.imageProfile.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class)));
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserActivity.class)));
        binding.imageSearch.setOnClickListener(v -> startSearchActivity());
    }
    private void startSearchActivity() {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        intent.putParcelableArrayListExtra("conversations", new ArrayList<>(conversation));
        startActivity(intent);
    }
    private void loadUserDetail() {
        binding.textName.setText(preferenceManager.getString(Constants.Key_NAME));
        String encodedImage = preferenceManager.getString(Constants.Key_IMAGE);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.imageProfile.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                showToast("Tải ảnh người dùng thất bại");
                Log.e("MainActivity", "Failed to decode Base64 string", e);
            }
        } else {
            showToast("Không thể tìm thấy ảnh đại diện của người dùng");
            Log.e("MainActivity", "User image not found");
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void listenConvertasion() {
        database.collection(Constants.Key_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.Key_SENDER_ID, preferenceManager.getString(Constants.Key_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.Key_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.Key_RECEIVER_ID, preferenceManager.getString(Constants.Key_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                String senderID = documentChange.getDocument().getString(Constants.Key_SENDER_ID);
                String receiverID = documentChange.getDocument().getString(Constants.Key_RECEIVER_ID);

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    boolean conversationExists = false;
                    for (ChatMessage chatMessage : conversation) {
                        if (chatMessage.senderId.equals(senderID) && chatMessage.receiverId.equals(receiverID)) {
                            conversationExists = true;
                            break;
                        }
                    }
                    if (!conversationExists) {

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = senderID;
                        chatMessage.receiverId = receiverID;
                        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                        String nicknamesJson = prefs.getString("nicknames", "{}");
                        try {
                            JSONObject nicknames = new JSONObject(nicknamesJson);
                            String senderName = documentChange.getDocument().getString(Constants.Key_SENDER_NAME);
                            String receiverName = documentChange.getDocument().getString(Constants.Key_RECEIVER_NAME);
                            String uniqueKeya = preferenceManager.getString(Constants.Key_EMAIL) + "_" + senderID;
                            String uniqueKeyb = preferenceManager.getString(Constants.Key_EMAIL) + "_" + receiverID;

                            String senderNickname = nicknames.optString(uniqueKeya, senderName);
                            String receiverNickname = nicknames.optString(uniqueKeyb, receiverName);

                            if (preferenceManager.getString(Constants.Key_USER_ID).equals(senderID)) {
                                chatMessage.conversionImage = documentChange.getDocument().getString(Constants.Key_RECEIVER_IMAGE);
                                chatMessage.conversionName = receiverNickname;
                                chatMessage.conversionId = receiverID;
                            } else {
                                chatMessage.conversionImage = documentChange.getDocument().getString(Constants.Key_SENDER_IMAGE);
                                chatMessage.conversionName = senderNickname;
                                chatMessage.conversionId = senderID;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chatMessage.message = documentChange.getDocument().getString(Constants.Key_LAST_MESSAGE);
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.Key_TIMESTAMP);
                        conversation.add(chatMessage);
                    }
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversation.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.Key_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.Key_RECEIVER_ID);
                        if (conversation.get(i).senderId.equals(senderId) && conversation.get(i).receiverId.equals(receiverId)) {
                            conversation.get(i).message = documentChange.getDocument().getString(Constants.Key_LAST_MESSAGE);
                            conversation.get(i).dateObject = documentChange.getDocument().getDate(Constants.Key_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversation, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.Key_COLLECTION_USER)
                .document(preferenceManager.getString(Constants.Key_USER_ID));
        documentReference.update(Constants.Key_FCM_TOKEN, token).addOnFailureListener(e -> showToast("Unable to update token"));
    }
    private void signOut() {
        showToast("Đăng xuất...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constants.Key_USER_ID);
        if (userId == null || userId.isEmpty()) {
            showToast("Không tìm thấy người dùng");
            return;
        }
        DocumentReference documentReference = database.collection(Constants.Key_COLLECTION_USER)
                .document(userId);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.Key_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Không thể đăng xuất");
                });
    }
    @Override
    public void onConversionClick(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.Key_USER, user);
        startActivity(intent);
    }
}
