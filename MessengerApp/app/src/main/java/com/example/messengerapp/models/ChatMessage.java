package com.example.messengerapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ChatMessage implements Parcelable {
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String fileUrl;

    public String conversionId, conversionName, conversionImage, fileType,file;

    // Constructor mặc định
    public ChatMessage() {}

    // Constructor cho Parcelable
    protected ChatMessage(Parcel in) {
        senderId = in.readString();
        receiverId = in.readString();
        message = in.readString();
        dateTime = in.readString();
        conversionId = in.readString();
        conversionName = in.readString();
        conversionImage = in.readString();
        fileType = in.readString();
        dateObject = new Date(in.readLong());
        fileUrl = in.readString(); // Đọc fileUrl từ Parcel
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(senderId);
        dest.writeString(receiverId);
        dest.writeString(message);
        dest.writeString(dateTime);
        dest.writeString(conversionId);
        dest.writeString(conversionName);
        dest.writeString(conversionImage);
        dest.writeString(fileType);
        dest.writeLong(dateObject != null ? dateObject.getTime() : -1);
        dest.writeString(fileUrl); // Ghi fileUrl vào Parcel
    }
}
