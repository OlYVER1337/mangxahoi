package com.example.myapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemFriendRequestBinding;
import com.example.myapplication.models.User;
import com.bumptech.glide.Glide;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<User> friendRequests;
    private OnFriendRequestActionListener listener;

    public interface OnFriendRequestActionListener {
        void onAccept(User user);
        void onDecline(User user);
    }

    public FriendRequestAdapter(List<User> friendRequests, OnFriendRequestActionListener listener) {
        this.friendRequests = friendRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendRequestBinding binding = ItemFriendRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(friendRequests.get(position));
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private ItemFriendRequestBinding binding;

        RequestViewHolder(ItemFriendRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            if (user.getImage() != null) {
                Glide.with(binding.imageProfile.getContext())
                        .load(user.getImage())
                        .circleCrop()
                        .into(binding.imageProfile);
            }
            binding.textTimestamp.setText(user.getFormattedDate());
            binding.textName.setText(user.getName());
            binding.buttonAccept.setOnClickListener(v -> listener.onAccept(user));
            binding.buttonDecline.setOnClickListener(v -> listener.onDecline(user));
        }
    }
}

