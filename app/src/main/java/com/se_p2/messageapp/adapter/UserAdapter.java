package com.se_p2.messageapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se_p2.messageapp.Common;
import com.se_p2.messageapp.MessageActivity;
import com.se_p2.messageapp.R;
import com.se_p2.messageapp.model.MessageModel;
import com.se_p2.messageapp.model.UserModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private final Context context;
    private final List<UserModel> users;
    private boolean isChat;

    String lastMsg;

    public UserAdapter(Context context, List<UserModel> users, boolean isChat) {
        this.context = context;
        this.users = users;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.username.setText(users.get(position).getName());

        if(isChat){
            lastMessage(users.get(position).getUserId(),holder.last_msg);
        }else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat) {
            if (users.get(position).getStatus().equals("online")) {
                holder.img_online.setVisibility(View.VISIBLE);
                holder.img_offline.setVisibility(View.GONE);
            } else {
                holder.img_online.setVisibility(View.GONE);
                holder.img_offline.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_online.setVisibility(View.GONE);
            holder.img_offline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userId", users.get(position).getUserId());
            intent.putExtra("phoneNo",users.get(position).getPhone());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        Unbinder unbinder;

        @BindView(R.id.profile_image)
        CircleImageView profile_image;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.last_msg)
        TextView last_msg;
        @BindView(R.id.img_online)
        CircleImageView img_online;
        @BindView(R.id.img_offline)
        CircleImageView img_offline;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }

    public void lastMessage(String userId, TextView last_msg) {
        lastMsg = "Default";
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference(Common.CHATS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel message = snapshot1.getValue(MessageModel.class);
                            if (message.getReceiver().equals(fUser.getUid()) && message.getSender().equals(userId)
                                    || message.getReceiver().equals(userId) && message.getSender().equals(fUser.getUid())) {
                                lastMsg = message.getMessage();
                                if(message.getIsSeen().equals("false")){
                                    last_msg.setTypeface(null, Typeface.BOLD_ITALIC);
                                }
                            }
                        }
                        if ("Default".equals(lastMsg)) {
                            last_msg.setText("No message");
                        } else {
                            last_msg.setText(lastMsg);
                        }
                        lastMsg = "Default";
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
