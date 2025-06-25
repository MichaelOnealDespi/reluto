package com.myapp.reluto.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.myapp.reluto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class profilefoodtechedit extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private EditText fullNameEdt, lastNameEdt, contactEdt, descriptionEdt, birthdateEdt;
    private ImageView profileImageView, backButton;
    private Button saveProfileBtn, uploadImageBtn;

    private String currentPhotoUrl;
    private String newImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilefoodtechedit);

        // Initialize Firebase and other components
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        fullNameEdt = findViewById(R.id.fullNameEdt);
        lastNameEdt = findViewById(R.id.lastNameEdt);
        contactEdt = findViewById(R.id.contactEdt);
        descriptionEdt = findViewById(R.id.descriptionEdt);
        birthdateEdt = findViewById(R.id.birthdateEdt); // Initialize birthdate EditText
        profileImageView = findViewById(R.id.profileImageView);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
        backButton = findViewById(R.id.backBtn);

        // Set input filter for contactEdt
        contactEdt.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(11), // Limit input to 11 characters
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return ""; // Disallow non-digit input
                            }
                        }
                        return null; // Allow digit input
                    }
                }
        });

        // Load current user profile data
        loadUserProfile();

        uploadImageBtn.setOnClickListener(v -> openFileChooser());

        saveProfileBtn.setOnClickListener(v -> showConfirmationDialog());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(profilefoodtechedit.this, FoodtechProfileActivity.class);
            startActivity(intent);
            finish(); // Optional: if you want to remove this activity from the back stack
        });
    }

    private void loadUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("foodtech").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    String fullName = task.getResult().getString("fullName");
                    String lastName = task.getResult().getString("lastName");
                    String contact = task.getResult().getString("contact");
                    String description = task.getResult().getString("description");
                    String birthdate = task.getResult().getString("birthdate"); // Retrieve birthdate
                    currentPhotoUrl = task.getResult().getString("photoUrl");

                    fullNameEdt.setText(fullName);
                    lastNameEdt.setText(lastName);
                    contactEdt.setText(contact);
                    descriptionEdt.setText(description);
                    birthdateEdt.setText(birthdate); // Set birthdate

                    if (currentPhotoUrl != null) {
                        Glide.with(this).load(currentPhotoUrl).into(profileImageView);
                    }
                }
            } else {
                Toast.makeText(profilefoodtechedit.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void updateProfile() {
        String userId = auth.getCurrentUser().getUid();
        String fullName = fullNameEdt.getText().toString();
        String lastName = lastNameEdt.getText().toString();
        String contact = contactEdt.getText().toString();
        String description = descriptionEdt.getText().toString();
        String birthdate = birthdateEdt.getText().toString(); // Get birthdate

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(userId + ".jpg");

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String photoUrl = uri.toString();
                Map<String, Object> profileUpdates = new HashMap<>();
                profileUpdates.put("fullName", fullName);
                profileUpdates.put("lastName", lastName);
                profileUpdates.put("contact", contact);
                profileUpdates.put("description", description);
                profileUpdates.put("birthdate", birthdate); // Update birthdate
                profileUpdates.put("photoUrl", photoUrl);

                firestore.collection("foodtech").document(userId).update(profileUpdates)
                        .addOnSuccessListener(aVoid -> Toast.makeText(profilefoodtechedit.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(profilefoodtechedit.this, "Failed to Update Profile", Toast.LENGTH_SHORT).show());
            }));
        } else {
            Map<String, Object> profileUpdates = new HashMap<>();
            profileUpdates.put("fullName", fullName);
            profileUpdates.put("lastName", lastName);
            profileUpdates.put("contact", contact);
            profileUpdates.put("description", description);
            profileUpdates.put("birthdate", birthdate); // Update birthdate

            firestore.collection("foodtech").document(userId).update(profileUpdates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(profilefoodtechedit.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(profilefoodtechedit.this, "Failed to Update Profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Changes")
                .setMessage("Are you sure you want to save these changes?")
                .setPositiveButton("Yes", (dialog, which) -> updateProfile())
                .setNegativeButton("No", null)
                .show();
    }
}

