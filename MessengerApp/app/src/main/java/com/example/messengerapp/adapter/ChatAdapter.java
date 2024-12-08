package com.example.messengerapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messengerapp.R;
import com.example.messengerapp.listeners.ChatAdapterListener;
import com.example.messengerapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String receiverUserImage;
    private final Context context;
    private final String senderId;
    private final ChatAdapterListener listener;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages, String receiverUserImage, String senderId, ChatAdapterListener listener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.receiverUserImage = receiverUserImage;
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
            ((ReceivedMessageViewHolder) holder).setData(chatMessage, receiverUserImage, listener);
        }
        // Thêm sự kiện nhấn giữ để xóa tin nhắn
        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Xóa tin nhắn")
                    .setMessage("Bạn có chắc chắn muốn xóa tin nhắn này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        listener.onDeleteMessage(chatMessage); // Gọi phương thức xóa
                    })
                    .setNegativeButton("Không", null)
                    .show();
            return true;
        });
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
                Glide.with(imageMessage.getContext())
                        .load(chatMessage.fileUrl)
                        .into(imageMessage);
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
        void setData(ChatMessage chatMessage, String receiverUserImage, ChatAdapterListener listener) {
            if ("media".equals(chatMessage.fileType) && chatMessage.fileUrl != null) {
                textMessage.setVisibility(View.GONE);
                imageMessage.setVisibility(View.VISIBLE);
                Glide.with(imageMessage.getContext())
                        .load(chatMessage.fileUrl)
                        .into(imageMessage);
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
            if (receiverUserImage != null && !receiverUserImage.isEmpty()) {
                Glide.with(imageProfile.getContext())
                        .load(receiverUserImage)
                        .placeholder(R.drawable.default_user_image)
                        .error(R.drawable.default_user_image)
                        .circleCrop()
                        .into(imageProfile);
                imageProfile.setVisibility(View.VISIBLE);
            } else {
                imageProfile.setImageResource(R.drawable.default_user_image);
            }
        }
    }
}
