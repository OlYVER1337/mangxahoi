package com.example.myapplication.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.activities.post.CommentActivity;
import com.example.myapplication.activities.post.ImageViewActivity;
import com.example.myapplication.databinding.ItemPostBinding;
import com.example.myapplication.models.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private String currentUserId;
    private OnLikeClickListener likeClickListener;
    private OnCommentClickListener commentClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Post post, int position);
    }

    public interface OnCommentClickListener {
        void onCommentClick(Post post);
    }
    public interface OnLikeClickListener {
        void onLikeClick(Post post, int position);
    }

    public PostAdapter(List<Post> posts, String currentUserId, OnLikeClickListener likeClickListener, OnCommentClickListener commenClicktListener, OnDeleteClickListener deleteClickListener) {
        this.posts = posts;
        this.currentUserId = currentUserId;
        this.likeClickListener = likeClickListener;
        this.commentClickListener = commenClicktListener;
        this.deleteClickListener = deleteClickListener;
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.setPostData(posts.get(position));
    }



    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;

        PostViewHolder(ItemPostBinding itemPostBinding) {
            super(itemPostBinding.getRoot());
            binding = itemPostBinding;
        }

        void setPostData(Post post) {
            binding.textUsername.setText(post.getUserName());
            binding.textTimestamp.setText(post.getFormattedDate());
            binding.textPostContent.setText(post.getContent());
            binding.textLikeCount.setText(String.valueOf(post.getLikes()));

            if (post.getUserImage() != null) {
                byte[] decodedString = Base64.decode(post.getUserImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.imageProfile.setImageBitmap(decodedByte);
            }

            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                binding.imagePostContent.setVisibility(View.VISIBLE);
                Picasso.get().load(post.getImageUrl()).into(binding.imagePostContent);
                binding.imagePostContent.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ImageViewActivity.class);
                    intent.putExtra("imageUrl", post.getImageUrl());
                    v.getContext().startActivity(intent);
                });
            } else {
                binding.imagePostContent.setVisibility(View.GONE);
            }
            updateLikeButton(post);

            binding.buttonLike.setOnClickListener(v -> {
                if (likeClickListener != null) {
                    likeClickListener.onLikeClick(post, getAdapterPosition());
                }
            });
            binding.buttonComment.setOnClickListener(v -> {
                if (commentClickListener != null) {
                    commentClickListener.onCommentClick(post);
                }
            });
            binding.textCommentCount.setText(String.valueOf(post.getCommentCount()));
            if (post.getUserId().equals(currentUserId)) {
                binding.buttonDelete.setVisibility(View.VISIBLE);
                binding.buttonDelete.setOnClickListener(v -> {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(post, getAdapterPosition());
                    }
                });
            } else {
                binding.buttonDelete.setVisibility(View.GONE);
            }
        }


        void updateLikeButton(Post post) {
            binding.buttonLike.setSelected(post.isLikedBy(currentUserId));
            binding.textLikeCount.setText(String.valueOf(post.getLikes()));
        }

    }
}