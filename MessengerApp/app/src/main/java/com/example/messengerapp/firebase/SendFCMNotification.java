package com.example.messengerapp.firebase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendFCMNotification {
    public static void sendNotification(String fcmToken, String accessToken, String title,String chat) throws Exception {
        String fcmUrl = "https://fcm.googleapis.com/v1/projects/easychat-7788b/messages:send";
        URL url = new URL(fcmUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);

        String message = "{"
                + "\"message\": {"
                + "\"token\":\"" + fcmToken + "\","
                + "\"notification\": {"
                +"\"title\":\"" + title + "\","
                + "\"title\":\"" + chat + "\","
                + "}"
                + "}"
                + "}";

        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            outputStream.write(message.getBytes());
            outputStream.flush();
        }

        int responseCode = httpURLConnection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
    }
}
