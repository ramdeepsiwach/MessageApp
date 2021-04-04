package com.se_p2.messageapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.se_p2.messageapp.model.UserModel;

import java.util.Random;

import androidx.core.app.NotificationCompat;

public class Common {
    public static final String USERS_REF = "USERS";
    public static final String CHATS ="CHATS" ;
    public static final String TOKENS = "TOKENS";
    public static final String CHATLIST = "Chatlist";


    public static UserModel currentUser;

}
