package com.example.myapplication.fragments;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.activities.home.AllFriendActivity;
import com.example.myapplication.activities.home.AllFriendRequestActivity;
import com.example.myapplication.adapter.UserSearchAdapter;
import com.example.myapplication.databinding.FragmentFriendBinding;
import com.example.myapplication.adapter.FriendRequestAdapter;
import com.example.myapplication.adapter.FriendListAdapter;
import com.example.myapplication.databinding.FragmentProfileBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.FriendManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FriendFragment extends Fragment implements FriendRequestAdapter.OnFriendRequestActionListener, UserSearchAdapter.OnUserActionListener, FriendListAdapter.OnFriendActionListener {

    private FragmentFriendBinding binding;
    private FriendRequestAdapter friendRequestAdapter;
    private FriendListAdapter friendListAdapter;
    private UserSearchAdapter userSearchAdapter;
    private static final int LIMIT_DISPLAY = 5;
    private List<User> allFriendRequests = new ArrayList<>();
    private List<User> allFriends = new ArrayList<>();
    private List<User> friendRequests;
    private List<User> friendList;
    private List<User> searchResults;
    private FirebaseFirestore db;
    private String currentUserId;
    private FriendManager friendManager;
    private ListenerRegistration friendRequestListener;
    private ListenerRegistration friendListListener;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            binding = FragmentFriendBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e("FriendFragment", "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error loading friend fragment", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            initializeLists();
            setupRecyclerViews();
            setupSearchFunction();
            db = FirebaseFirestore.getInstance();
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            friendManager = new FriendManager();
            setupSnapshotListeners();
            setListeners();
        } catch (Exception e) {
            Log.e("FriendFragment", "Error in onViewCreated", e);
            Toast.makeText(getContext(), "Error initializing friend fragment", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListeners() {
        binding.buttonViewAllFriendRequests.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllFriendRequestActivity.class);
            startActivity(intent);
        });
        binding.buttonViewAllFriends.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllFriendActivity.class);
            startActivity(intent);
        });
    }
    private void setupSnapshotListeners() {
        setupFriendRequestListener();
        setupFriendListListener();

    }

    private void setupFriendRequestListener() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("FriendFragment", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        List<String> requestIds = (List<String>) snapshot.get(Constants.KEY_FRIEND_REQUESTS);
                        updateFriendRequests(requestIds);
                    }
                });
    }

    private void setupFriendListListener() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("FriendFragment", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        List<String> friendIds = (List<String>) snapshot.get(Constants.KEY_FRIENDS);
                        updateFriendList(friendIds);
                    }
                });
    }

    private void updateFriendRequests(List<String> requestIds) {
        allFriendRequests.clear();
        if (requestIds != null && !requestIds.isEmpty()) {
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener(document -> {
                        Map<String, Timestamp> timestamps =
                                (Map<String, com.google.firebase.Timestamp>) document.get(Constants.KEY_FRIEND_REQUEST_TIMESTAMPS);
                        List<Task<User>> tasks = new ArrayList<>();
                        for (String userId : requestIds) {
                            tasks.add(loadUserData(userId, timestamps != null ? timestamps.get(userId) : null));
                        }
                        Tasks.whenAllComplete(tasks)
                                .addOnSuccessListener(task -> {
                                    Collections.sort(allFriendRequests, (u1, u2) -> {
                                        if (u1.getTimestamp() == null || u2.getTimestamp() == null) {
                                            return 0;
                                        }
                                        return u2.getTimestamp().compareTo(u1.getTimestamp());
                                    });
                                    friendRequests.clear();
                                    friendRequests.addAll(allFriendRequests.subList(0, Math.min(allFriendRequests.size(), LIMIT_DISPLAY)));
                                    friendRequestAdapter.notifyDataSetChanged();
                                });
                    });
        }
    }
    private void updateFriendList(List<String> friendIds) {
        allFriends.clear();
        if (friendIds != null && !friendIds.isEmpty()) {
            List<Task<User>> tasks = new ArrayList<>();
            for (String userId : friendIds) {
                tasks.add(loadFriendData(userId));
            }
            Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener(task -> {
                        friendList.clear();
                        friendList.addAll(allFriends.subList(0, Math.min(allFriends.size(), LIMIT_DISPLAY)));
                        friendListAdapter.notifyDataSetChanged();
                    });
        } else {
            friendList.clear();
            friendListAdapter.notifyDataSetChanged();
        }
    }



    private Task<User> loadUserData(String userId, com.google.firebase.Timestamp timestamp) {
        return db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            User user = new User();
                            user.setId(document.getId());
                            user.setName(document.getString(Constants.KEY_NAME));
                            user.setEmail(document.getString(Constants.KEY_EMAIL));
                            user.setImage(document.getString(Constants.KEY_IMAGE));
                            user.setTimestamp(timestamp);
                            allFriendRequests.add(user);
                            return user;
                        }
                    }
                    return null;
                });
    }



    private Task<User> loadFriendData(String userId) {
        return db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            User user = new User();
                            user.setId(document.getId());
                            user.setName(document.getString(Constants.KEY_NAME));
                            user.setEmail(document.getString(Constants.KEY_EMAIL));
                            user.setImage(document.getString(Constants.KEY_IMAGE));
                            allFriends.add(user);
                            return user;
                        }
                    }
                    return null;
                });
    }

    private void showAllFriendRequests() {
        friendRequests.clear();
        friendRequests.addAll(allFriendRequests);
        friendRequestAdapter.notifyDataSetChanged();
    }

    private void showAllFriends() {
        friendList.clear();
        friendList.addAll(allFriends);
        friendListAdapter.notifyDataSetChanged();
    }


    private void initializeLists() {
        friendRequests = new ArrayList<>();
        friendList = new ArrayList<>();
        searchResults = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        friendRequestAdapter = new FriendRequestAdapter(friendRequests, this);
        binding.recyclerViewFriendRequests.setAdapter(friendRequestAdapter);
        binding.recyclerViewFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        friendListAdapter = new FriendListAdapter(friendList, this);
        binding.recyclerViewFriendList.setAdapter(friendListAdapter);
        binding.recyclerViewFriendList.setLayoutManager(new LinearLayoutManager(getContext()));


        userSearchAdapter = new UserSearchAdapter(searchResults, this);
        binding.recyclerViewSearchResults.setAdapter(userSearchAdapter);
        binding.recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    private void setupSearchFunction() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> searchUsers(s.toString());
                searchHandler.postDelayed(searchRunnable, 300); // Đợi 300ms sau khi người dùng ngừng gõ
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String searchQuery) {
        Log.d("FriendFragment", "Starting search for query: " + searchQuery);
        final String query = searchQuery.toLowerCase().trim();
        if (query.isEmpty()) {
            searchResults.clear();
            userSearchAdapter.notifyDataSetChanged();
            return;
        }

        searchResults.clear();

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(currentUserDocument -> {
                    List<String> friendIds = (List<String>) currentUserDocument.get(Constants.KEY_FRIENDS);

                    db.collection(Constants.KEY_COLLECTION_USERS)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Task<Boolean>> pendingRequestTasks = new ArrayList<>();
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    User user = document.toObject(User.class);
                                    user.setId(document.getId());
                                    String name = user.getName().toLowerCase();
                                    String email = user.getEmail().toLowerCase();

                                    if ((name.contains(query) || email.contains(query)) &&
                                            !user.getId().equals(currentUserId) &&
                                            (friendIds == null || !friendIds.contains(user.getId()))) {
                                        searchResults.add(user);
                                        Task<Boolean> pendingRequestTask = friendManager.checkPendingRequest(currentUserId, user.getId())
                                                .addOnSuccessListener(hasPendingRequest -> {
                                                    user.setHasPendingRequest(hasPendingRequest);
                                                });
                                        pendingRequestTasks.add(pendingRequestTask);
                                    }
                                }
                                Tasks.whenAllComplete(pendingRequestTasks)
                                        .addOnCompleteListener(task -> {
                                            userSearchAdapter.notifyDataSetChanged();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FriendFragment", "Error searching users", e);
                                showToast("Error searching users");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FriendFragment", "Error getting current user's friends", e);
                    showToast("Error getting friend list");
                });
    }



    @Override
    public void onAccept(User user) {
        if (user == null || user.getId() == null) {
            Log.e("FriendFragment", "Invalid user data");
            showToast("Invalid user data");
            return;
        }

        friendManager.acceptFriendRequest(currentUserId, user.getId())
                .addOnSuccessListener(aVoid -> {
                    friendRequests.remove(user);
                    friendList.add(user);
                    friendRequestAdapter.notifyDataSetChanged();
                    friendListAdapter.notifyDataSetChanged();
                    showToast("Friend request accepted");
                })
                .addOnFailureListener(e -> {
                    Log.e("FriendFragment", "Error accepting friend request", e);
                    showToast("Failed to accept friend request");
                });
    }

    @Override
    public void onDeleteFriend(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa bạn bè")
                .setMessage("Bạn có chắc chắn muốn xóa " + user.getName() + " khỏi danh sách bạn bè?")
                .setPositiveButton("Có", (dialog, which) -> {
                    friendManager.removeFriend(currentUserId, user.getId())
                            .addOnSuccessListener(aVoid -> {
                                friendList.remove(user);
                                friendListAdapter.notifyDataSetChanged();
                                showToast("Đã xóa kết bạn với " + user.getName());
                            })
                            .addOnFailureListener(e -> {
                                showToast("Lỗi: " + e.getMessage());
                            });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onDecline(User user) {
        friendManager.declineFriendRequest(currentUserId, user.getId())
                .addOnSuccessListener(aVoid -> {
                    friendRequests.remove(user);
                    friendRequestAdapter.notifyDataSetChanged();
                    showToast("Friend request declined");
                })
                .addOnFailureListener(e -> showToast("Failed to decline friend request"));
    }
    @Override
    public void onAddFriend(User user) {
        friendManager.sendFriendRequest(currentUserId, user.getId())
                .addOnSuccessListener(aVoid -> {
                    user.setHasPendingRequest(true);
                    userSearchAdapter.notifyDataSetChanged();
                    showToast("Friend request sent to " + user.getName());
                })
                .addOnFailureListener(e -> showToast("Failed to send friend request"));
    }

    @Override
    public void onCancelFriend(User user) {
        friendManager.cancelFriendRequest(currentUserId, user.getId())
                .addOnSuccessListener(aVoid -> {
                    user.setHasPendingRequest(false);
                    userSearchAdapter.notifyDataSetChanged();
                    showToast("Friend request to " + user.getName() + " cancelled");
                })
                .addOnFailureListener(e -> showToast("Failed to cancel friend request"));
    }

    private boolean isFriend(User user) {
        return friendList.contains(user);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendRequestListener != null) {
            friendRequestListener.remove();
        }
        if (friendListListener != null) {
            friendListListener.remove();
        }
    }
}