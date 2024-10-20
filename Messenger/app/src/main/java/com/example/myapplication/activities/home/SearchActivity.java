package com.example.myapplication.activities.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.activities.chat.ChatActivity;
import com.example.myapplication.adapter.RecentConversationsAdapter;
import com.example.myapplication.databinding.ActivitySearchBinding;
import com.example.myapplication.listeners.ConversionListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.User;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements ConversionListener {
    private ActivitySearchBinding binding;
    private List<ChatMessage> allConversations;
    private List<ChatMessage> searchResults;
    private RecentConversationsAdapter conversationAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        allConversations = getIntent().getParcelableArrayListExtra("conversations");
        searchResults = new ArrayList<>();
        conversationAdapter = new RecentConversationsAdapter(searchResults, this);

        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.searchRecyclerView.setAdapter(conversationAdapter);

        setListeners();
        setupImageBack();
    }
    private void setListeners() {
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterConversations(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
    private void setupImageBack() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void filterConversations(String query) {
        searchResults.clear();
        for (ChatMessage conversation : allConversations) {
            if (conversation.conversionName.toLowerCase().contains(query.toLowerCase()) ||
                    conversation.message.toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(conversation);
            }
        }
        conversationAdapter.notifyDataSetChanged();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onConversionClick(User user) {
        Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}
