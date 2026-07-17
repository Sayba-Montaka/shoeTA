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

public class PendingFactoriesActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private RecyclerView recycler;
    private LinearLayout layoutEmpty;
    private final List<JSONObject> factoryList = new ArrayList<>();
    private FactoryAdapter adapter;
    private SessionManager sm;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_factories);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        recycler    = findViewById(R.id.recyclerPending);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FactoryAdapter(factoryList, queue, this, BASE_URL, sm.getUserId());
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
        String url = BASE_URL + "admin_settings.php?action=list_pending_factories&admin_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONObject(response).getJSONArray("factories");
                        factoryList.clear();
                        for (int i = 0; i < arr.length(); i++) factoryList.add(arr.getJSONObject(i));
                        adapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(factoryList.isEmpty() ? View.VISIBLE : View.GONE);
                    } catch (Exception e) {
                        Toast.makeText(PendingFactoriesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(PendingFactoriesActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show());
        queue.add(req);
    }

    // ─── Inner adapter ────────────────────────────────────────
    static class FactoryAdapter extends RecyclerView.Adapter<FactoryAdapter.VH> {
        private final List<JSONObject> items;
        private final RequestQueue queue;
        private final android.content.Context ctx;
        private final String baseUrl;
        private final int adminId;

        FactoryAdapter(List<JSONObject> items, RequestQueue queue, android.content.Context ctx,
                       String baseUrl, int adminId) {
            this.items = items; this.queue = queue; this.ctx = ctx;
            this.baseUrl = baseUrl; this.adminId = adminId;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_pending_factory, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JSONObject f = items.get(pos);
            try {
                int factoryId = f.getInt("id");
                h.tvCompany.setText(f.optString("company_name", "—"));
                h.tvProprietorCompany.setText(f.optString("proprietor", "—"));
                h.tvPhone.setText("📞 " + f.optString("phone", "—"));
                h.tvLocation.setText("📍 " + f.optString("location", "—"));

                h.btnApprove.setOnClickListener(v -> decide(factoryId, "approve_factory", h));
                h.btnReject.setOnClickListener(v -> decide(factoryId, "reject_factory", h));
            } catch (Exception ignored) {}
        }

        private void decide(int factoryId, String action, VH h) {
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
                                        action.equals("approve_factory") ? "অনুমোদিত হয়েছে" : "প্রত্যাখ্যাত হয়েছে",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ctx, new JSONObject(response).optString("message", "ব্যর্থ হয়েছে"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ignored) {}
                    },
                    error -> Toast.makeText(ctx, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> p = new HashMap<>();
                    p.put("action", action);
                    p.put("factory_id", String.valueOf(factoryId));
                    p.put("admin_id", String.valueOf(adminId));
                    return p;
                }
            };
            queue.add(req);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCompany, tvPhone, tvLocation,tvProprietorCompany;
            Button btnApprove, btnReject;
            VH(View v) {
                super(v);
                tvCompany  = v.findViewById(R.id.tvPendingCompany);
                tvPhone    = v.findViewById(R.id.tvPendingPhone);
                tvLocation = v.findViewById(R.id.tvPendingLocation);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject  = v.findViewById(R.id.btnReject);
                tvProprietorCompany  = v.findViewById(R.id.tvProprietorCompany);
            }
        }
    }
}