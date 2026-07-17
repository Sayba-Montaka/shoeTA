package com.example.shoeta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String notificationBody  = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();

            String timeStr = remoteMessage.getData().get("time");
            long notificationTime = System.currentTimeMillis();
            if (timeStr != null) {
                try { notificationTime = Long.parseLong(timeStr); }
                catch (NumberFormatException e) { e.printStackTrace(); }
            }

            saveNotification(notificationTitle, notificationBody, notificationTime);

            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    notificationTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            sendNotification(notificationTitle, notificationBody + " • " + timeAgo);

            Intent intent = new Intent("NEW_NOTIFICATION");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("TOKEN_F", "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        SessionManager sm = new SessionManager(this);
        if (!sm.isLoggedIn() || !sm.isWorker()) return; // only workers need this for now

        String url = BASE_URL + "save_token.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("TOKEN_SERVER", "Token saved: " + response),
                error -> Log.e("TOKEN_SERVER", "Error saving token: " + error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("worker_id", String.valueOf(sm.getUserId()));
                params.put("token", token);

                Log.d("TOKEN_F", "Refreshed token: " + token);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "ShoeTA Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void saveNotification(String title, String body, long time) {
        SharedPreferences prefs = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
        String json = prefs.getString("notifications", "[]");

        try {
            JSONArray array = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("title", title);
            obj.put("body", body);
            obj.put("time", time);

            JSONArray newArray = new JSONArray();
            newArray.put(obj);
            for (int i = 0; i < array.length(); i++) newArray.put(array.getJSONObject(i));

            if (newArray.length() > 15) {
                JSONArray limited = new JSONArray();
                for (int i = 0; i < 15; i++) limited.put(newArray.getJSONObject(i));
                newArray = limited;
            }

            prefs.edit().putString("notifications", newArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}