package com.myapp.reluto.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.myapp.reluto.R;
import com.myapp.reluto.databinding.ActivityLoginBinding;
import com.myapp.reluto.food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setVariable();
    }

    private void setVariable() {
        binding.loginBtn.setOnClickListener(view -> {
            String email = binding.emailEdt.getText().toString().trim();
            String password = binding.passEdt.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                // Check if the login credentials match the hardcoded admintechnical credentials
                if ("tech".equals(email) && "tech".equals(password)) {
                    redirectToAdminTechnical();
                } else if ("food".equals(email) && "food".equals(password)) {
                    redirectToAdminFoodtech();
                } else {
                    // Proceed with Firebase authentication for other users
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Retrieve user profile from Firestore
                                getUserProfile(firebaseUser.getUid(), firebaseUser.isEmailVerified());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(LoginActivity.this, "Please Input Email and Password", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listener for the "Sign Up" TextView
        binding.signup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish(); // Close the LoginActivity
        });

        // Set up click listener for the "Change Password" TextView
        binding.forgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish(); // Close the LoginActivity
        });
    }

    private void getUserProfile(String userId, boolean isEmailVerified) {
        // Determine if the user is in the "users" or "foodtech" collection
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            if (isEmailVerified) {
                                boolean isVerified = document.getBoolean("isVerified");
                                if (isVerified) {
                                    String userType = document.getString("userType");
                                    redirectUser(userType);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Your account is not verified yet. Please verify your email account or contact our support.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If not found in "users", try "foodtech"
                            getFoodtechProfile(userId);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getFoodtechProfile(String userId) {
        // Try to get user data from the "foodtech" collection
        db.collection("foodtech").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            boolean isVerified = document.getBoolean("isVerified");
                            if (isVerified) {
                                redirectUser("Foodtech");
                            } else {
                                // Show the first message
                                Toast.makeText(LoginActivity.this,
                                        "We need to validate your account. This process may take 3-4 working days.",
                                        Toast.LENGTH_LONG).show();

                                // Show the second message after a delay
                                new Handler().postDelayed(() ->
                                                Toast.makeText(LoginActivity.this,
                                                        "We'll email you once we have validated your account.",
                                                        Toast.LENGTH_LONG).show(),
                                        Toast.LENGTH_LONG); // Delay equal to Toast.LENGTH_LONG to ensure sequential display
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectUser(String userType) {
        Intent intent;
        if ("Foodtech".equals(userType)) {
            intent = new Intent(LoginActivity.this, FoodtechProfileActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish(); // Close the login activity
    }

    private void redirectToAdminTechnical() {
        Intent intent = new Intent(LoginActivity.this, tech.class);
        startActivity(intent);
        finish(); // Close the login activity
    }

    private void redirectToAdminFoodtech() {
        Intent intent = new Intent(LoginActivity.this, food.class);
        startActivity(intent);
        finish(); // Close the login activity
    }
}