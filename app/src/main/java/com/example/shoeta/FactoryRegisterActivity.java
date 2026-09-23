package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class FactoryRegisterActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private EditText etCompanyName, etPhone, etPassword, etLocation,etProprietorName;
    private ProgressBar progressBar;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_register);

        queue = Volley.newRequestQueue(this);

        etCompanyName = findViewById(R.id.etCompanyName);
        etPhone       = findViewById(R.id.etPhone);
        etPassword    = findViewById(R.id.etPassword);
        etLocation    = findViewById(R.id.etLocation);
        progressBar   = findViewById(R.id.progressBar);
        etProprietorName   = findViewById(R.id.etProprietorName);

        findViewById(R.id.btnRegister).setOnClickListener(v -> doRegister());
        findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("user_type", "factory");
            startActivity(i);
        });
    }

    private void doRegister() {
        String company = etCompanyName.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        String pass    = etPassword.getText().toString().trim();
        String loc     = etLocation.getText().toString().trim();
        String proprietorName = etProprietorName.getText().toString().trim();

        if (!RegistrationValidator.isFactoryRegistrationValid(company, phone, pass)) {
            Toast.makeText(this, "নাম, ফোন এবং পাসওয়ার্ড আবশ্যক (পাসওয়ার্ড কমপক্ষে ৬ অক্ষর)", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "register.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                Intent i = new Intent(FactoryRegisterActivity.this, FactoryPendingActivity.class);
                                i.putExtra("status", "pending");
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(FactoryRegisterActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(FactoryRegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(FactoryRegisterActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_type",    "factory");
                params.put("company_name", company);
                params.put("phone",        phone);
                params.put("password",     pass);
                params.put("location",     loc);
                params.put("proprietor",   proprietorName );
                return params;
            }
        };
        queue.add(req);
    }
}