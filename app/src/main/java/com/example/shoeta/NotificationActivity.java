package com.example.shoeta;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        RecyclerView recycler = findViewById(R.id.recyclerNotifications);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
        String json = prefs.getString("notifications", "[]");

        recycler.setAdapter(new NotifAdapter(json));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    static class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {
        private final JSONArray items;

        NotifAdapter(String json) {
            JSONArray arr;
            try { arr = new JSONArray(json); } catch (Exception e) { arr = new JSONArray(); }
            items = arr;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_notification, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            try {
                JSONObject o = items.getJSONObject(pos);
                h.tvTitle.setText(o.optString("title", ""));
                h.tvBody.setText(o.optString("body", ""));
            } catch (Exception ignored) {}
        }

        @Override public int getItemCount() { return items.length(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBody;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvNotifTitle);
                tvBody  = v.findViewById(R.id.tvNotifBody);
            }
        }
    }
}