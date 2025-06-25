package com.myapp.reluto.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.myapp.reluto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class admintechnichal extends AppCompatActivity {

    private static final String TAG = "admintechnichal";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView emailList;
    private Map<String, String> emailStatusMap = new HashMap<>();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admintechnichal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        emailList = findViewById(R.id.emailList);
        sharedPreferences = getSharedPreferences("EmailStatusPrefs", Context.MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.signOutBtn).setOnClickListener(v -> showSignOutConfirmationDialog());

        // Retrieve and display email data
        fetchUserEmails();
    }

    private void showSignOutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> signOut())
                .setNegativeButton("No", null)
                .show();
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void fetchUserEmails() {
        StringBuilder emailBuilder = new StringBuilder();

        // Define column widths and formatting
        final int emailWidth = 40;
        final int userTypeWidth = 20;
        final int nameWidth = 20;
        final int linkWidth = 30;
        final int prcWidth = 20;
        final int verificationWidth = 35;
        final int approvalWidth = 10;
        final int declineWidth = 10;

        final String headerFormat = "%-" + emailWidth + "s %-" + userTypeWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + linkWidth + "s %-" + prcWidth + "s %-" + verificationWidth + "s %-" + approvalWidth + "s %-" + declineWidth + "s\n";

        // Add header row without the "Profile Picture" column
        emailBuilder.append(String.format(headerFormat, "Email Address", "User Type", "Full Name", "Last Name", "Birthdate", "Contact", "Link", "PRC", "Verification", "Approve", "Decline"));

        // Fetch emails from "foodtech" collection first
        db.collection("foodtech").get()
                .addOnCompleteListener(foodtechTask -> {
                    if (foodtechTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : foodtechTask.getResult()) {
                            String photoUrl = document.getString("photoUrl"); // Fetching photoUrl
                            String email = document.getString("email");
                            String fullName = document.getString("fullName");
                            String lastName = document.getString("lastName");
                            String birthdate = document.getString("birthdate");
                            String contact = document.getString("contact");
                            String link = document.getString("link");
                            String prc = document.getString("prc");
                            String verification = "Not Yet Verified";
                            String userType = "FoodTech";

                            // Retrieve stored verification status
                            String verificationStatus = sharedPreferences.getString(email, "Not Yet Verified");
                            emailStatusMap.put(email, verificationStatus);

                            // Add foodtech data
                            if ("Not Yet Verified".equals(verificationStatus)) {
                                emailBuilder.append(String.format(headerFormat,
                                        email != null ? email : "N/A",
                                        userType,
                                        fullName != null ? fullName : "N/A",
                                        lastName != null ? lastName : "N/A",
                                        birthdate != null ? birthdate : "N/A",
                                        contact != null ? contact : "N/A",
                                        link != null ? link : "N/A",
                                        prc != null ? prc : "N/A",
                                        verificationStatus,
                                        "Approve",
                                        "Decline"));
                            } else {
                                emailBuilder.append(String.format(headerFormat,
                                        email != null ? email : "N/A",
                                        userType,
                                        fullName != null ? fullName : "N/A",
                                        lastName != null ? lastName : "N/A",
                                        birthdate != null ? birthdate : "N/A",
                                        contact != null ? contact : "N/A",
                                        link != null ? link : "N/A",
                                        prc != null ? prc : "N/A",
                                        verificationStatus,
                                        "", // No approval
                                        "")); // No decline
                            }

                            // Load the image into the ImageView for each FoodTech
                            // Load the image into the ImageView for each FoodTech
                            // Load the image into the ImageView for each FoodTech
                            if (photoUrl != null) {
                                ImageView photoImageView = new ImageView(this);

                                // Set layout parameters for the ImageView
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        60, // Width in pixels (adjust as needed)
                                        60  // Height in pixels (adjust as needed)
                                );

                                // Add margin to the ImageView
                                int marginInDp = 2; // Change this to adjust spacing
                                float scale = getResources().getDisplayMetrics().density;
                                int marginInPixels = (int) (marginInDp * scale + 0.1f); // Convert DP to pixels
                                params.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);

                                // Apply the parameters to the ImageView
                                photoImageView.setLayoutParams(params);

                                // Load the image using Glide
                                Glide.with(this).load(photoUrl).into(photoImageView);

                                // Ensure you have a layout to add these images
                                LinearLayout imageContainer = findViewById(R.id.imageContainer);
                                imageContainer.addView(photoImageView);
                            }



                            // Add extra new line for spacing
                            emailBuilder.append("\n");
                        }

                        // After "foodtech" is fetched, now fetch from "users" collection
                        db.collection("users").get()
                                .addOnCompleteListener(usersTask -> {
                                    if (usersTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : usersTask.getResult()) {
                                            String email = document.getString("email");
                                            String fullName = document.getString("fullName");
                                            String lastName = document.getString("lastName");
                                            String birthdate = document.getString("birthdate");
                                            String contact = document.getString("contact");
                                            String link = document.getString("link");
                                            String prc = document.getString("prc");
                                            String verification = "Verified";
                                            String userType = "User";

                                            // Add user data
                                            emailBuilder.append(String.format(headerFormat,
                                                    email != null ? email : "N/A",
                                                    userType,
                                                    fullName != null ? fullName : "N/A",
                                                    lastName != null ? lastName : "N/A",
                                                    birthdate != null ? birthdate : "N/A",
                                                    contact != null ? contact : "N/A",
                                                    link != null ? link : "N/A",
                                                    prc != null ? prc : "N/A",
                                                    verification,
                                                    "", // Approval
                                                    "")); // Decline

                                            // Add extra new line for spacing
                                            emailBuilder.append("\n");
                                        }

                                        // Set the combined email list to TextView
                                        emailList.setText(emailBuilder.toString());

                                        // Set click listener to handle clicks on the TextView
                                        setApprovalTextViewListeners();
                                    } else {
                                        Toast.makeText(this, "Error getting documents from users: " + usersTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Error getting documents from foodtech: " + foodtechTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void setApprovalTextViewListeners() {
        emailList.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float x = event.getX();
                float y = event.getY();
                int offset = emailList.getLayout().getOffsetForHorizontal(emailList.getLayout().getLineForVertical((int) y), x);

                String[] lines = emailList.getText().toString().split("\n");
                int lineIndex = emailList.getLayout().getLineForOffset(offset);

                if (lineIndex >= lines.length) return false; // Out of bounds

                String line = lines[lineIndex];
                int startOfApprovalText = line.indexOf("Approve");
                int startOfDeleteText = line.indexOf("Decline");

                if (startOfApprovalText != -1 && x >= emailList.getLayout().getPrimaryHorizontal(startOfApprovalText) && x <= emailList.getLayout().getPrimaryHorizontal(startOfApprovalText) + emailList.getPaint().measureText("Approve")) {
                    handleApprovalClick(line, lineIndex, lines);
                } else if (startOfDeleteText != -1 && x >= emailList.getLayout().getPrimaryHorizontal(startOfDeleteText) && x <= emailList.getLayout().getPrimaryHorizontal(startOfDeleteText) + emailList.getPaint().measureText("Decline")) {
                    handleDeleteClick(line, lineIndex, lines);
                }

                return true;
            }
            return false;
        });
    }

    private void handleApprovalClick(String line, int lineIndex, String[] lines) {
        String email = extractEmailFromLine(line);
        if (email != null && emailStatusMap.containsKey(email)) {
            // Show a confirmation dialog before proceeding
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Approval")
                    .setMessage("Are you sure you want to approve this food technologists?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User confirmed, proceed with approval

                        String currentStatus = emailStatusMap.get(email);
                        String newStatus = currentStatus.equals("Not Yet Verified") ? "Verified by A.T" : "Not Yet Verified";
                        emailStatusMap.put(email, newStatus);

                        // Update SharedPreferences with the new verification status
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(email, newStatus);
                        boolean saved = editor.commit(); // Commit synchronously for debugging
                        Log.d(TAG, "SharedPreferences saved: " + saved);

                        // Update TextView with new status
                        String updatedLine = line.replace(currentStatus, newStatus);
                        lines[lineIndex] = updatedLine;

                        updateEmailListText(lines);

                        Toast.makeText(admintechnichal.this, "Verification status updated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }


    private void handleDeleteClick(String line, int lineIndex, String[] lines) {
        String email = extractEmailFromLine(line);
        if (email != null && emailStatusMap.containsKey(email)) {
            // Show a confirmation dialog before proceeding
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Decline")
                    .setMessage("Are you sure you want to decline this food technologists?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User confirmed, proceed with decline

                        // Update the verification status to "Declined by A.T"
                        String updatedStatus = "Declined by A.T";
                        emailStatusMap.put(email, updatedStatus);

                        // Update SharedPreferences with the new verification status
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(email, updatedStatus);
                        boolean saved = editor.commit(); // Commit synchronously for debugging
                        Log.d(TAG, "SharedPreferences saved: " + saved);

                        // Update the line to reflect the new verification status
                        String updatedLine = line.replaceAll("Not Yet Verified|Verified by A.T", updatedStatus);
                        lines[lineIndex] = updatedLine;

                        // Update the TextView with the new content
                        updateEmailListText(lines);

                        Toast.makeText(admintechnichal.this, "Verification status updated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }


    private void updateEmailListText(String[] lines) {
        StringBuilder updatedContent = new StringBuilder();
        for (String updatedLineText : lines) {
            updatedContent.append(updatedLineText).append("\n");
        }

        emailList.setText(updatedContent.toString());
    }

    private String extractEmailFromLine(String line) {
        // Extract email from the line based on the expected format
        int endOfEmail = line.indexOf(" ");
        return endOfEmail != -1 ? line.substring(0, endOfEmail).trim() : null;
    }
}
