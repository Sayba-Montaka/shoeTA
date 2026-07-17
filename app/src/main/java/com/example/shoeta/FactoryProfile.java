package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FactoryProfile extends AppCompatActivity {

    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_profile);

        sm = new SessionManager(this);

        TextView tvName  = findViewById(R.id.tvFactoryName);
        TextView tvPhone = findViewById(R.id.tvFactoryPhone);
        TextView tvId    = findViewById(R.id.tvFactoryId);
        TextView tvType  = findViewById(R.id.tvFactoryType);
        TextView tvProprietor  = findViewById(R.id.tvProprietor);

        tvName.setText(sm.getUserName());
        tvPhone.setText(sm.getUserPhone());
        tvId.setText(String.valueOf(sm.getUserId()));
        tvType.setText(sm.getUserType());
        tvProprietor.setText(sm.getProprietorName());


    }
}