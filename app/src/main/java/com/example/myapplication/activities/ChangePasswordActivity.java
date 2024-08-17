package com.example.myapplication.activities;

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

import com.example.myapplication.databinding.ActivityChangePasswordBinding;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ActivityChangePasswordBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String currentUserId;
    private String currentUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
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
                        String encodedImage = documentSnapshot.getString(Constants.Key_IMAGE);
                        if (encodedImage != null && !encodedImage.isEmpty()) {
                            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.profileImageView.setImageBitmap(bitmap);
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

            if (!newPassword.equals(confirmNewPassword)) {
                showToast("Mật khẩu xác nhận và mật khẩu mới không khớp");
                return;
            }

            if (!password.equals(currentUserPassword)) {
                showToast("Old password is incorrect");
                return;
            }

            updatePassword(newPassword);
        });
    }

    private void updatePassword(String newPassword) {
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.Key_PASSWORD, newPassword);
        database.collection(Constants.Key_COLLECTION_USER)
                .document(currentUserId)
                .update(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Password updated successfully");
                        finish();
                    } else {
                        showToast("Failed to update password");
                    }
                });
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
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.profileImageView.setImageBitmap(bitmap);
                uploadImageToFirebase(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        String encodedImage = encodeImageToBase64(bitmap);
        if (encodedImage != null) {
            // Update Firestore with the new image
            database.collection(Constants.Key_COLLECTION_USER).document(currentUserId)
                    .update(Constants.Key_IMAGE, encodedImage)
                    .addOnSuccessListener(unused -> {
                        // Update SharedPreferences
                        preferenceManager.putString(Constants.Key_IMAGE, encodedImage);
                        showToast("Profile image updated successfully");
                    }).addOnFailureListener(e -> showToast("Failed to update profile image"));
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
