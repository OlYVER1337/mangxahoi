package com.example.myapplication.activities.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myapplication.activities.chat.ChatActivity;
import com.example.myapplication.adapter.UserAdapter;
import com.example.myapplication.databinding.ActivityUserBinding;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {
    private static final String TAG = "UserActivity";
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
        getFriends();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getFriends() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.Key_USER_ID);

        // First, get the current user's friend list
        database.collection(Constants.Key_COLLECTION_USER)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> friendIds = (List<String>) documentSnapshot.get(Constants.Key_FRIENDS);
                    if (friendIds != null && !friendIds.isEmpty()) {
                        loadFriendsData(friendIds);
                    } else {
                        loading(false);
                        showErrorMessage("No friends available");
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showErrorMessage("Failed to fetch friends");
                    Log.e(TAG, "Error getting friends: ", e);
                });
    }

    private void loadFriendsData(List<String> friendIds) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        List<User> users = new ArrayList<>();

        for (String friendId : friendIds) {
            database.collection(Constants.Key_COLLECTION_USER)
                    .document(friendId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = new User();
                        user.email = documentSnapshot.getString(Constants.Key_EMAIL);
                        user.image = documentSnapshot.getString(Constants.Key_IMAGE);
                        user.token = documentSnapshot.getString(Constants.Key_FCM_TOKEN);
                        user.name = documentSnapshot.getString(Constants.Key_NAME);
                        user.id = documentSnapshot.getId();
                        users.add(user);

                        // Check if we've loaded all friends
                        if (users.size() == friendIds.size()) {
                            loading(false);
                            if (users.size() > 0) {
                                UserAdapter userAdapter = new UserAdapter(users, this);
                                binding.userRecyclerview.setAdapter(userAdapter);
                                binding.userRecyclerview.setVisibility(View.VISIBLE);
                                binding.textErrorMessage.setVisibility(View.GONE);
                            } else {
                                showErrorMessage("No friends available");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        loading(false);
                        Log.e(TAG, "Error loading friend data: ", e);
                    });
        }
    }

    private void showErrorMessage(String message) {
        binding.textErrorMessage.setText(message);
        binding.textErrorMessage.setVisibility(View.VISIBLE);
        binding.userRecyclerview.setVisibility(View.GONE);
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.Key_USER, user);
        startActivity(intent);
        finish();
    }
}

