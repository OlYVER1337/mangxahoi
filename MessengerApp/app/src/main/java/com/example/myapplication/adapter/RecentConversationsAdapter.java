package com.example.myapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemContainerRecentConversionBinding;
import com.example.myapplication.listeners.ConversionListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRecentConversionBinding binding = ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ConversionViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerRecentConversionBinding binding;
        public ConversionViewHolder(ItemContainerRecentConversionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            Glide.with(binding.imageProfile.getContext())
                    .load(chatMessage.conversionImage)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.imageProfile);
            binding.textName.setText(chatMessage.conversionName); // Changed from conversionId to conversionName
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionClick(user);
            });
        }
    }

}
