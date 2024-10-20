package com.example.myapplication.utilities;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.Collections;

import android.content.Context;

import java.io.InputStream;


public class FCMOAuth {
    public static String getAccessToken(Context context) throws IOException {
        // Mở tệp JSON từ thư mục assets
        InputStream inputStream = context.getAssets().open("easychat-7788b-c6fc84a2094d.json");  // Đặt tên đúng với tệp JSON của bạn
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(inputStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
