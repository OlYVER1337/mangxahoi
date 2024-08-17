package com.example.myapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constants;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.databinding.ActivityUserInfoBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.example.myapplication.utilities.PreferenceManager;

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
        String image = getIntent().getStringExtra(Constants.Key_RECEIVER_IMAGE);
        String userId = getIntent().getStringExtra(Constants.Key_RECEIVER_ID);

        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String nicknamesJson = prefs.getString("nicknames", "{}");

        try {
            JSONObject nicknames = new JSONObject(nicknamesJson);
            String uniqueKey = preferenceManager.getString(Constants.Key_EMAIL)+"_"+userId ; ;
            String nickname = nicknames.optString(uniqueKey, name);

            if (nickname.isEmpty()){
                binding.nameTextView.setText(name);
            }else {
                binding.nameTextView.setText(nickname);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (image != null && !image.isEmpty()) {
            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.profileImageView.setImageBitmap(bitmap);
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
                    Toast.makeText(UserInfoActivity.this, "Nickname updated", Toast.LENGTH_SHORT).show();
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
