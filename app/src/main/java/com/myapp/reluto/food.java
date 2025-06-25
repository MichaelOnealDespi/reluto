package com.myapp.reluto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.myapp.reluto.Activity.FoodTechReporting;
import com.myapp.reluto.Activity.LoginActivity;
import com.myapp.reluto.databinding.ActivityFoodBinding;
import com.myapp.reluto.databinding.ActivityTechBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class food extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // FirebaseAuth reference
    private TextView emailList;
    private LinearLayout imageContainer;
    private LinearLayout profileImageContainer; // New container for profile images
    private List<String> emailAddresses = new ArrayList<>();
    private HashMap<String, CheckBox> checkBoxMap = new HashMap<>();
    private ActivityFoodBinding binding;  // This is your ViewBinding object
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tech); // Make sure the layout contains profileImageContainer

        // Handling insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize binding object and set content view
        binding = ActivityFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // Set the root of the binding as the content view


        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        emailList = findViewById(R.id.emailList);
        imageContainer = findViewById(R.id.imageContainer);
        profileImageContainer = findViewById(R.id.profileImageContainer); // Initialize profileImageContainer

        // Initialize buttons
        Button approveButton = findViewById(R.id.approveButton);
        Button declineButton = findViewById(R.id.declineButton);
        findViewById(R.id.signOutBtn).setOnClickListener(v -> showSignOutConfirmationDialog());

        // Set up button click
        approveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Approval")
                    .setMessage("Are you sure you want to approve the selected emails?")
                    .setPositiveButton("Yes", (dialog, which) -> updateVerificationStatus("Verified by T.A & Approved F.A"))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        declineButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Decline")
                    .setMessage("Are you sure you want to decline the selected emails?")
                    .setPositiveButton("Yes", (dialog, which) -> updateVerificationStatus("Verified by T.A & Declined by F.A"))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Handle click event for "reportsact" TextView
        binding.reportsact.setOnClickListener(view -> {
            // Start FoodTechReportingActivity when "reportsact" is clicked
            Intent intent = new Intent(food.this, FoodTechReporting.class);
            startActivity(intent);
            finish(); // Close the current activity (food)
        });

        // Sign out button click listener
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

    private void updateVerificationStatus(String status) {
        for (String email : emailAddresses) {
            CheckBox checkBox = checkBoxMap.get(email);
            if (checkBox != null && checkBox.isChecked()) {
                db.collection("Verification").document(email)
                        .set(new HashMap<String, Object>() {{
                            put("status", status);
                        }}, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, email + " " + status, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
        fetchUserEmails(); // Refresh the email list to show updated verification statuses
    }

    private void fetchUserEmails() {
        StringBuilder emailBuilder = new StringBuilder();

        // Define column widths for a well-formatted table
        final int emailWidth = 40;
        final int userTypeWidth = 20;
        final int nameWidth = 20;
        final int linkWidth = 30;
        final int prcWidth = 20;
        final int statusWidth = 25;

        // Adjust header format to use "Status" instead of "Verification"
        final String headerFormat = "%-" + emailWidth + "s %-" + userTypeWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + linkWidth + "s %-" + prcWidth + "s %-" + statusWidth + "s\n";

        // Add header row with "Status"
        emailBuilder.append(String.format(headerFormat, "Email Address", "User Type", "Full Name", "Last Name", "Birthdate", "Contact", "Link", "PRC", "Status"));

        // Clear previous email addresses and checkboxes before fetching
        emailAddresses.clear();
        imageContainer.removeAllViews();
        profileImageContainer.removeAllViews(); // Clear previous profile images

        // Fetch data from "Verification" collection first to get verified emails
        db.collection("Verification").whereEqualTo("status", "Verified by T.A").get().addOnCompleteListener(verificationTask -> {
            if (verificationTask.isSuccessful()) {
                List<String> verifiedEmails = new ArrayList<>();
                for (QueryDocumentSnapshot document : verificationTask.getResult()) {
                    verifiedEmails.add(document.getId()); // Collect verified emails
                }

                // Now fetch data from "foodtech" collection for the verified emails
                db.collection("foodtech").get().addOnCompleteListener(foodtechTask -> {
                    if (foodtechTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : foodtechTask.getResult()) {
                            String email = document.getString("email");
                            if (verifiedEmails.contains(email)) { // Only show verified emails
                                String fullName = document.getString("fullName");
                                String lastName = document.getString("lastName");
                                String birthdate = document.getString("birthdate");
                                String contact = document.getString("contact");
                                String link = document.getString("link");
                                String prc = document.getString("prc");
                                String photoUrl = document.getString("photoUrl"); // Fetch photo URL
                                String userType = "FoodTech";

                                // Append data into the table format
                                emailBuilder.append(String.format(headerFormat,
                                        email != null ? email : "N/A",
                                        userType,
                                        fullName != null ? fullName : "N/A",
                                        lastName != null ? lastName : "N/A",
                                        birthdate != null ? birthdate : "N/A",
                                        contact != null ? contact : "N/A",
                                        link != null ? link : "N/A",
                                        prc != null ? prc : "N/A",
                                        "Verified by T.A"
                                ));

                                emailBuilder.append("\n");

                                // Add email to the list and create a checkbox only for verified FoodTech users
                                emailAddresses.add(email);
                                CheckBox verificationCheckBox = new CheckBox(this);
                                verificationCheckBox.setChecked(false);
                                verificationCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    String status = isChecked ? "is clicked" : "Not Yet Verified";
                                    Toast.makeText(this, email + " " + status, Toast.LENGTH_SHORT).show();
                                });

                                // Map the checkbox to the email
                                checkBoxMap.put(email, verificationCheckBox);

                                // Create a container for each user to align the checkbox and profile image horizontally
                                LinearLayout foodTechLayout = new LinearLayout(this);
                                foodTechLayout.setOrientation(LinearLayout.HORIZONTAL); // Set orientation to horizontal
                                foodTechLayout.setGravity(Gravity.START); // Align elements to the start (left)

                                // Add the checkbox first, followed by the profile image
                                foodTechLayout.addView(verificationCheckBox);

                                // Create and add an ImageView for the profile image (photoUrl)
                                if (photoUrl != null) {
                                    ImageView photoImageView = createImageView(photoUrl);
                                    foodTechLayout.addView(photoImageView); // Add profile image next to the checkbox
                                }

                                // Add the whole layout (which contains the checkbox and profile image) to the imageContainer
                                imageContainer.addView(foodTechLayout);
                            }
                        }

                        // Display the email list after images are processed
                        emailList.setText(emailBuilder.toString());
                    } else {
                        Toast.makeText(this, "Error fetching foodtech data: " + foodtechTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Error fetching verification data: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Helper method to create an ImageView and load an image from URL
    private ImageView createImageView(String photoUrl) {
        // Create an ImageView with square dimensions
        ImageView photoImageView = new ImageView(this);
        int size = 65; // Set the size of the square (width and height)

        // Set the layout parameters for the small thumbnail image
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.gravity = Gravity.CENTER; // Center the image
        photoImageView.setLayoutParams(params);

        // Load the image using Glide and set it to scale center crop
        Glide.with(this)
                .load(photoUrl)
                .override(size, size) // Resize to square
                .centerCrop() // Scale the image to fill the view
                .into(photoImageView);

        // Set a click listener to show the image in a dialog
        photoImageView.setOnClickListener(v -> {
            // Create a custom dialog for the expanded image
            Dialog imageDialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
            imageDialog.setContentView(R.layout.dialog_fullscreen_image); // Custom layout for fullscreen image

            // Find the ImageView and Back Button in the custom dialog layout
            ImageView fullImageView = imageDialog.findViewById(R.id.fullScreenImageView);
            ImageButton backButton = imageDialog.findViewById(R.id.backButton);

            // Load the full-size image using Glide
            Glide.with(this)
                    .load(photoUrl)
                    .into(fullImageView); // Set the full-size image to the ImageView

            // Set up the back button to dismiss the dialog
            backButton.setOnClickListener(view -> imageDialog.dismiss()); // Close the dialog when the back button is clicked

            // Show the dialog
            imageDialog.show();
        });

        return photoImageView; // Return the ImageView directly
    }

}


