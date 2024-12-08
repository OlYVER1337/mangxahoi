package com.example.messengerapp.utilities;


import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseStorageHelper {

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    public FirebaseStorageHelper() {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    public void uploadImage(Uri imageUri, final OnUploadCompleteListener listener) {
        final StorageReference fileReference = storageReference.child("profile_images/" + imageUri.getLastPathSegment());
        UploadTask uploadTask = fileReference.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                listener.onSuccess(uri.toString());
            });
        }).addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnUploadCompleteListener {
        void onSuccess(String imageUrl);

        void onFailure(String errorMessage);
    }
}
