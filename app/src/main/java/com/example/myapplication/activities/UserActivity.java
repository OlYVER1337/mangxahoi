package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.Key_COLLECTION_USER)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.Key_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.email = queryDocumentSnapshot.getString(Constants.Key_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.Key_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.Key_FCM_TOKEN);
                            user.name = queryDocumentSnapshot.getString(Constants.Key_NAME);
                            user.id =queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.userRecyclerview.setAdapter(userAdapter);
                            binding.userRecyclerview.setVisibility(View.VISIBLE);
                            binding.textErrorMessage.setVisibility(View.GONE);
                        } else {
                            showErrorMessage("No users available");
                        }
                    } else {
                        if (task.getException() != null) {
                            Log.e(TAG, "Error fetching users: ", task.getException());
                        }
                        showErrorMessage("Failed to fetch users");
                    }
                });
    }
    private void showErrorMessage(String message){
        binding.textErrorMessage.setText(message);
        binding.textErrorMessage.setVisibility(View.VISIBLE);
        binding.userRecyclerview.setVisibility(View.GONE);
    }
    private void loading(boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.Key_USER,user);
        startActivity(intent);
        finish();
    }
}
