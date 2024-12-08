package com.example.messengerapp.activities.home;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.messengerapp.utilities.Constants;
import com.example.messengerapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        documentReference=database.collection(Constants.Key_COLLECTION_USER).document(preferenceManager.getString(Constants.Key_USER_ID));

    }
    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.Key_AVAILABILITY,0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.Key_AVAILABILITY,1);
    }
}
