package com.se_p2.messageapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se_p2.messageapp.adapter.MessageAdapter;
import com.se_p2.messageapp.model.MessageModel;
import com.se_p2.messageapp.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {

    private static final  int MY_PERMISSION_REQUEST_CODE=1;

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fUser;
    Intent intent;

    ImageButton btn_send;
    EditText txt_send;

    MessageAdapter messageAdapter;
    List<MessageModel> messages;
    RecyclerView recyclerView;

    ValueEventListener seenListener;
    String userId,phoneNo;

    boolean notify = false;
    DatabaseReference reference;

    TelephonyManager myTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        myTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (isTelephonyEnabled()) {
            checkForCallPermission();
        } else {
            Toast.makeText(this, "Telephony not enabled", Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(this, HomeScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));


        recyclerView = findViewById(R.id.recycler_messages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        txt_send = findViewById(R.id.txt_send);

        intent = getIntent();
        userId = intent.getStringExtra("userId");
        phoneNo=intent.getStringExtra("phoneNo");

        btn_send.setOnClickListener(v -> {
            notify = true;
            String message = txt_send.getText().toString();

            if (!message.equals("")) {
                sendMessage(fUser.getUid(), userId, message);
            } else {
                txt_send.setError("Message cannot be empty");
                txt_send.requestFocus();
            }
            txt_send.setText("");

        });

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference(Common.USERS_REF)
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                assert user != null;
                username.setText(user.getName());
                profile_image.setImageResource(R.mipmap.ic_launcher);

                getMessagesFromFirebase(fUser.getUid(), userId, null);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userId);
    }

    private void seenMessage(String userId) {
        reference=FirebaseDatabase.getInstance().getReference(Common.CHATS);
        seenListener=reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel message = snapshot1.getValue(MessageModel.class);
                            if (message.getReceiver().equals(fUser.getUid()) &&
                                    message.getSender().equals(userId)) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("isSeen", "true");
                                snapshot1.getRef().updateChildren(data);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String sender, String receiver, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("sender", sender);
        data.put("receiver", receiver);
        data.put("message", message);
        data.put("isSeen", "false");

        FirebaseDatabase.getInstance().getReference().child(Common.CHATS).push().setValue(data);

        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fUser.getUid())
                .child(userId);

        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    childRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    private void getMessagesFromFirebase(String myId, String userId, String imageUrl) {
        messages = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.CHATS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel message = snapshot1.getValue(MessageModel.class);
                            if (message.getReceiver().equals(myId) && message.getSender().equals(userId)
                                    || message.getReceiver().equals(userId) && message.getSender().equals(myId)) {
                                messages.add(message);
                            }

                            messageAdapter = new MessageAdapter(getApplicationContext(), messages);
                            recyclerView.setAdapter(messageAdapter);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREPS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }


    private void status(String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", status);

        FirebaseDatabase.getInstance().getReference(Common.USERS_REF)
                .child(fUser.getUid()).updateChildren(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.call_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.call_phone) {
            callPhoneNumber();
        }
        return super.onOptionsItemSelected(item);
    }

    public void callPhoneNumber() {
        String phoneNumber = String.format("tel: %s", phoneNo);
        Intent callIntent = new Intent(Intent.ACTION_CALL);

        if (isTelephonyEnabled()) {
            checkForCallPermission();
            callIntent.setData(Uri.parse(phoneNumber));
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(callIntent);
            } else {
                Toast.makeText(this, "Can not resolve the intent for calling", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Telephony not enabled", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isTelephonyEnabled() {
        if (myTelephonyManager != null) {
            if (myTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        }
        return false;
    }

    private void checkForCallPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST_CODE);
        }
    }


}