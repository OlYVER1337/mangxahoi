package com.example.myapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemUserSearchBinding;
import com.example.myapplication.models.User;
import com.bumptech.glide.Glide;
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
            String displayName = user.getName() != null ? user.getName() : "Unknown User";
            binding.textName.setText(displayName);

            if (user.getImage() != null) {
                if (user.getImage().startsWith("http")) {
                    // Xử lý URL từ Firebase Storage
                    Glide.with(binding.imageProfile.getContext())
                            .load(user.getImage())
                            .placeholder(R.drawable.default_user_image)
                            .error(R.drawable.default_user_image)
                            .circleCrop()
                            .into(binding.imageProfile);
                } else {
                    // Xử lý ảnh mã hóa Base64
                    try {
                        byte[] bytes = Base64.decode(user.getImage(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        binding.imageProfile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        // Nếu không phải Base64 hợp lệ, hiển thị ảnh mặc định
                        binding.imageProfile.setImageResource(R.drawable.default_user_image);
                    }
                }
            } else {
                binding.imageProfile.setImageResource(R.drawable.default_user_image);
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

    public interface OnUserActionListener {
        void onAddFriend(User user);
        void onCancelFriend(User user);
    }
}
