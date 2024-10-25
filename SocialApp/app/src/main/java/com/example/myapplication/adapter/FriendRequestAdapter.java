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

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<User> friendRequests;
    private OnFriendRequestActionListener listener;

    public interface OnFriendRequestActionListener {
        void onAccept(User user);
        void onDecline(User user);
    }

    public FriendRequestAdapter(List<User> friendRequests, OnFriendRequestActionListener listener) {
        this.friendRequests = friendRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = friendRequests.get(position);
        if (user.getImage() != null) {
            Bitmap bitmap = getUserImage(user.getImage());
            holder.imageProfile.setImageBitmap(bitmap);
        }
        holder.textTimestamp.setText(user.getFormattedDate());
        holder.textName.setText(user.getName());
        holder.buttonAccept.setOnClickListener(v -> listener.onAccept(user));
        holder.buttonDecline.setOnClickListener(v -> listener.onDecline(user));
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textTimestamp;
        Button buttonAccept;
        Button buttonDecline;
        ImageView imageProfile;

        ViewHolder(View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            textName = itemView.findViewById(R.id.textName);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonDecline = itemView.findViewById(R.id.buttonDecline);
        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
