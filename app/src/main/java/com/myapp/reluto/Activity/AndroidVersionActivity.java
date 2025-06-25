package com.myapp.reluto.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.myapp.reluto.R;

public class AndroidVersionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_version);

        // Find the back button using its ID
        ImageView backButton = findViewById(R.id.backbtn);

        // Set an OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close ContactUsActivity and return to the previous activity
            }
        });
    }
}