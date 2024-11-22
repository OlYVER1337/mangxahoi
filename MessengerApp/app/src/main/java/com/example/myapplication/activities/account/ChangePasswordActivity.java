package com.example.myapplication.activities.account;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.activities.home.MainActivity;
import com.example.myapplication.databinding.ActivityChangePasswordBinding;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private ActivityChangePasswordBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private String currentUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = preferenceManager.getString(Constants.Key_USER_ID);

        getUserDetails();
        setupButtonClick();
        setupChatNowClickListener();
        setupChangeProfileImageClickListener();
    }

    private void getUserDetails() {
        database.collection(Constants.Key_COLLECTION_USER)
                .document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        binding.inputName.setText(documentSnapshot.getString(Constants.Key_NAME));
                        binding.inputEmail.setText(documentSnapshot.getString(Constants.Key_EMAIL));
                        currentUserPassword = documentSnapshot.getString(Constants.Key_PASSWORD);
                        String imageUrl = documentSnapshot.getString(Constants.Key_IMAGE);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(binding.profileImageView);
                        }
                    }
                });
    }

    private void setupButtonClick() {
        binding.buttonChangePassword.setOnClickListener(v -> {
            String password = binding.inputPassword.getText().toString().trim();
            String newPassword = binding.inputNewPassword.getText().toString().trim();
            String confirmNewPassword = binding.inputConfirmNewPassword.getText().toString().trim();

            if (password.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                showToast("Nhập mật khẩu");
                return;
            }
            if (newPassword.length() < MIN_PASSWORD_LENGTH) {
                showToast("Mật khẩu mới phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                showToast("Mật khẩu xác nhận và mật khẩu mới không khớp");
                return;
            }


            changePassword(password, newPassword);
        });
    }

    private void changePassword(String oldPassword, String newPassword) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            showToast("Cập Nhật Mật Khẩu Thành Công");
                                            finish();
                                        } else {
                                            showToast("Cập Nhật Mật Khẩu Thất Bại: " + task1.getException().getMessage());
                                        }
                                    });
                        } else {
                            showToast("Mật khẩu cũ không đúng");
                        }
                    });
        }
    }


    private void setupChangeProfileImageClickListener() {
        binding.buttonChangeProfileImage.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // Hiển thị ảnh preview
            Picasso.get().load(imageUri).into(binding.profileImageView);
            // Upload ảnh lên Firebase
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images/" + currentUserId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        database.collection(Constants.Key_COLLECTION_USER)
                                .document(currentUserId)
                                .update(Constants.Key_IMAGE, imageUrl)
                                .addOnSuccessListener(unused -> {
                                    preferenceManager.putString(Constants.Key_IMAGE, imageUrl);
                                    showToast("Cập nhật ảnh đại diện thành công");
                                })
                                .addOnFailureListener(e -> showToast("Cập nhật ảnh đại diện thất bại"));
                    });
                })
                .addOnFailureListener(e -> showToast("Tải ảnh lên thất bại"));
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupChatNowClickListener() {
        binding.textMain.setOnClickListener(v -> {
            startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
            finish();
        });
    }
}
