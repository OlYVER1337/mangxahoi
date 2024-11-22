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
import com.example.myapplication.databinding.ItemFriendBinding;
import com.example.myapplication.models.User;
import com.bumptech.glide.Glide;
import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {

    private List<User> friendList;
    private OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onDeleteFriend(User user);
    }

    public FriendListAdapter(List<User> friendList, OnFriendActionListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendBinding binding = ItemFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private ItemFriendBinding binding;

        FriendViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.textName.setText(user.getName());
            if (user.getImage() != null) {
                Glide.with(binding.imageProfile.getContext())
                        .load(user.getImage())
                        .circleCrop()
                        .into(binding.imageProfile);
            }
            binding.buttonDeleteFriend.setOnClickListener(v -> listener.onDeleteFriend(user));
        }
    }
}

