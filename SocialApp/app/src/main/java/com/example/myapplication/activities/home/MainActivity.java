package com.example.myapplication.activities.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.activities.account.SignInActivity;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.fragments.FriendFragment;
import com.example.myapplication.fragments.HomeFragment;
import com.example.myapplication.fragments.ProfileFragment;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends BaseActivity  implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            preferenceManager = new PreferenceManager(getApplicationContext());

            if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
                return;
            }

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            updateToken(task.getResult());
                            binding.bottomNavigationView.setOnNavigationItemSelectedListener(this);
                            loadFragment(new HomeFragment());
                        }
                    });
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate", e);
            Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }





    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        if (item.getItemId() == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (item.getItemId() == R.id.navigation_friends) {
            fragment = new FriendFragment();
        } else if (item.getItemId() == R.id.navigation_profile) {
            fragment = new ProfileFragment();
        }

        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String fragmentToLoad = intent.getStringExtra("fragment");
            if (fragmentToLoad != null) {
                switch (fragmentToLoad) {
                    case "home":
                        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                        break;
                    case "friends":
                        loadFragment(new FriendFragment());
                        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_friends);
                        break;
                    case "profile":
                        loadFragment(new ProfileFragment());
                        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                        break;
                }
            }
        }
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }


    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken)
                .addOnFailureListener(e -> showToast("Không thể lấy token"));
    }

    private void updateToken(String token) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if (userId != null && !userId.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            documentSnapshot.getReference().update(Constants.KEY_FCM_TOKEN, token)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("MainActivity", "Token updated successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("MainActivity", "Token update failed: " + e.getMessage());
                                    });
                        }
                    });
        }
    }




    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
