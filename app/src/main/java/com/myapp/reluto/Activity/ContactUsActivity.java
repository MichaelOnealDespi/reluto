package com.myapp.reluto.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.myapp.reluto.R;

public class ContactUsActivity extends AppCompatActivity {

    private boolean emailSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // Find the back button and social media TextViews
        ImageView backButton = findViewById(R.id.backbtn);

        // Set an OnClickListener for the back button to close the activity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close ContactUsActivity and return to the previous activity
            }
        });

        // Facebook link
        TextView facebookInfo = findViewById(R.id.facebook_info);
        facebookInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String facebookUrl = "https://www.facebook.com/profile.php?viewas=100000686899395&id=61569980063398";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                startActivity(intent);
            }
        });

        // Instagram link
        TextView instagramInfo = findViewById(R.id.instagram_info);
        instagramInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instagramUrl = "https://www.instagram.com/relutoph/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl));
                startActivity(intent);
            }
        });

        // Gmail link
        TextView emailInfo = findViewById(R.id.email_info);
        emailInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gmail email URL
                String email = "mailto:reluto2024@gmail.com";
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
                emailSent = true; // Set a flag indicating the email activity was launched
                startActivity(intent);
            }
        });
    }

}
