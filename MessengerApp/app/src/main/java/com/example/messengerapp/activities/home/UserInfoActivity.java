package com.example.messengerapp.activities.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.messengerapp.utilities.Constants;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messengerapp.databinding.ActivityUserInfoBinding;
import com.example.messengerapp.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfoActivity extends AppCompatActivity  {


    private ActivityUserInfoBinding  binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        loadUserInfo();
        setListeners();
        handleNickname();
    }




    private void setListeners() {

        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }

    private void loadUserInfo() {
        String name = getIntent().getStringExtra(Constants.Key_RECEIVER_NAME);
        String imageUrl = getIntent().getStringExtra(Constants.Key_RECEIVER_IMAGE);
        String userId = getIntent().getStringExtra(Constants.Key_RECEIVER_ID);

        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String nicknamesJson = prefs.getString("nicknames", "{}");

        try {
            JSONObject nicknames = new JSONObject(nicknamesJson);
            String uniqueKey = preferenceManager.getString(Constants.Key_EMAIL)+"_"+userId;
            String nickname = nicknames.optString(uniqueKey, name);

            binding.nameTextView.setText(nickname.isEmpty() ? name : nickname);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(binding.profileImageView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void handleNickname() {
        String userID = getIntent().getStringExtra(Constants.Key_RECEIVER_ID);
        String name = getIntent().getStringExtra(Constants.Key_RECEIVER_NAME);
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String nicknamesJson = prefs.getString("nicknames", "{}");
        try {
            JSONObject nicknames = new JSONObject(nicknamesJson);
            String nickname = nicknames.optString(userID, name);
            String uniqueKey =preferenceManager.getString(Constants.Key_EMAIL)+"_"+userID ;
            binding.saveNicknameButton.setOnClickListener(view -> {
                try {
                    String newNickname = binding.nicknameEditText.getText().toString();
                    if (newNickname.isEmpty()) {
                        nicknames.remove(uniqueKey);
                        binding.nameTextView.setText(name);
                    } else {
                        nicknames.put(uniqueKey, newNickname);
                        binding.nameTextView.setText(newNickname);
                    }
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nicknames", nicknames.toString());
                    editor.apply();
                    Toast.makeText(UserInfoActivity.this, "Cập nhật biệt danh", Toast.LENGTH_SHORT).show();
                    binding.nicknameEditText.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
