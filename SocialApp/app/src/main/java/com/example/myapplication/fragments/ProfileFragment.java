package com.example.myapplication.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageEncoded = documentSnapshot.getString(Constants.KEY_IMAGE);
                        if (imageEncoded != null) {
                            byte[] bytes = Base64.decode(imageEncoded, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.imageProfile.setImageBitmap(bitmap);
                        }
                    }
                });
    }

    private void setUserInfo() {
        String name = preferenceManager.getString(Constants.KEY_NAME);
        if (name != null) {
            binding.textName.setText(name);
        }

        String imageString = preferenceManager.getString(Constants.KEY_IMAGE);
        if (imageString != null) {
            byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
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

    }

    private void signOut() {
        showToast("Đang đăng xuất...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getActivity(), SignInActivity.class));
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> showToast("Không thể đăng xuất"));
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
