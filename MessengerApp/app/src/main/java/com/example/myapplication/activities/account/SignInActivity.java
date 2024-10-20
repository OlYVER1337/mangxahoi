package com.example.myapplication.activities.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.activities.home.MainActivity;
import com.example.myapplication.databinding.ActivitySignInBinding;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.example.myapplication.utilities.ImageUtil; // Import lớp ImageUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());

        // Kiểm tra nếu người dùng đã đăng nhập
        if (preferenceManager.getBoolean(Constants.Key_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        configureGoogleSignIn();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
        binding.googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        binding.buttonResetPassword.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class))
                );
    }

    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.Key_COLLECTION_USER)
                .whereEqualTo(Constants.Key_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.Key_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        loading(false);
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.Key_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.Key_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.Key_NAME, documentSnapshot.getString(Constants.Key_NAME));
                            preferenceManager.putString(Constants.Key_IMAGE, documentSnapshot.getString(Constants.Key_IMAGE));
                            preferenceManager.putString(Constants.Key_EMAIL, documentSnapshot.getString(Constants.Key_EMAIL));
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();  // Đảm bảo rằng SignInActivity kết thúc để người dùng không thể quay lại nó
                        } else {
                            showToast("Unable to sign in");
                            Log.e("SignIn", "Sign in failed: No matching documents found.");
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    showToast("Đặng nhập bằng Google thành công");
                }
            } catch (ApiException e) {
                showToast("Đăng nhập bằng Google thất bại do: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            saveUserInfo(user);
                        } else {
                            showToast("Ủy quyền thất bại.");
                        }
                    }
                });
    }

    private void saveUserInfo(FirebaseUser user) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.Key_COLLECTION_USER)
                .whereEqualTo(Constants.Key_EMAIL, user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.Key_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.Key_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.Key_NAME, documentSnapshot.getString(Constants.Key_NAME));
                            preferenceManager.putString(Constants.Key_IMAGE, documentSnapshot.getString(Constants.Key_IMAGE));
                            preferenceManager.putString(Constants.Key_EMAIL, documentSnapshot.getString(Constants.Key_EMAIL));
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Lấy ảnh đại diện của người dùng
                            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
                            String encodedImage;

                            // Kiểm tra xem có ảnh đại diện hay không, nếu không sử dụng ảnh mặc định
                            if (photoUrl != null) {
                                encodedImage = ImageUtil.encodeImageToBase64(photoUrl);
                            } else {
                                // Sử dụng ảnh mặc định
                                encodedImage = ImageUtil.encodeImageToBase64(String.valueOf(R.drawable.default_user_image));
                            }

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put(Constants.Key_NAME, user.getDisplayName());
                            userMap.put(Constants.Key_EMAIL, user.getEmail());
                            userMap.put(Constants.Key_IMAGE, encodedImage);

                            database.collection(Constants.Key_COLLECTION_USER)
                                    .add(userMap)
                                    .addOnSuccessListener(documentReference -> {
                                        preferenceManager.putBoolean(Constants.Key_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.Key_USER_ID, documentReference.getId());
                                        preferenceManager.putString(Constants.Key_NAME, user.getDisplayName());
                                        preferenceManager.putString(Constants.Key_IMAGE, encodedImage);
                                        preferenceManager.putString(Constants.Key_EMAIL, user.getEmail());
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Hãy nhập email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Nhập mật khẩu");
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
            googleSignInClient = null;
        }
    }
}
