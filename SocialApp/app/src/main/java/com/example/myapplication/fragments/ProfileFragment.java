package com.example.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.activities.account.ChangePasswordActivity;
import com.example.myapplication.activities.account.SignInActivity;
import com.example.myapplication.activities.account.SignUpActivity;
import com.example.myapplication.activities.home.UserInfoActivity;
import com.example.myapplication.databinding.FragmentProfileBinding;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());
        setUserInfo();
        setListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserInfo();
    }

    private void updateUserInfo() {
        // Lấy thông tin người dùng từ Firestore và cập nhật giao diện
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString(Constants.KEY_IMAGE);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Dùng Glide để hiển thị ảnh đại diện dạng tròn
                            Glide.with(this)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(binding.imageProfile);
                        }
                    }
                });
    }

    private void setUserInfo() {
        // Hiển thị tên người dùng
        String name = preferenceManager.getString(Constants.KEY_NAME);
        if (name != null) {
            binding.textName.setText(name);
        }

        // Hiển thị ảnh đại diện từ URL
        String imageUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(binding.imageProfile);
        }
    }

    private void setListeners() {
        binding.buttonChangePassword.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        binding.buttonSignOut.setOnClickListener(v -> signOut());
        binding.layoutUserInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            startActivity(intent);
        });
        binding.buttonOpenMessenger.setOnClickListener(v -> openMessengerApp());
    }

    private void openMessengerApp() {
        try {
            // Lấy email của người dùng hiện tại
            String currentEmail = preferenceManager.getString(Constants.KEY_EMAIL);

            // Mở MessengerApp với email
            Intent intent = new Intent();
            intent.setClassName("com.example.messengerapp",
                    "com.example.messengerapp.activities.home.MainActivity");
            intent.putExtra("userEmail", currentEmail);
            intent.putExtra("autoLogin", true);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Không thể mở ứng dụng Messenger");
        }
    }




    private void signOut() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if (userId != null && !userId.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            documentSnapshot.getReference()
                                    .update(Constants.KEY_FCM_TOKEN, "")
                                    .addOnSuccessListener(unused -> {
                                        preferenceManager.clear();
                                        navigateToSignIn();
                                    });
                        } else {
                            preferenceManager.clear();
                            navigateToSignIn();
                        }
                    })
                    .addOnFailureListener(e -> {
                        preferenceManager.clear();
                        navigateToSignIn();
                    });
        } else {
            preferenceManager.clear();
            navigateToSignIn();
        }
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
