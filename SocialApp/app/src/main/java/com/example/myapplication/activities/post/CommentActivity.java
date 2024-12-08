package com.example.myapplication.activities.post;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myapplication.adapter.CommentAdapter;
import com.example.myapplication.databinding.ActivityCommentBinding;
import com.example.myapplication.models.Comment;
import com.example.myapplication.models.Post;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private ActivityCommentBinding binding;
    private Post post;
    private List<Comment> comments;
    private CommentAdapter commentAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ListenerRegistration commentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityCommentBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            preferenceManager = new PreferenceManager(getApplicationContext());
            database = FirebaseFirestore.getInstance();

            post = (Post) getIntent().getSerializableExtra("post");
            if (post == null) {
                Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setupPostView();
            setupCommentsList();
            setListeners();
            loadComments();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void setupPostView() {

        if (post.getUserName() != null) {
            binding.layoutPost.textUsername.setText(post.getUserName());
        }
        if (post.getFormattedDate() != null) {
            binding.layoutPost.textTimestamp.setText(post.getFormattedDate());
        }
        if (post.getContent() != null) {
            binding.layoutPost.textPostContent.setText(post.getContent());
        }
        if (post.getUserImage() != null) {
            Glide.with(binding.layoutPost.imageProfile.getContext())
                    .load(post.getUserImage())
                    .circleCrop()
                    .into(binding.layoutPost.imageProfile);
        }

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            binding.layoutPost.imagePostContent.setVisibility(View.VISIBLE);
            Glide.with(binding.layoutPost.imagePostContent.getContext())
                    .load(post.getImageUrl())
                    .into(binding.layoutPost.imagePostContent);
        } else {
            binding.layoutPost.imagePostContent.setVisibility(View.GONE);
        }

        // Set up like button and count
        binding.layoutPost.buttonLike.setSelected(post.isLikedBy(preferenceManager.getString(Constants.KEY_USER_ID)));
        binding.layoutPost.textLikeCount.setText(String.valueOf(post.getLikes()));

        binding.layoutPost.textCommentCount.setText(String.valueOf(post.getCommentCount()));
        // Disable comment button in this view
        // binding.layoutPost.buttonComment.setVisibility(View.GONE);
    }

    private void setupCommentsList() {
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(comments);
        binding.recyclerViewComments.setAdapter(commentAdapter);
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setListeners() {
        binding.buttonClose.setOnClickListener(v -> closeAndRefresh());
        binding.buttonAddImage.setOnClickListener(v -> openFileChooser());
        binding.buttonSendComment.setOnClickListener(v -> sendComment());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // You might want to show a preview of the selected image
        }
    }
    private void closeAndRefresh() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("refreshRequired", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void sendComment() {
        String commentText = binding.editTextComment.getText().toString().trim();
        if (commentText.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Please enter a comment or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImage(commentText);
        } else {
            saveCommentToDatabase(commentText, null);
        }
    }

    private void uploadImage(String commentText) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "comment_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveCommentToDatabase(commentText, uri.toString())))
                .addOnFailureListener(e -> Toast.makeText(CommentActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void saveCommentToDatabase(String commentText, String imageUrl) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        String userName = preferenceManager.getString(Constants.KEY_NAME);
        String userImage = preferenceManager.getString(Constants.KEY_IMAGE);

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", userId);
        commentData.put("userName", userName);
        commentData.put("userImage", userImage);
        commentData.put("content", commentText);
        commentData.put("imageUrl", imageUrl);
        commentData.put("timestamp", Timestamp.now());

        database.collection("posts").document(post.getId())
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CommentActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                    binding.editTextComment.setText("");
                    imageUri = null;
                    DocumentReference postRef = database.collection("posts").document(post.getId());
                    postRef.update("commentCount", post.getCommentCount() + 1)
                            .addOnSuccessListener(aVoid -> {
                                post.setCommentCount(post.getCommentCount() + 1);
                                updateCommentCount();
                            });
                    loadComments();
                })
                .addOnFailureListener(e -> Toast.makeText(CommentActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show());
    }

    private void updateCommentCount() {
        binding.layoutPost.textCommentCount.setText(String.valueOf(post.getCommentCount()));
    }
    private void loadComments() {
        commentsListener = database.collection("posts").document(post.getId())
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(CommentActivity.this, "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    comments.clear();
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Comment comment = dc.getDocument().toObject(Comment.class);
                        comment.setId(dc.getDocument().getId());

                        switch (dc.getType()) {
                            case ADDED:
                                comments.add(comment);
                                commentAdapter.notifyItemInserted(comments.size() - 1);
                                break;
                            case MODIFIED:
                                int modifiedIndex = comments.indexOf(comment);
                                if (modifiedIndex != -1) {
                                    comments.set(modifiedIndex, comment);
                                    commentAdapter.notifyItemChanged(modifiedIndex);
                                }
                                break;
                            case REMOVED:
                                int removedIndex = comments.indexOf(comment);
                                if (removedIndex != -1) {
                                    comments.remove(removedIndex);
                                    commentAdapter.notifyItemRemoved(removedIndex);
                                }
                                break;
                        }
                    }
                    Collections.sort(comments, (c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()));

                    commentAdapter.notifyDataSetChanged();
                });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }


}
