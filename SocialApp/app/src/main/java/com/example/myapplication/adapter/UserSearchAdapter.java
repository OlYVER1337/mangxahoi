package com.example.myapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.databinding.ItemUserSearchBinding;
import com.example.myapplication.models.User;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<User> users;
    private OnUserActionListener listener;

    public UserSearchAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserSearchBinding binding = ItemUserSearchBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemUserSearchBinding binding;

        UserViewHolder(ItemUserSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            if (user.getImage() != null) {
                Bitmap bitmap = getUserImage(user.getImage());
                binding.imageProfile.setImageBitmap(bitmap);
            }
            binding.textName.setText(user.getName());
            if (user.hasPendingRequest()) {
                binding.buttonAddFriend.setVisibility(View.GONE);
                binding.buttonCancelFriend.setVisibility(View.VISIBLE);
                binding.buttonCancelFriend.setOnClickListener(v -> listener.onCancelFriend(user));
            } else {
                binding.buttonAddFriend.setVisibility(View.VISIBLE);
                binding.buttonCancelFriend.setVisibility(View.GONE);
                binding.buttonAddFriend.setOnClickListener(v -> listener.onAddFriend(user));
            }
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public interface OnUserActionListener {
        void onAddFriend(User user);
        void onCancelFriend(User user);
    }
}
