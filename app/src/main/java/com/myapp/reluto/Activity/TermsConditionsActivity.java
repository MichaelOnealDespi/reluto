package com.myapp.reluto.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myapp.reluto.R;

public class TermsConditionsActivity extends AppCompatActivity {

    public static final int RESULT_ACCEPTED = 1;
    public static final int RESULT_REJECTED = 0;

    private Button acceptButton;
    private Button rejectButton;
    private TextView termsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);
        termsTextView = findViewById(R.id.termsTextView);

        acceptButton.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            setResult(RESULT_ACCEPTED, resultIntent);
            finish();
        });

        rejectButton.setOnClickListener(view -> {
            Toast.makeText(TermsConditionsActivity.this, "You must accept the terms to proceed.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_REJECTED);
            finish();
        });
    }
}
