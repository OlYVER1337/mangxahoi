package com.example.myapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.myapplication.activities.home.BaseActivity;
import com.example.myapplication.activities.post.CommentActivity;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.models.Post;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.myapplication.interfaces.PostBindingInterface;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment implements PostAdapter.OnLikeClickListener, PostAdapter.OnCommentClickListener, PostAdapter.OnDeleteClickListener {

    private FragmentHomeBinding binding;
    private Set<String> addedPostIds = new HashSet<>();

    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private ListenerRegistration postsListener;
    private List<Post> posts;
    private PostAdapter postAdapter;
    private Uri imageUri;
    private Set<String> friendIds;
    private boolean isLoading = false;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int COMMENT_ACTIVITY_REQUEST_CODE = 1;
    private static final int POSTS_PER_PAGE = 10;
    private DocumentSnapshot lastVisible;
    private boolean isLastItemReached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());
        init();
        setListeners();
        loadFriendIds();
    }


   /*
   private void updatePostsWithCommentCount() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection(Constants.KEY_COLLECTION_POSTS)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String postId = document.getId();
                    db.collection(Constants.KEY_COLLECTION_POSTS)
                        .document(postId)
                        .collection("comments")
                        .get()
                        .addOnSuccessListener(commentSnapshots -> {
                            int commentCount = commentSnapshots.size();
                            document.getReference().update("commentCount", commentCount)
                                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Comment count updated for post: " + postId))
                                .addOnFailureError(e -> Log.e("HomeFragment", "Error updating comment count for post: " + postId, e));
                        })
                        .addOnFailureListener(e -> Log.e("HomeFragment", "Error getting comments for post: " + postId, e));
                }
            } else {
                Log.e("HomeFragment", "Error getting posts: ", task.getException());
            }
        });
}


   private void updateOldPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_POSTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            if (!document.contains("likes") || !document.contains("likedBy")) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("likes", 0);
                                updates.put("likedBy", new ArrayList<String>());

                                document.getReference().update(updates)
                                        .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Document updated successfully"))
                                        .addOnFailureListener(e -> Log.e("HomeFragment", "Error updating document", e));
                            }
                        }
                    } else {
                        Log.e("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }*/

    private void init() {
        posts = new ArrayList<>();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        postAdapter = new PostAdapter(posts, currentUserId, this,this, this);
        binding.recyclerViewPosts.setAdapter(postAdapter);
        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseFirestore.getInstance();
        friendIds = new HashSet<>();
    }
    @Override
    public void onDeleteClick(Post post, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> deletePost(post, position))
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void deletePost(Post post, int position) {
        database.collection(Constants.KEY_COLLECTION_POSTS)
                .document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    removePost(post.getId());
                    showToast("Bài viết đã được xóa");
                })
                .addOnFailureListener(e -> showToast("Không thể xóa bài viết: " + e.getMessage()));
    }

    @Override
    public void onCommentClick(Post post) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CommentActivity.class);
            intent.putExtra("post", post);
            startActivityForResult(intent, COMMENT_ACTIVITY_REQUEST_CODE);
        }
    }
    @Override
    public void onLikeClick(Post post, int position) {
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        post.toggleLike(currentUserId);
        postAdapter.notifyItemChanged(position);

        // Cập nhật Firestore
        updateLikeInFirestore(post);
    }
    private void updateLikeInFirestore(Post post) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_POSTS)
                .document(post.getId())
                .update(
                        "likes", post.getLikes(),
                        "likedBy", post.getLikedBy()
                )
                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Like updated successfully"))
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error updating like", e));
    }

    private void setListeners() {
        binding.layoutCreatePost.buttonAddImage.setOnClickListener(v -> openFileChooser());
        binding.layoutCreatePost.buttonPost.setOnClickListener(v -> uploadPost());
    }


    private void loadFriendIds() {
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    friendIds.add(currentUserId); // Add current user's ID
                    List<String> friends = (List<String>) documentSnapshot.get(Constants.KEY_FRIENDS);
                    if (friends != null) {
                        friendIds.addAll(friends);
                    }
                    Log.d("HomeFragment", "FriendIds: " + friendIds);
                    setupPostsListener();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to load friends");
                });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn Ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.layoutCreatePost.imageViewPreview.setImageURI(imageUri);
            binding.layoutCreatePost.imageViewPreview.setVisibility(View.VISIBLE);
        }
        if (requestCode == COMMENT_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getBooleanExtra("refreshRequired", false)) {
                refreshPosts();
            }
        }
    }

    private void refreshPosts() {
        // Xóa danh sách bài đăng hiện tại
        posts.clear();
        addedPostIds.clear();
        postAdapter.notifyDataSetChanged();

        // Tải lại dữ liệu
        setupPostsListener();
    }

    private void uploadPost() {
        String postText = binding.layoutCreatePost.editTextPost.getText().toString().trim();
        if (TextUtils.isEmpty(postText) && imageUri == null) {
            showToast("Vui lòng nhập nội dung hoặc chọn ảnh");
            return;
        }

        loading(true);
        if (imageUri != null) {
            uploadImage(imageUri, postText);
        } else {
            savePostToDatabase(null, postText);
        }
    }

    private void uploadImage(Uri imageUri, String postText) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if (userId == null || userId.isEmpty()) {
            showToast("Không thể xác định người dùng");
            return;
        }

        loading(true);
        showToast("Đang tải ảnh lên...");

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "images/" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    savePostToDatabase(imageUrl, postText);
                }))
                .addOnFailureListener(e -> {
                    loading(false);
                    Log.e("UploadError", "Error uploading image", e);
                    showToast("Không thể tải lên ảnh: " + e.getMessage());
                });
    }

    private void setupPostsListener() {
        if (postsListener != null) {
            postsListener.remove();
        }

        Query query = database.collection(Constants.KEY_COLLECTION_POSTS)
                .whereIn(Constants.KEY_USER_ID, new ArrayList<>(friendIds))
                .orderBy(Constants.KEY_POST_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(15);

        Log.d("HomeFragment", "Query: " + query.toString());
        postsListener = query.addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            Log.e("HomeFragment", "Listen failed.", error);
            return;
        }
        if (value != null) {
            for (DocumentChange dc : value.getDocumentChanges()) {
                String postId = dc.getDocument().getId();
                Log.d("HomeFragment", "Document change type: " + dc.getType() + ", PostId: " + postId);
                switch (dc.getType()) {
                    case ADDED:
                        if (!addedPostIds.contains(postId)) {
                            Post post = createPostFromDocument(dc.getDocument());
                            posts.add(0, post);
                            addedPostIds.add(postId);
                            Log.d("HomeFragment", "Added new post: " + postId);
                        }
                        break;
                    case MODIFIED:
                        updatePost(dc.getDocument());
                        break;
                    case REMOVED:
                        removePost(postId);
                        break;
                }
            }
            Collections.sort(posts, (p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()));
            postAdapter.notifyDataSetChanged();
            updatePostsVisibility();
            Log.d("HomeFragment", "Total posts: " + posts.size());
        }
    };


    private Post createPostFromDocument(DocumentSnapshot document) {
        Post post = new Post();
        post.setId(document.getId());
        post.setUserId(document.getString(Constants.KEY_USER_ID));
        post.setContent(document.getString(Constants.KEY_POST_CONTENT));
        post.setImageUrl(document.getString(Constants.KEY_POST_IMAGE));
        post.setUserName(document.getString(Constants.KEY_USER_NAME));
        post.setUserImage(document.getString(Constants.KEY_USER_IMAGE));
        post.setTimestamp(document.getTimestamp(Constants.KEY_POST_TIMESTAMP));
        post.setLikes(document.getLong("likes") != null ? document.getLong("likes").intValue() : 0);
        post.setLikedBy((List<String>) document.get("likedBy"));
        post.setCommentCount(document.getLong("commentCount") != null ? document.getLong("commentCount").intValue() : 0);
        return post;
    }


    private void updatePost(DocumentSnapshot document) {
        String postId = document.getId();
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                Post updatedPost = createPostFromDocument(document);
                posts.set(i, updatedPost);
                postAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void removePost(String postId) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                posts.remove(i);
                addedPostIds.remove(postId);
                postAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }






    private void savePostToDatabase(String imageUrl, String postText) {
        Map<String, Object> post = new HashMap<>();
        post.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        post.put(Constants.KEY_POST_CONTENT, postText);
        post.put(Constants.KEY_USER_NAME, preferenceManager.getString(Constants.KEY_NAME));
        post.put(Constants.KEY_USER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        post.put(Constants.KEY_POST_TIMESTAMP, Timestamp.now());
        post.put("likes", 0); // Thêm trường likes
        post.put("likedBy", new ArrayList<String>()); // Thêm trường likedBy
        post.put("commentCount", 0);
        if (imageUrl != null) {
            post.put(Constants.KEY_POST_IMAGE, imageUrl);
        }

        database.collection(Constants.KEY_COLLECTION_POSTS)
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    showToast("Đăng bài thành công");
                    binding.layoutCreatePost.editTextPost.getText().clear();
                    binding.layoutCreatePost.imageViewPreview.setVisibility(View.GONE);
                    imageUri = null;

                    setupPostsListener();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast("Không thể đăng bài: " + e.getMessage());
                });
    }




    private void updatePostsVisibility() {
        if (posts.isEmpty()) {
            binding.textViewNoPosts.setVisibility(View.VISIBLE);
            binding.recyclerViewPosts.setVisibility(View.GONE);
        } else {
            binding.textViewNoPosts.setVisibility(View.GONE);
            binding.recyclerViewPosts.setVisibility(View.VISIBLE);
        }
    }


    private void showNoPosts() {
        binding.textViewNoPosts.setVisibility(View.VISIBLE);
        binding.recyclerViewPosts.setVisibility(View.GONE);
    }

    private void hideNoPosts() {
        binding.textViewNoPosts.setVisibility(View.GONE);
        binding.recyclerViewPosts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).updateAvailabilityToOnline();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).updateAvailabilityToOffline();
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.layoutCreatePost.buttonPost.setVisibility(View.INVISIBLE);
            binding.layoutCreatePost.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.layoutCreatePost.buttonPost.setVisibility(View.VISIBLE);
            binding.layoutCreatePost.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postsListener != null) {
            postsListener.remove();
        }
        binding = null;
    }


}
/**/