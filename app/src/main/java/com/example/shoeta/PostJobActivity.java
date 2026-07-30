package com.example.shoeta;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class PostJobActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    // ← put your actual bKash/Nagad "Send Money" number here
    private static final String PAYMENT_NUMBER = "01743598339";

    private Spinner  spinnerJobTitle, spinnerShift, spinnerJobType, spinnerPayment, spinnerGender, spinnerDuration;
    private EditText etWorkersNeeded, etSalaryRate, etContactPhone;
    private CheckBox cbFood, cbOvertime, cbSit;
    private ProgressBar progressBar;
    private TextView tvTokenInfo;
    private RequestQueue queue;

    private final List<String>  titleNames  = new ArrayList<>();
    private final List<Integer> titleIds    = new ArrayList<>();
    private final List<String>  shiftNames  = new ArrayList<>();
    private final List<Integer> shiftIds    = new ArrayList<>();
    private final List<String>  typeNames   = new ArrayList<>();
    private final List<Integer> typeIds     = new ArrayList<>();
    private final List<String>  payNames    = new ArrayList<>();
    private final List<Integer> payIds      = new ArrayList<>();
    private final List<String>  genderOpts  = Arrays.asList("উভয়","পুরুষ", "মহিলা");
    private final List<String>  durationOpts = Arrays.asList(
            "১ সপ্তাহ / 1 Week", "২ সপ্তাহ / 2 Weeks", "৩ সপ্তাহ / 3 Weeks", "৪ সপ্তাহ / 4 Weeks");

    private SessionManager sm;
    private int tokenBalance = -1; // -1 = not yet loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        spinnerJobTitle  = findViewById(R.id.spinnerJobTitle);
        spinnerShift     = findViewById(R.id.spinnerShift);
        spinnerJobType   = findViewById(R.id.spinnerJobType);
        spinnerPayment   = findViewById(R.id.spinnerPayment);
        spinnerGender    = findViewById(R.id.spinnerGender);
        spinnerDuration  = findViewById(R.id.spinnerDuration);
        etWorkersNeeded  = findViewById(R.id.etWorkersNeeded);
        etSalaryRate     = findViewById(R.id.etSalaryRate);
        etContactPhone   = findViewById(R.id.etContactPhone);
        cbFood           = findViewById(R.id.cbFood);
        cbSit            = findViewById(R.id.cbSit);
        cbOvertime       = findViewById(R.id.cbOvertime);
        progressBar      = findViewById(R.id.progressBar);
        tvTokenInfo      = findViewById(R.id.tvTokenInfo);

        etContactPhone.setText(sm.getUserPhone());

        ArrayAdapter<String> ga = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genderOpts);
        ga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(ga);

        ArrayAdapter<String> da = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, durationOpts);
        da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(da);
        spinnerDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateTokenDisplay(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        loadDropdowns();
        loadTokenBalance();
        findViewById(R.id.btnPostJob).setOnClickListener(v -> doPostJob(""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenBalance >= 0) loadTokenBalance();
    }

    private void loadDropdowns() {
        String url = BASE_URL + "admin_settings.php?action=get_dropdowns";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            populate(obj.getJSONArray("job_titles"),      titleNames, titleIds, spinnerJobTitle);
                            populate(obj.getJSONArray("shifts"),          shiftNames, shiftIds,  spinnerShift);
                            populate(obj.getJSONArray("job_types"),       typeNames,  typeIds,   spinnerJobType);
                            populate(obj.getJSONArray("payment_systems"), payNames,   payIds,    spinnerPayment);
                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PostJobActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }

    private void populate(JSONArray arr, List<String> names, List<Integer> ids, Spinner s) throws Exception {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            names.add(o.getString("name")); ids.add(o.getInt("id"));
        }
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }

    /** Fetches this factory's current token balance. */
    private void loadTokenBalance() {
        String url = BASE_URL + "factory_profile.php?factory_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        tokenBalance = obj.getJSONObject("factory").optInt("token_balance", 0);
                    } catch (Exception e) {
                        tokenBalance = 0;
                    }
                    updateTokenDisplay();
                },
                error -> {
                    tokenBalance = 0;
                    updateTokenDisplay();
                });
        req.setShouldCache(false);
        queue.add(req);
    }

    private int getSelectedWeeks() {
        return spinnerDuration.getSelectedItemPosition() + 1;
    }

    private void updateTokenDisplay() {
        if (tokenBalance < 0) {
            tvTokenInfo.setText("টোকেন লোড হচ্ছে...");
            return;
        }
        int weeks = getSelectedWeeks();
        int freeWeeks = Math.min(tokenBalance, weeks);
        int paidWeeks = weeks - freeWeeks;
        int price = paidWeeks * 10;

        if (price == 0) {
            tvTokenInfo.setText("🎟 আপনার টোকেন: " + tokenBalance + " | এই পোস্টে ব্যবহার হবে: " + weeks
                    + " | অবশিষ্ট থাকবে: " + (tokenBalance - weeks));
        } else if (freeWeeks > 0) {
            tvTokenInfo.setText("🎟 " + freeWeeks + " সপ্তাহ ফ্রি টোকেন থেকে (অবশিষ্ট টোকেন: " + tokenBalance
                    + ") + ৳" + price + " (" + paidWeeks + " সপ্তাহ) পরিশোধ করতে হবে");
        } else {
            tvTokenInfo.setText("মূল্য: ৳" + price + " (৳১০ × " + weeks + " সপ্তাহ) — কোনো ফ্রি টোকেন নেই");
        }
    }

    private void showPaymentDialog(int price, int freeWeeks, int paidWeeks) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 16, 48, 16);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Payment Number", PAYMENT_NUMBER);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Payment number copied", Toast.LENGTH_SHORT).show();

        Button btnOpenBkash = new Button(this);
        btnOpenBkash.setText("📲 বিকাশ অ্যাপ খুলুন / Open bKash");
        btnOpenBkash.setOnClickListener(v -> openBkashApp());
        container.addView(btnOpenBkash);

        TextView tvHint = new TextView(this);
        tvHint.setText("টাকা পাঠানোর পর বিকাশের SMS/কনফার্মেশন স্ক্রিনে \"TrxID\" নামে ১০ অক্ষরের একটি কোড পাবেন (যেমন: 8N7A5B3C2D) — সেটাই নিচে লিখুন।");
        tvHint.setTextSize(12f);
        tvHint.setPadding(0, 24, 0, 16);
        container.addView(tvHint);

        final EditText etTransactionId = new EditText(this);
        etTransactionId.setHint("Transaction ID (যেমন: 8N7A5B3C2D)");
        container.addView(etTransactionId);

        String freeNote = freeWeeks > 0
                ? (freeWeeks + " সপ্তাহ আপনার ফ্রি টোকেন থেকে কভার হবে।\n\n")
                : "";

        new AlertDialog.Builder(this)
                .setTitle("পেমেন্ট প্রয়োজন / Payment Required")
                .setMessage(freeNote
                        + "বাকি " + paidWeeks + " সপ্তাহের জন্য মূল্য ৳" + price + "।\n\n"
                        + "এই নম্বরে bKash/Nagad Send Money করুন:\n"
                        + PAYMENT_NUMBER)

                .setView(container)
                .setPositiveButton("জমা দিন / Submit", (dialog, which) -> {
                    String txnId = etTransactionId.getText().toString().trim();
                    if (txnId.isEmpty()) {
                        Toast.makeText(this, "Transaction ID লিখুন", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (txnId.length() < 6) {
                        Toast.makeText(this, "Transaction ID সঠিক মনে হচ্ছে না, আবার চেক করুন", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    doPostJob(txnId);
                })
                .setNegativeButton("বাতিল", null)
                .setCancelable(false)
                .show();
    }

    private void openBkashApp() {
        String bkashPackage = "com.bKash.customerapp";
        android.content.pm.PackageManager pm = getPackageManager();
        android.content.Intent launchIntent = pm.getLaunchIntentForPackage(bkashPackage);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            try {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("market://details?id=" + bkashPackage)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + bkashPackage)));
            }
        }
    }

    private void doPostJob(String paymentRef) {
        if (titleIds.isEmpty()) {
            Toast.makeText(this, "ড্রপডাউন লোড হয়নি, অনুগ্রহ করে আবার চেষ্টা করুন", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "post_job.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("RES_POST_JOB", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                String msg = obj.optString("message", "");
                                if (msg.contains("pending")) {
                                    Toast.makeText(PostJobActivity.this,
                                            "✓ জমা হয়েছে! পেমেন্ট যাচাইয়ের পর পোস্টটি প্রকাশিত হবে।",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(PostJobActivity.this, "✓ চাকরি পোস্ট হয়েছে!", Toast.LENGTH_LONG).show();
                                }
                                finish();
                            } else {
                                String message = obj.getString("message");
                                if ("PAYMENT_REQUIRED".equals(message)) {
                                    int price     = obj.optInt("price", 0);
                                    int freeWeeks = obj.optInt("free_weeks", 0);
                                    int paidWeeks = obj.optInt("paid_weeks", 0);
                                    showPaymentDialog(price, freeWeeks, paidWeeks);
                                } else {
                                    Toast.makeText(PostJobActivity.this, message, Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(PostJobActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("RES_POST_JOB", String.valueOf(error));
                        Toast.makeText(PostJobActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new HashMap<>();
                p.put("factory_id",        String.valueOf(sm.getUserId()));
                p.put("job_title_id",      String.valueOf(titleIds.get(spinnerJobTitle.getSelectedItemPosition())));
                p.put("shift_id",          String.valueOf(shiftIds.get(spinnerShift.getSelectedItemPosition())));
                p.put("job_type_id",       String.valueOf(typeIds.get(spinnerJobType.getSelectedItemPosition())));
                p.put("payment_system_id", String.valueOf(payIds.get(spinnerPayment.getSelectedItemPosition())));
                p.put("workers_needed",    etWorkersNeeded.getText().toString().trim());
                p.put("salary_rate",       etSalaryRate.getText().toString().trim());
                p.put("gender_required",   genderOpts.get(spinnerGender.getSelectedItemPosition()));
                p.put("contact_phone",     etContactPhone.getText().toString().trim());
                p.put("food_accommodation", cbFood.isChecked() ? "1" : "0");
                p.put("shelter",            cbSit.isChecked() ? "1" : "0");
                p.put("overtime",           cbOvertime.isChecked() ? "1" : "0");
                p.put("duration_weeks",     String.valueOf(getSelectedWeeks()));
                p.put("payment_ref",        paymentRef);
                return p;
            }
        };
        queue.add(req);
    }
}