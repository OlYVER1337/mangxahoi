package com.example.myapplication.listeners;

import com.example.myapplication.models.ChatMessage;

public interface ChatAdapterListener {
    void onOpenFile(String fileUrl);
    void onDownloadFile(String fileUrl);
    void onDeleteMessage(ChatMessage chatMessage);
}
