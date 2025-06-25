package com.myapp.reluto.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.myapp.reluto.R;
import com.myapp.reluto.databinding.ActivityIntroBinding;

public class IntroActivity extends BaseActivity {
    ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVariable();
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
    }

    private void setVariable() {
        // Keep click listener for loginBtn
        binding.loginBtn.setOnClickListener(view -> showDisclaimerDialog());
    }

    private void showDisclaimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disclaimer");
        builder.setMessage("By proceeding, you acknowledge that you have read and understood the disclaimer.\n" +
                "1. Recipes are provided for informational purposes only. The platform is not responsible for any health issues, including illness or allergic reactions, from using these recipes.\n" +
                "2. Users must ensure that ingredients are safe and properly handled. The platform is not liable for issues resulting from improper food preparation or storage.\n" +
                "3. The platform is not liable for any harm or loss resulting from recipe use, including health issues or financial losses.\n" +
                "4. You understand that any violation of the platform's policies may result in account suspension.\n\n" +
                "Do you agree to this disclaimer?");
        builder.setPositiveButton("Agree", (dialog, which) -> {
            // Proceed to the LoginActivity
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
