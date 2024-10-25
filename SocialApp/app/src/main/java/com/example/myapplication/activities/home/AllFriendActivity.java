package com.example.myapplication.activities.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.FriendListAdapter;
import com.example.myapplication.databinding.ActivityAllFriendsBinding;
import com.example.myapplication.databinding.ActivityChangePasswordBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.FriendManager;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AllFriendActivity extends AppCompatActivity implements FriendListAdapter.OnFriendActionListener {

    private ActivityAllFriendsBinding binding;
    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private List<User> friends;
    private FirebaseFirestore db;
    private String currentUserId;
    private FriendManager friendManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = binding.recyclerViewAllFriends;
        friends = new ArrayList<>();
        adapter = new FriendListAdapter(friends, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        friendManager = new FriendManager();

        db = FirebaseFirestore.getInstance();
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        loadAllFriends();
        setupBackClickListener();
    }

    private void setupBackClickListener() {
        binding.buttonBack.setOnClickListener(v -> finish());
    }
    private void loadAllFriends() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> friendIds = (List<String>) documentSnapshot.get(Constants.KEY_FRIENDS);
                    if (friendIds != null) {
                        for (String userId : friendIds) {
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
                        friends.add(user);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDeleteFriend(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bạn bè")
                .setMessage("Bạn có chắc chắn muốn xóa " + user.getName() + " khỏi danh sách bạn bè?")
                .setPositiveButton("Có", (dialog, which) -> {
                    friendManager.removeFriend(currentUserId, user.getId())
                            .addOnSuccessListener(aVoid -> {
                                // Xóa user khỏi danh sách bạn bè
                                friends.remove(user);
                                adapter.notifyDataSetChanged();
                                showToast("Đã xóa bạn bè");
                            })
                            .addOnFailureListener(e -> {
                                showToast("Lỗi: " + e.getMessage());
                            });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

