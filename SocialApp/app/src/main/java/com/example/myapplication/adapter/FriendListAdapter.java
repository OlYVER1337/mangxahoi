package com.example.myapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.User;
import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<User> friendList;
    private OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onDeleteFriend(User user);
    }

    public FriendListAdapter(List<User> friendList, OnFriendActionListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = friendList.get(position);
        holder.textName.setText(user.getName());
        if (user.getImage() != null) {
            Bitmap bitmap = getUserImage(user.getImage());
            holder.imageProfile.setImageBitmap(bitmap);
        }
        holder.buttonDeleteFriend.setOnClickListener(v -> listener.onDeleteFriend(user));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageView imageProfile;
        Button buttonDeleteFriend;

        ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            buttonDeleteFriend = itemView.findViewById(R.id.buttonDeleteFriend);
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
