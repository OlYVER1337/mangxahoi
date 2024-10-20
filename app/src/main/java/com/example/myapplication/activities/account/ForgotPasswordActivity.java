package com.example.myapplication.activities.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.SendEmailTask;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextOtp, editTextNewPassword;
    private Button buttonSendOtp, buttonVerifyOtp, buttonResetPassword;
    private ProgressBar progressBar;
    private String generatedOtp;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextOtp = findViewById(R.id.editTextOtp);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        progressBar = findViewById(R.id.progressBar);

        buttonSendOtp.setOnClickListener(v -> sendOtpEmail());
        buttonVerifyOtp.setOnClickListener(v -> verifyOtp());
        buttonResetPassword.setOnClickListener(v -> updatePassword());
    }

    private void sendOtpEmail() {
        String email = editTextEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.Key_COLLECTION_USER)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Tìm thấy người dùng
                            userId = task.getResult().getDocuments().get(0).getId();
                            generatedOtp = generateOtp();
                            new SendEmailTask(email, generatedOtp).execute();
                            saveOtpToDatabase(userId, generatedOtp);

                            // Hiển thị các trường OTP và nút xác nhận
                            editTextOtp.setVisibility(View.VISIBLE);
                            buttonVerifyOtp.setVisibility(View.VISIBLE);
                            buttonSendOtp.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(this, "Không tìm thấy người dùng với email này", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi khi tìm người dùng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveOtpToDatabase(String userId, String otp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);

        db.collection("otp")
                .document(userId)
                .set(otpData)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Lưu OTP thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyOtp() {
        String enteredOtp = editTextOtp.getText().toString().trim();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("otp")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String savedOtp = (String) task.getResult().get("otp");
                        if (enteredOtp.equals(savedOtp)) {
                            // Hiển thị trường mật khẩu mới
                            editTextNewPassword.setVisibility(View.VISIBLE);
                            buttonResetPassword.setVisibility(View.VISIBLE);
                            buttonVerifyOtp.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(this, "OTP không chính xác", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi khi xác nhận OTP: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("password", newPassword);

        db.collection(Constants.Key_COLLECTION_USER)
                .document(userId)
                .update(userUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Mật khẩu đã được cập nhật", Toast.LENGTH_SHORT).show();

                        // Chuyển đến activity đăng nhập
                        Intent intent = new Intent(ForgotPasswordActivity.this, SignUpActivity.class);
                        startActivity(intent);
                        finish(); // Đóng activity hiện tại
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Cập nhật mật khẩu thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = random.nextInt(999999);
        return String.format("%06d", otp);
    }
}
