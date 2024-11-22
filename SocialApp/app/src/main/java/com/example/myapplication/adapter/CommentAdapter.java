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

import com.example.myapplication.activities.post.ImageViewActivity;
import com.example.myapplication.databinding.ItemCommentBinding;
import com.example.myapplication.models.Comment;
import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.setCommentData(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        CommentViewHolder(ItemCommentBinding itemCommentBinding) {
            super(itemCommentBinding.getRoot());
            binding = itemCommentBinding;
        }

        void setCommentData(Comment comment) {
            binding.textUsername.setText(comment.getUserName());
            if (comment.getContent() != null && !comment.getContent().isEmpty()) {
                binding.textCommentContent.setText(comment.getContent());
            } else {
                binding.textCommentContent.setVisibility(View.GONE);
            }
            binding.textTimestamp.setText(comment.getFormattedDate());

            if (comment.getUserImage() != null && !comment.getUserImage().isEmpty()) {
                Glide.with(binding.imageProfile.getContext())
                        .load(comment.getUserImage())
                        .circleCrop()
                        .into(binding.imageProfile);
            }

            if (comment.getImageUrl() != null && !comment.getImageUrl().isEmpty()) {
                binding.imageCommentContent.setVisibility(View.VISIBLE);
                Glide.with(binding.imageCommentContent.getContext())
                        .load(comment.getImageUrl())
                        .into(binding.imageCommentContent);

                binding.imageCommentContent.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ImageViewActivity.class);
                    intent.putExtra("imageUrl", comment.getImageUrl());
                    v.getContext().startActivity(intent);
                });
            } else {
                binding.imageCommentContent.setVisibility(View.GONE);
            }
        }
    }
}
