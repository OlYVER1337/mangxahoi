package com.example.myapplication.activities;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.adapter.ChatAdapter;
import com.example.myapplication.databinding.ActivityChatBinding;
import com.example.myapplication.listeners.ChatAdapterListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ChatActivity extends BaseActivity implements ChatAdapterListener {
    private static final int PICK_FILE_REQUEST = 1;
    private static final int PICK_MEDIA_REQUEST = 2;
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String conversionID = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        checkPermissions();
        setListeners();
        loadReceiverDetail();
        init();
        listenMessages();
        listenAvailabilityReceiver();
    }
    @Override
    public void onDownloadFile(String fileUrl) {
        Log.d("ChatActivity", "Đang tải xuống tệp: " + fileUrl);
        downloadFile(fileUrl);
    }

    private void downloadFile(String fileUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setDescription("Đang tải xuống tệp...");
        request.setTitle("Tải xuống tệp");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFileNameFromUrl(fileUrl));

        // Lấy dịch vụ tải xuống và xếp hàng tệp
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Đang tải xuống tệp...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Quản lý tải xuống không khả dụng", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUrl(String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this,chatMessages, getBitmapFromEncodedString(receiverUser.image), preferenceManager.getString(Constants.Key_USER_ID), this);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(String type, String content, @Nullable String fileUrl) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.Key_SENDER_ID, preferenceManager.getString(Constants.Key_USER_ID));
        message.put(Constants.Key_RECEIVER_ID, receiverUser.id);
        message.put(Constants.Key_MESSAGE, content);
        message.put(Constants.Key_TYPE, type);
        message.put(Constants.Key_TIMESTAMP, new Date());
        if (fileUrl != null) {
            message.put(Constants.Key_FILE, fileUrl);
        }
        database.collection(Constants.Key_COLLECTION_CHAT).add(message);
        if (conversionID != null) {
            updateConversion(content);
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.Key_SENDER_ID, preferenceManager.getString(Constants.Key_USER_ID));
            conversion.put(Constants.Key_SENDER_NAME, preferenceManager.getString(Constants.Key_NAME));
            conversion.put(Constants.Key_SENDER_IMAGE, preferenceManager.getString(Constants.Key_IMAGE));
            conversion.put(Constants.Key_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.Key_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.Key_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.Key_LAST_MESSAGE, content);
            conversion.put(Constants.Key_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.Key_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.Key_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.Key_MESSAGE);
                    chatMessage.fileType = documentChange.getDocument().getString(Constants.Key_TYPE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.Key_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.Key_TIMESTAMP);
                    if ("media".equals(chatMessage.fileType)) {
                        chatMessage.fileUrl = documentChange.getDocument().getString(Constants.Key_FILE);
                    }
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(count, chatMessages.size() - count);
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionID == null) {
            checkForConversion();
        }
    };


    private void listenAvailabilityReceiver() {
        database.collection(Constants.Key_COLLECTION_USER).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.Key_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.Key_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.Key_FCM_TOKEN);
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void listenMessages() {
        database.collection(Constants.Key_COLLECTION_CHAT)
                .whereEqualTo(Constants.Key_SENDER_ID, preferenceManager.getString(Constants.Key_USER_ID))
                .whereEqualTo(Constants.Key_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.Key_COLLECTION_CHAT)
                .whereEqualTo(Constants.Key_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.Key_RECEIVER_ID, preferenceManager.getString(Constants.Key_USER_ID))
                .addSnapshotListener(eventListener);
    }



    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadReceiverDetail() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.Key_USER);
        byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String nicknamesJson = prefs.getString("nicknames", "{}");

        try {
            JSONObject nicknames = new JSONObject(nicknamesJson);
            String uniqueKey = preferenceManager.getString(Constants.Key_EMAIL) + "_" + receiverUser.id;
            String nickname = nicknames.optString(uniqueKey, receiverUser.name);

            if (!nickname.isEmpty()) {
                binding.textName.setText(nickname);
            } else {
                binding.textName.setText(receiverUser.name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        binding.imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
            intent.putExtra(Constants.Key_RECEIVER_NAME, receiverUser.name);
            intent.putExtra(Constants.Key_RECEIVER_IMAGE, receiverUser.image);
            intent.putExtra(Constants.Key_RECEIVER_ID, receiverUser.id);
            startActivity(intent);
        });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage("text", binding.inputMessage.getText().toString(), null));
        binding.imageAttachment.setOnClickListener(v -> openFilePicker(PICK_FILE_REQUEST));
        binding.imageSendMedia.setOnClickListener(v -> openFilePicker(PICK_MEDIA_REQUEST));
        binding.buttonDeleteChat.setOnClickListener(v -> deleteChat());
    }


    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (requestCode == PICK_FILE_REQUEST) {
            intent.setType("*/*");
        } else if (requestCode == PICK_MEDIA_REQUEST) {
            intent.setType("image/* video/*");
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onOpenFile(String fileUrl) {
        String mimeType = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        if (mimeType != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeType);

            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    Intent intent = new Intent(this, ImageViewActivity.class);
                    intent.putExtra("imageUrl", fileUrl);
                    startActivity(intent);
                } else if (mimeType.startsWith("video/")) {
                    Intent intent = new Intent(this, VideoViewActivity.class);
                    intent.putExtra("videoUrl", fileUrl);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Unknown file type", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid file URL", Toast.LENGTH_SHORT).show();
        }
    }




    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String fileType = getFileType(fileUri);
                if (requestCode == PICK_FILE_REQUEST) {
                    uploadFile(fileUri, fileType);
                } else if (requestCode == PICK_MEDIA_REQUEST) {
                    uploadMedia(fileUri, fileType);
                }
            }
        }
    }

    private String getFileType(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile(Uri fileUri, String fileType) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("files/" + System.currentTimeMillis() + "." + fileType);
        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    sendMessage("file", "File attached", uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "File upload failed", Toast.LENGTH_SHORT).show());
    }

    private void uploadMedia(Uri mediaUri, String fileType) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("media/" + System.currentTimeMillis() + "." + fileType);
        storageReference.putFile(mediaUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    sendMessage("media", "Đã gửi một tập tin", uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "tải file thất bại", Toast.LENGTH_SHORT).show());
    }

    private void deleteChat() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.Key_COLLECTION_CHAT)
                .whereEqualTo(Constants.Key_SENDER_ID, preferenceManager.getString(Constants.Key_USER_ID))
                .whereEqualTo(Constants.Key_RECEIVER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            db.collection(Constants.Key_COLLECTION_CHAT).document(document.getId()).delete();
                        }
                    }
                });

        db.collection(Constants.Key_COLLECTION_CHAT)
                .whereEqualTo(Constants.Key_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.Key_RECEIVER_ID, preferenceManager.getString(Constants.Key_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            db.collection(Constants.Key_COLLECTION_CHAT).document(document.getId()).delete();
                        }
                    }
                });
        Toast.makeText(this, "Chat deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.Key_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionID = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Constants.Key_COLLECTION_CONVERSATIONS).document(conversionID);
        documentReference.update(
                Constants.Key_LAST_MESSAGE, message,
                Constants.Key_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.Key_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.Key_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.Key_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.Key_SENDER_ID, senderId)
                .whereEqualTo(Constants.Key_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionID = documentSnapshot.getId();
        }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityReceiver();
    }
}
