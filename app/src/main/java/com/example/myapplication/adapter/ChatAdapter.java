package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.listeners.ChatAdapterListener;
import com.example.myapplication.models.ChatMessage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final Context context;
    private final String senderId;
    private final ChatAdapterListener listener;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, ChatAdapterListener listener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.listener = listener;
    }
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return 1;
        } else {
            return 2;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new SentMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_contaitner_sent_message, parent, false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_contaitner_received_message, parent, false
                    )
            );
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        if (getItemViewType(position) == 1) {
            ((SentMessageViewHolder) holder).setData(chatMessage, listener);
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessage, receiverProfileImage, listener);
        }
    }
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final ImageView imageMessage;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            imageMessage = itemView.findViewById(R.id.imageMessage);
        }
        void setData(ChatMessage chatMessage, ChatAdapterListener listener) {
            if ("media".equals(chatMessage.fileType) && chatMessage.fileUrl != null) {
                textMessage.setVisibility(View.GONE);
                imageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(chatMessage.fileUrl).into(imageMessage);
                imageMessage.setOnClickListener(v -> listener.onOpenFile(chatMessage.fileUrl));
                imageMessage.setOnLongClickListener(v -> {
                    listener.onDownloadFile(chatMessage.fileUrl);
                    return true;
                });
            } else if ("file".equals(chatMessage.fileType) && chatMessage.fileUrl != null) {
                textMessage.setVisibility(View.VISIBLE);
                imageMessage.setVisibility(View.GONE);
                textMessage.setText(chatMessage.message); // Assuming you have a placeholder text for files
                textMessage.setOnClickListener(v -> listener.onOpenFile(chatMessage.fileUrl));
                textMessage.setOnLongClickListener(v -> {
                    listener.onDownloadFile(chatMessage.fileUrl);
                    return true;
                });
            } else {
                textMessage.setVisibility(View.VISIBLE);
                imageMessage.setVisibility(View.GONE);
                textMessage.setText(chatMessage.message);
            }
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final ImageView imageMessage;
        private final ImageView imageProfile;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            imageMessage = itemView.findViewById(R.id.imageMessage);
            imageProfile = itemView.findViewById(R.id.imageProfile);
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage, ChatAdapterListener listener) {
            if ("media".equals(chatMessage.fileType) && chatMessage.fileUrl != null) {
                textMessage.setVisibility(View.GONE);
                imageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(chatMessage.fileUrl).into(imageMessage);
                imageMessage.setOnClickListener(v -> listener.onOpenFile(chatMessage.fileUrl));
                imageMessage.setOnLongClickListener(v -> {
                    listener.onDownloadFile(chatMessage.fileUrl);
                    return true;
                });
            } else if ("file".equals(chatMessage.fileType) && chatMessage.fileUrl != null) {
                textMessage.setVisibility(View.VISIBLE);
                imageMessage.setVisibility(View.GONE);
                textMessage.setText(chatMessage.message); // Giả sử bạn có văn bản giữ chỗ cho các tệp
                textMessage.setOnClickListener(v -> listener.onOpenFile(chatMessage.fileUrl));
                textMessage.setOnLongClickListener(v -> {
                    listener.onDownloadFile(chatMessage.fileUrl);
                    return true;
                });
            } else {
                textMessage.setVisibility(View.VISIBLE);
                imageMessage.setVisibility(View.GONE);
                textMessage.setText(chatMessage.message);
            }
            if (receiverProfileImage != null) {
                imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
