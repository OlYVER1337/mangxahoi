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

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityChangePasswordBinding;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private ActivityChangePasswordBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String currentUserId;
    private String currentUserPassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        getUserDetails();
        setupButtonClick();
        setupBackClickListener();
        setupChangeProfileImageClickListener();
    }

    private void getUserDetails() {
        // Lấy thông tin người dùng từ Firestore
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        binding.inputName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                        binding.inputEmail.setText(documentSnapshot.getString(Constants.KEY_EMAIL));
                        currentUserPassword = documentSnapshot.getString(Constants.KEY_PASSWORD);

                        // Hiển thị ảnh đại diện từ URL
                        String imageUrl = documentSnapshot.getString(Constants.KEY_IMAGE);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_user_image)
                                    .error(R.drawable.default_user_image)
                                    .circleCrop()
                                    .into(binding.profileImageView);
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
        // Tạo đường dẫn lưu trữ ảnh trên Firebase Storage
        StorageReference imageRef = storageRef.child("profile_images/" + currentUserId + ".jpg");

        // Nén ảnh trước khi tải lên
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        // Tải ảnh lên Firebase Storage
        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL của ảnh sau khi tải lên thành công
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Cập nhật URL ảnh trong Firestore
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(currentUserId)
                                .update(Constants.KEY_IMAGE, imageUrl)
                                .addOnSuccessListener(unused -> {
                                    preferenceManager.putString(Constants.KEY_IMAGE, imageUrl);
                                    showToast("Cập nhật ảnh đại diện thành công");
                                    updateUserPostsWithNewImage(imageUrl);
                                })
                                .addOnFailureListener(e -> showToast("Cập nhật ảnh đại diện thất bại"));
                    });
                })
                .addOnFailureListener(e -> showToast("Tải ảnh lên thất bại"));
    }

    private void updateUserPostsWithNewImage(String newImageUrl) {
        // Cập nhật URL ảnh mới trong tất cả bài đăng của người dùng
        database.collection(Constants.KEY_COLLECTION_POSTS)
                .whereEqualTo(Constants.KEY_USER_ID, currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update(Constants.KEY_USER_IMAGE, newImageUrl);
                    }
                    showToast("Cập nhật ảnh đại diện trong bài đăng thành công");
                })
                .addOnFailureListener(e -> showToast("Không thể cập nhật ảnh đại diện trong bài đăng"));
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupBackClickListener() {
        binding.buttonBack.setOnClickListener(v -> finish());
        /*binding.imageBack.setOnClickListener(v -> {
            startActivity(new Intent(ChangePasswordActivity.this, HomeFragment.class));
            finish();
        });*/
    }
}
