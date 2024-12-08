package com.example.messengerapp.listeners;

import com.example.messengerapp.models.ChatMessage;

public interface ChatAdapterListener {
    void onOpenFile(String fileUrl);
    void onDownloadFile(String fileUrl);
    void onDeleteMessage(ChatMessage chatMessage);
}
