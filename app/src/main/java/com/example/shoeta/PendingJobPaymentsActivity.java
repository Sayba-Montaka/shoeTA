package com.example.shoeta;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class PendingJobPaymentsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private RecyclerView recycler;
    private LinearLayout layoutEmpty;
    private final List<JSONObject> paymentList = new ArrayList<>();
    private PaymentAdapter adapter;
    private SessionManager sm;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_job_payments);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        recycler    = findViewById(R.id.recyclerPendingPayments);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(paymentList, queue, this, BASE_URL, sm.getUserId());
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadPending();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPending();
    }

    private void loadPending() {
        String url = BASE_URL + "admin_settings.php?action=list_pending_payments&admin_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONObject(response).getJSONArray("jobs");
                        paymentList.clear();
                        for (int i = 0; i < arr.length(); i++) paymentList.add(arr.getJSONObject(i));
                        adapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(paymentList.isEmpty() ? View.VISIBLE : View.GONE);
                    } catch (Exception e) {
                        Toast.makeText(PendingJobPaymentsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(PendingJobPaymentsActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show());
        req.setShouldCache(false);
        queue.add(req);
    }

    // ─── Inner adapter ────────────────────────────────────────
    static class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.VH> {
        private final List<JSONObject> items;
        private final RequestQueue queue;
        private final android.content.Context ctx;
        private final String baseUrl;
        private final int adminId;

        PaymentAdapter(List<JSONObject> items, RequestQueue queue, android.content.Context ctx,
                       String baseUrl, int adminId) {
            this.items = items; this.queue = queue; this.ctx = ctx;
            this.baseUrl = baseUrl; this.adminId = adminId;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_pending_payment, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JSONObject j = items.get(pos);
            try {
                int jobId = j.getInt("id");
                h.tvCompany.setText(j.optString("company_name", "—"));
                h.tvJobTitle.setText("পদ: " + j.optString("job_title", "—"));
                h.tvPhone.setText("📞 " + j.optString("phone", "—"));
                h.tvDuration.setText("সময়কাল: " + j.optInt("duration_weeks", 0) + " সপ্তাহ ("
                        + j.optInt("tokens_used", 0) + " ফ্রি টোকেন ব্যবহৃত)");
                h.tvAmount.setText("💰 ৳" + j.optString("payment_amount", "0"));
                h.tvTxnId.setText("Transaction ID: " + j.optString("payment_ref", "—"));

                h.btnVerify.setOnClickListener(v -> decide(jobId, "verify_job_payment", h));
                h.btnReject.setOnClickListener(v -> decide(jobId, "reject_job_payment", h));
            } catch (Exception ignored) {}
        }

        private void decide(int jobId, String action, VH h) {
            String url = baseUrl + "admin_settings.php";

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            if (new JSONObject(response).getBoolean("success")) {
                                int idx = h.getAdapterPosition();
                                if (idx != RecyclerView.NO_POSITION) {
                                    items.remove(idx);
                                    notifyItemRemoved(idx);
                                }
                                Toast.makeText(ctx,
                                        action.equals("verify_job_payment") ? "যাচাই করা হয়েছে, পোস্ট প্রকাশিত" : "প্রত্যাখ্যান করা হয়েছে",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ctx, new JSONObject(response).optString("message", "ব্যর্থ হয়েছে"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ignored) {}
                    },
                    error -> Toast.makeText(ctx, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> p = new HashMap<>();
                    p.put("action", action);
                    p.put("job_id", String.valueOf(jobId));
                    p.put("admin_id", String.valueOf(adminId));
                    return p;
                }
            };
            queue.add(req);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCompany, tvJobTitle, tvPhone, tvDuration, tvAmount, tvTxnId;
            Button btnVerify, btnReject;
            VH(View v) {
                super(v);
                tvCompany  = v.findViewById(R.id.tvPayCompany);
                tvJobTitle = v.findViewById(R.id.tvPayJobTitle);
                tvPhone    = v.findViewById(R.id.tvPayPhone);
                tvDuration = v.findViewById(R.id.tvPayDuration);
                tvAmount   = v.findViewById(R.id.tvPayAmount);
                tvTxnId    = v.findViewById(R.id.tvPayTxnId);
                btnVerify  = v.findViewById(R.id.btnVerifyPayment);
                btnReject  = v.findViewById(R.id.btnRejectPayment);
            }
        }
    }
}