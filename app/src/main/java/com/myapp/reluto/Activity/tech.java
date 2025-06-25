package com.myapp.reluto.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.myapp.reluto.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class tech extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // Add FirebaseAuth reference
    private TextView emailList;
    private LinearLayout imageContainer;
    private LinearLayout profileImageContainer; // New container for profile images
    private List<String> emailAddresses = new ArrayList<>();
    private HashMap<String, CheckBox> checkBoxMap = new HashMap<>();
    private HashMap<String, String> verificationStatusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tech);

        // Handling insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        emailList = findViewById(R.id.emailList);
        imageContainer = findViewById(R.id.imageContainer);
        profileImageContainer = findViewById(R.id.profileImageContainer); // Initialize profileImageContainer

        // Set profileImageContainer size to match imageContainer
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageContainer.getLayoutParams().width,
                imageContainer.getLayoutParams().height
        );
        profileImageContainer.setLayoutParams(params);

        // Initialize buttons
        Button approveButton = findViewById(R.id.approveButton);
        Button declineButton = findViewById(R.id.declineButton);
        findViewById(R.id.signOutBtn).setOnClickListener(v -> showSignOutConfirmationDialog());

        // Set up button click listeners
        approveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Approval")
                    .setMessage("Are you sure you want to approve the selected emails?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        for (String email : emailAddresses) {
                            CheckBox checkBox = checkBoxMap.get(email);
                            if (checkBox != null && checkBox.isChecked()) {
                                // Update verification status to Approved in local map first
                                verificationStatusMap.put(email, "Verified by T.A");
                                // Now update Firestore
                                db.collection("Verification").document(email)
                                        .set(new HashMap<String, Object>() {{
                                            put("status", "Verified by T.A");
                                        }}, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, email + " Approved", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                        // Refresh the email list to show updated verification statuses
                        fetchUserEmails();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        declineButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Decline")
                    .setMessage("Are you sure you want to decline the selected emails?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        for (String email : emailAddresses) {
                            CheckBox checkBox = checkBoxMap.get(email);
                            if (checkBox != null && checkBox.isChecked()) {
                                // Update verification status to Declined in local map first
                                verificationStatusMap.put(email, "Declined by T.A");
                                // Now update Firestore
                                db.collection("Verification").document(email)
                                        .set(new HashMap<String, Object>() {{
                                            put("status", "Declined by T.A");
                                        }}, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, email + " Declined", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                        // Refresh the email list to show updated verification statuses
                        fetchUserEmails();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Fetch the user emails on activity start
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

        // Define column widths for a well-formatted table
        final int emailWidth = 40;
        final int userTypeWidth = 20;
        final int nameWidth = 20;
        final int linkWidth = 30;
        final int prcWidth = 20;
        final int statusWidth = 20;

        // Adjust header format to use "Status" instead of "Verification"
        final String headerFormat = "%-" + emailWidth + "s %-" + userTypeWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + nameWidth + "s %-" + linkWidth + "s %-" + prcWidth + "s %-" + statusWidth + "s\n";

        // Add header row with "Status"
        emailBuilder.append(String.format(headerFormat, "Email Address", "User Type", "Full Name", "Last Name", "Birthdate", "Contact", "Link", "PRC", "Status"));

        // Clear previous email addresses and checkboxes before fetching
        emailAddresses.clear();
        imageContainer.removeAllViews();
        profileImageContainer.removeAllViews(); // Clear profile image container

        // Fetch verification statuses from the "Verification" collection
        db.collection("Verification").get().addOnCompleteListener(verificationTask -> {
            if (verificationTask.isSuccessful()) {
                HashMap<String, String> verificationStatusMap = new HashMap<>();
                for (QueryDocumentSnapshot document : verificationTask.getResult()) {
                    String email = document.getId();
                    String status = document.getString("status");
                    verificationStatusMap.put(email, status != null ? status : "Not Yet Verified");
                }

                // Now fetch data from the "foodtech" collection
                db.collection("foodtech").get().addOnCompleteListener(foodtechTask -> {
                    if (foodtechTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : foodtechTask.getResult()) {
                            String email = document.getString("email");
                            String fullName = document.getString("fullName");
                            String lastName = document.getString("lastName");
                            String birthdate = document.getString("birthdate");
                            String contact = document.getString("contact");
                            String link = document.getString("link");
                            String prc = document.getString("prc");
                            String photoUrl = document.getString("photoUrl"); // Get photoUrl
                            String userType = "FoodTech";
                            String verificationStatus = verificationStatusMap.getOrDefault(email, "Not Yet Verified");

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
                                    verificationStatus
                            ));

                            emailBuilder.append("\n");

                            // Add email to the list and create a checkbox only for FoodTech users
                            emailAddresses.add(email);
                            CheckBox verificationCheckBox = new CheckBox(this);
                            verificationCheckBox.setChecked(false);
                            // Disable the checkbox if the verification status is not "Not Yet Verified"
                            verificationCheckBox.setEnabled(verificationStatus.equals("Not Yet Verified"));
                            verificationCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                String status = isChecked ? "is clicked" : "Not Yet Verified";
                                Toast.makeText(this, email + " " + status, Toast.LENGTH_SHORT).show();
                            });

                            // Map the checkbox to the email
                            checkBoxMap.put(email, verificationCheckBox);

                            // Create a container for each FoodTech entry to align the checkbox and profile image at the start
                            LinearLayout foodTechLayout = new LinearLayout(this);
                            foodTechLayout.setOrientation(LinearLayout.HORIZONTAL); // Set orientation to horizontal
                            foodTechLayout.setGravity(Gravity.START); // Align elements to the start (left)

                            // Create an ImageView for the photoUrl
                            if (photoUrl != null) {
                                ImageView photoImageView = createImageView(photoUrl);
                                // Add the checkbox first, followed by the profile image
                                foodTechLayout.addView(verificationCheckBox); // Add checkbox first
                                foodTechLayout.addView(photoImageView); // Add profile image second
                            }

                            // Add foodTechLayout (which contains the checkbox and image) to the imageContainer
                            imageContainer.addView(foodTechLayout);
                        }

                        // Display the email list
                        emailList.setText(emailBuilder.toString());
                        // Fetch data from the "users" collection after foodtech
                        fetchUsersCollection(emailBuilder, headerFormat);
                    } else {
                        Toast.makeText(this, "Error fetching foodtech data: " + foodtechTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Error fetching verification data: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void fetchUsersCollection(StringBuilder emailBuilder, String headerFormat) {
        db.collection("users").get().addOnCompleteListener(usersTask -> {
            if (usersTask.isSuccessful()) {
                for (QueryDocumentSnapshot document : usersTask.getResult()) {
                    String email = document.getString("email");
                    String fullName = document.getString("fullName");
                    String lastName = document.getString("lastName");
                    String birthdate = document.getString("birthdate");
                    String contact = document.getString("contact");
                    String link = document.getString("link");
                    String prc = document.getString("prc");
                    String verification = "Registered";
                    String userType = "User";

                    // Add the data row for users without checkbox
                    emailBuilder.append(String.format(headerFormat,
                            email != null ? email : "N/A",
                            userType,
                            fullName != null ? fullName : "N/A",
                            lastName != null ? lastName : "N/A",
                            birthdate != null ? birthdate : "N/A",
                            contact != null ? contact : "N/A",
                            link != null ? link : "N/A",
                            prc != null ? prc : "N/A",
                            verification
                    ));

                    emailBuilder.append("\n");
                }

                emailList.setText(emailBuilder.toString());
            } else {
                Toast.makeText(this, "Error fetching users data: " + usersTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
