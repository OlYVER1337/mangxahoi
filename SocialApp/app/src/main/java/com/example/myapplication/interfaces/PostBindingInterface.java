package com.example.myapplication.interfaces;

import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.LayoutCreatePostBinding;

public interface PostBindingInterface {
    void setLayoutCreatePost(LayoutCreatePostBinding layoutCreatePost);
    void setRecyclerViewPosts(RecyclerView recyclerViewPosts);
    void setTextViewNoPosts(TextView textViewNoPosts);
}
