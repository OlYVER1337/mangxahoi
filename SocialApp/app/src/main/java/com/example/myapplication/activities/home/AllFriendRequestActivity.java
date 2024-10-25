package com.example.myapplication.activities.home;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.FriendRequestAdapter;
import com.example.myapplication.databinding.ActivityAllFriendRequestsBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.FriendManager;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AllFriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.OnFriendRequestActionListener {

    private ActivityAllFriendRequestsBinding binding;
    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private List<User> friendRequests;
    private FirebaseFirestore db;
    private String currentUserId;
    private FriendManager friendManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllFriendRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = binding.recyclerViewAllFriendRequests;
        friendRequests = new ArrayList<>();
        adapter = new FriendRequestAdapter(friendRequests, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        friendManager = new FriendManager();

        db = FirebaseFirestore.getInstance();
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        loadAllFriendRequests();
        setupBackClickListener();
    }

    private void setupBackClickListener() {
        binding.buttonBack.setOnClickListener(v -> finish());
    }
    private void loadAllFriendRequests() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> requestIds = (List<String>) documentSnapshot.get(Constants.KEY_FRIEND_REQUESTS);
                    if (requestIds != null) {
                        for (String userId : requestIds) {
                            loadUserData(userId);
                        }
                    }
                });
    }

    private void loadUserData(String userId) {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.setId(documentSnapshot.getId());
                        friendRequests.add(user);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onAccept(User user) {
        friendManager.acceptFriendRequest(currentUserId, user.getId())
                .addOnSuccessListener(aVoid -> {
                    // Xóa user khỏi danh sách lời mời kết bạn
                    friendRequests.remove(user);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDecline(User user) {
        friendManager.declineFriendRequest(currentUserId, user.getId())
                .addOnSuccessListener(aVoid -> {
                    // Xóa user khỏi danh sách lời mời kết bạn
                    friendRequests.remove(user);
                    adapter.notifyDataSetChanged();
                    showToast("Đã từ chối lời mời kết bạn");
                })
                .addOnFailureListener(e -> {
                    showToast("Lỗi: " + e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

