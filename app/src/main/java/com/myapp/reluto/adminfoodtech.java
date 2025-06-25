package com.myapp.reluto;

import android.app.AlertDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.myapp.reluto.Activity.LoginActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class adminfoodtech extends AppCompatActivity {

    private static final String TAG = "adminfoodtech";
    private SharedPreferences sharedPreferences;
    private TextView emailList;
    private FirebaseFirestore db;
    private Map<String, String> emailStatusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adminfoodtech);

        emailList = findViewById(R.id.emailList);
        sharedPreferences = getSharedPreferences("EmailStatusPrefs", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.signOutBtn).setOnClickListener(v -> showSignOutConfirmationDialog());

        // Retrieve and display verified FoodTech users
        fetchVerifiedFoodtechUsers();
    }

    private void showSignOutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish(); // Close this activity
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void fetchVerifiedFoodtechUsers() {
        StringBuilder emailBuilder = new StringBuilder();

        // Define column widths and formatting
        final int emailWidth = 40;
        final int userTypeWidth = 20;
        final int nameWidth = 20;
        final int linkWidth = 30;
        final int prcWidth = 20;
        final int verificationWidth = 35;
        final int approvalWidth = 10;
        final int declineWidth = 10; // New width for "Decline"
        final String headerFormat = "%-" + emailWidth + "s %-" + userTypeWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + linkWidth + "s %-" + prcWidth + "s %-" + verificationWidth + "s %-" + approvalWidth + "s %-" + declineWidth + "s\n";

        // Add header row
        emailBuilder.append(String.format(headerFormat, "Email Address", "User Type", "Full Name", "Last Name", "Birthdate", "Contact", "Link", "PRC", "Verification", "Approve", "Decline"));

        // Fetch FoodTech users from Firestore
        db.collection("foodtech").get()
                .addOnCompleteListener(foodtechTask -> {
                    if (foodtechTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : foodtechTask.getResult()) {
                            String photoUrl = document.getString("photoUrl"); // Retrieve photoUrl
                            String email = document.getString("email");
                            String fullName = document.getString("fullName");
                            String lastName = document.getString("lastName");
                            String birthdate = document.getString("birthdate");
                            String contact = document.getString("contact");
                            String link = document.getString("link");
                            String prc = document.getString("prc");


                            // Retrieve stored verification status
                            String verificationStatus = sharedPreferences.getString(email, "Not Yet Verified");

                            // Only include users with "Verified by A.T"
                            if ("Verified by A.T".equals(verificationStatus)) {
                                String verification = "Verified by A.T"; // Set the verification status for display
                                String userType = "FoodTech";
                                String approvalText = "Approve";
                                String declineText = "Decline";

                                // Append user details to the StringBuilder
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
                                        approvalText,
                                        declineText)); // Add "Decline" column

                                // Add extra new line for spacing
                                emailBuilder.append("\n");

                                // Save email status in the map
                                emailStatusMap.put(email, verification);

                                // Display the user's photo
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
                            }
                        }

                        if (emailBuilder.length() == 0) {
                            emailBuilder.append("No verified Foodtech users found.");
                        }

                        // Set the combined email list to TextView
                        emailList.setText(emailBuilder.toString());

                        // Set click listener to handle clicks on the TextView
                        setApprovalTextViewListeners();
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
                int startOfDeclineText = line.indexOf("Decline");

                if (startOfApprovalText != -1 && x >= emailList.getLayout().getPrimaryHorizontal(startOfApprovalText) && x <= emailList.getLayout().getPrimaryHorizontal(startOfApprovalText) + emailList.getPaint().measureText("Approve")) {
                    handleApprovalClick(line, lineIndex, lines);
                } else if (startOfDeclineText != -1 && x >= emailList.getLayout().getPrimaryHorizontal(startOfDeclineText) && x <= emailList.getLayout().getPrimaryHorizontal(startOfDeclineText) + emailList.getPaint().measureText("Decline")) {
                    handleDeclineClick(line, lineIndex, lines);
                }

                return true;
            }
            return false;
        });
    }

    private void handleApprovalClick(String line, int lineIndex, String[] lines) {
        String email = extractEmailFromLine(line);
        if (email != null && emailStatusMap.containsKey(email)) {
            // Show confirmation dialog before proceeding
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Approval")
                    .setMessage("Are you sure you want to approve this food technologists?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with approval
                        String currentStatus = emailStatusMap.get(email);
                        String newStatus;

                        // Determine the new status based on the current status
                        if ("Not Yet Verified".equals(currentStatus)) {
                            newStatus = "Verified by A.T & A.F";
                        } else if ("Verified by A.T".equals(currentStatus)) {
                            newStatus = "Verified by A.T & A.F";
                        } else if ("Verified by A.T & Declined by A.F".equals(currentStatus)) {
                            newStatus = "Verified by A.T & A.F";
                        } else {
                            newStatus = currentStatus; // Keep the current status if it doesn't match expected values
                        }

                        emailStatusMap.put(email, newStatus);

                        // Update SharedPreferences with the new verification status
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(email, newStatus);
                        boolean saved = editor.commit(); // Commit synchronously for debugging
                        Log.d(TAG, "SharedPreferences saved: " + saved);

                        // Update TextView with new status
                        String updatedLine;
                        if ("Verified by A.T & Declined by A.F".equals(currentStatus)) {
                            updatedLine = line.replace("Verified by A.T & Declined by A.F", newStatus);
                        } else if ("Verified by A.T".equals(currentStatus)) {
                            updatedLine = line.replace("Verified by A.T", newStatus);
                        } else if ("Not Yet Verified".equals(currentStatus)) {
                            updatedLine = line.replace("Not Yet Verified", newStatus);
                        } else {
                            updatedLine = line; // If current status doesn't match expected values, don't change the line
                        }

                        lines[lineIndex] = updatedLine;

                        // Update the TextView with the new content
                        updateEmailListText(lines);

                        Toast.makeText(this, "Verification status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void handleDeclineClick(String line, int lineIndex, String[] lines) {
        String email = extractEmailFromLine(line);
        if (email != null && emailStatusMap.containsKey(email)) {
            // Show confirmation dialog before proceeding
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Decline")
                    .setMessage("Are you sure you want to decline this food technologists?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with decline
                        // Current status
                        String currentStatus = emailStatusMap.get(email);
                        String newStatus = "Verified by A.T & Declined by A.F";

                        // Only update if the status is "Not Yet Verified" or already verified
                        if ("Not Yet Verified".equals(currentStatus) || "Verified by A.T".equals(currentStatus) || "Verified by A.T & A.F".equals(currentStatus)) {
                            emailStatusMap.put(email, newStatus);

                            // Update SharedPreferences with the new verification status
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(email, newStatus);
                            boolean saved = editor.commit(); // Commit synchronously for debugging
                            Log.d(TAG, "SharedPreferences saved: " + saved);

                            // Update the line to reflect the new verification status
                            // Handle status replacement more carefully to avoid appending extra text
                            String updatedLine;
                            if ("Verified by A.T & A.F".equals(currentStatus)) {
                                updatedLine = line.replace("Verified by A.T & A.F", newStatus);
                            } else if ("Verified by A.T".equals(currentStatus)) {
                                updatedLine = line.replace("Verified by A.T", newStatus);
                            } else {
                                updatedLine = line.replace("Not Yet Verified", newStatus);
                            }

                            lines[lineIndex] = updatedLine;

                            // Update the TextView with the new content
                            updateEmailListText(lines);

                            Toast.makeText(this, "Verification status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }




    private String extractEmailFromLine(String line) {
        // Extract email from the line based on the expected format
        String[] parts = line.split("\\s+");
        return parts.length > 0 ? parts[0].trim() : null;
    }

    private void updateEmailListText(String[] lines) {
        // Combine lines back into a single string and set it to the TextView
        StringBuilder updatedText = new StringBuilder();
        for (String line : lines) {
            updatedText.append(line).append("\n");
        }
        emailList.setText(updatedText.toString());
    }
}
