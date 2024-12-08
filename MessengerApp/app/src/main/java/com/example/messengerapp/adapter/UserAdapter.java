package com.example.messengerapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messengerapp.databinding.ItemContainerUserBinding;
import com.example.messengerapp.listeners.UserListener;
import com.example.messengerapp.models.User;

import java.util.List;
public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.UserViewHolder>{
    private final List<User> users;
    private final UserListener userListener;
    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding =ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));

    }
    @Override
    public int getItemCount() {
        return users.size();
    }
    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding ){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }
        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            Glide.with(binding.imageProfile.getContext())
                    .load(user.image)
                    .circleCrop()
                    .into(binding.imageProfile);
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

}
