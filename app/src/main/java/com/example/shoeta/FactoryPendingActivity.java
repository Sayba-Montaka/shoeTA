package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FactoryPendingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_pending);

        String status = getIntent().getStringExtra("status");
        TextView tvIcon    = findViewById(R.id.tvStatusIcon);
        TextView tvTitle   = findViewById(R.id.tvStatusTitle);
        TextView tvMessage = findViewById(R.id.tvStatusMessage);

        if ("rejected".equals(status)) {
            tvIcon.setText("❌");
            tvTitle.setText("আবেদন প্রত্যাখ্যাত / Application Rejected");
            tvMessage.setText("দুঃখিত, আপনার কারখানার আবেদন অনুমোদিত হয়নি। আরও তথ্যের জন্য সহায়তা কেন্দ্রে যোগাযোগ করুন।\n\nSorry, your factory application was not approved. Please contact support for details.");
        } else {
            tvIcon.setText("⏳");
            tvTitle.setText("অনুমোদনের অপেক্ষায় / Awaiting Approval");
            tvMessage.setText("আপনার প্রোফাইল পর্যালোচনার জন্য পাঠানো হয়েছে। অ্যাডমিন অনুমোদন করলে আপনি লগইন করতে পারবেন।\n\nYour profile has been sent for review. You'll be able to log in once an admin approves it.");
        }

        findViewById(R.id.btnBackToLogin).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}