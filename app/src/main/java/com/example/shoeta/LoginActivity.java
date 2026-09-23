package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class LoginActivity extends AppCompatActivity {

    private String userType;
    private EditText etPhone, etPassword;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userType = getIntent().getStringExtra("user_type");
        sm       = new SessionManager(this);

        etPhone    = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        TextView tvTitle      = findViewById(R.id.tvTitle);
        TextView tvPhoneLabel = findViewById(R.id.tvPhoneLabel);
        Button   btnLogin     = findViewById(R.id.btnLogin);
        TextView tvRegLink    = findViewById(R.id.tvRegisterLink);

        switch (userType) {
            case "admin":
                tvTitle.setText("Admin Login");
                tvPhoneLabel.setText("Username");
                etPhone.setHint("admin");
                etPhone.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                tvRegLink.setVisibility(android.view.View.GONE);
                break;
            case "factory":
                tvTitle.setText("কারখানা লগইন\nFactory Login");
                tvRegLink.setText("নতুন কারখানা? নিবন্ধন করুন / Register");
                break;
            default:
                tvTitle.setText("স্বাগতম!\nWelcome Back");
                tvRegLink.setText("নতুন? নিবন্ধন করুন / Register here");
        }

        btnLogin.setOnClickListener(v -> doLogin());

        tvRegLink.setOnClickListener(v -> {
            Intent i = new Intent(this,
                    "factory".equals(userType)
                            ? FactoryRegisterActivity.class
                            : WorkerRegisterActivity.class);
            startActivity(i);
        });

        //blur view
        BlurView glassCardView = findViewById(R.id.glassCard);

        ViewGroup rootView = findViewById(android.R.id.content);

        glassCardView.setupWith(rootView, new RenderScriptBlur(this))
                .setFrameClearDrawable(getWindow().getDecorView().getBackground())
                .setBlurRadius(20f);

        glassCardView.setClipToOutline(true);
    }

    private void doLogin() {

        String phoneOrUser = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!RegistrationValidator.isLoginInputValid(phoneOrUser, password)) {
            Toast.makeText(this, "সব তথ্য পূরণ করুন", Toast.LENGTH_SHORT).show();
            return;
        }

        String url ="https://blood-bridge.org/shoeTA/login.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.d("RESPONSE", s);

                try {
                    JSONObject obj = new JSONObject(s);

                    if (obj.getBoolean("success")) {
                        String proprietorName = obj.optString("proprietor_name", "");
                        sm.createSession(
                                obj.getInt("id"),
                                obj.getString("name"),
                                obj.getString("user_type"),
                                phoneOrUser,
                                proprietorName
                        );
                        if ("worker".equals(obj.getString("user_type"))) {
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            try {
                                                sendTokenToServer(task.getResult(), obj.getInt("id"));
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    });
                        }

                        Intent i;

                        switch (obj.getString("user_type")) {
                            case "admin":
                                i = new Intent(LoginActivity.this, AdminPanelActivity.class);
                                break;

                            case "factory":
                                i = new Intent(LoginActivity.this, FactoryDashboardActivity.class);
                                break;

                            default:
                                i = new Intent(LoginActivity.this, HomeWorkerActivity.class);
                                break;
                        }

                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();

                    } else {
                        String message = obj.getString("message");
                        if ("PENDING_APPROVAL".equals(message)) {
                            Intent i = new Intent(LoginActivity.this, FactoryPendingActivity.class);
                            i.putExtra("status", "pending");
                            startActivity(i);
                        } else if ("REJECTED".equals(message)) {
                            Intent i = new Intent(LoginActivity.this, FactoryPendingActivity.class);
                            i.putExtra("status", "rejected");
                            startActivity(i);
                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("NETWORK_ERROR", volleyError.toString());
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map params = new HashMap<String,String>();
                params.put("user_type", userType);
                params.put("password", password);

                if ("admin".equals(userType)) {
                    params.put("username", phoneOrUser);
                } else {
                    params.put("phone", phoneOrUser);
                }
                Log.d("LOGIN_DATA", params.toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(stringRequest);
    }
    private void sendTokenToServer(String token, int workerId) {
        String url = "https://blood-bridge.org/shoeTA/save_token.php";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {}, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("worker_id", String.valueOf(workerId));
                p.put("token", token);
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }
}