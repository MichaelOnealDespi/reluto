package com.myapp.reluto.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.myapp.reluto.R;
import com.myapp.reluto.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setInputFilters(); // Set input filters
        setVariable(); // Set up click listeners and radio button logic
    }

    private void setInputFilters() {
        // Set input filter for contact number (11 digits)
        binding.contactEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        // Set input filter for password (8-16 characters)
        binding.passEdt.setFilters(new InputFilter[]{new PasswordInputFilter()});


        // No filter needed for date input; use date picker instead.
    }

    private class PasswordInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // Allow characters only if the length is within the range of 8-16 characters
            String current = dest.toString() + source.toString();
            if (current.length() > 16) {
                return "";
            }
            return null;
        }
    }

    private void setVariable() {
        // Set the default radio button selection to "User"
        binding.radioButtonUser.setChecked(true);
        toggleFields(View.GONE); // Hide additional fields for User by default
        binding.textView9.setVisibility(View.GONE); // Hide textView9 by default
        binding.textView3.setVisibility(View.GONE);
        binding.textView4.setVisibility(View.GONE);
        binding.profilephotoBtn.setVisibility(View.GONE); // Hide photo upload button by default
        binding.photoimage.setVisibility(View.GONE); // Hide photo image by default
        binding.socialEdt.setVisibility(View.GONE);

        binding.bdyEdt.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Inflate custom layout
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_date_picker, null);
            DatePicker datePicker = dialogView.findViewById(R.id.datePicker);

            // Set the initial date
            datePicker.init(year, month, day, null);

            // Set the minimum and maximum dates
            Calendar minDate = new GregorianCalendar(1900, Calendar.JANUARY, 1);
            Calendar maxDate = new GregorianCalendar(2006, Calendar.DECEMBER, 31);

            datePicker.setMinDate(minDate.getTimeInMillis());
            datePicker.setMaxDate(maxDate.getTimeInMillis());

            // Create DatePickerDialog with custom view
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setView(dialogView)
                    .setTitle("Please Select Your Date of Birth")
                    .setPositiveButton("OK", (dialog, which) -> {
                        int selectedYear = datePicker.getYear();
                        int selectedMonth = datePicker.getMonth();
                        int selectedDay = datePicker.getDayOfMonth();

                        // Format the date
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        binding.bdyEdt.setText(selectedDate); // Set the selected date to EditText
                    })
                    .setNegativeButton("Cancel", null);

            // Show the custom date picker dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });



        // Set up radio group listener
        binding.radioGroupUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonFoodtech) {
                toggleFields(View.VISIBLE); // Show additional fields for Foodtech
                binding.textView9.setVisibility(View.VISIBLE); // Show textView9
                binding.textView3.setVisibility(View.VISIBLE); // Show textView9
                binding.textView4.setVisibility(View.VISIBLE); // Hide textView3
                binding.socialEdt.setVisibility(View.VISIBLE);
                binding.profilephotoBtn.setVisibility(View.VISIBLE); // Show photo upload button for Foodtech
                binding.photoimage.setVisibility(View.VISIBLE); // Show photo image for Foodtech
            } else {
                toggleFields(View.GONE); // Hide additional fields for User
                binding.textView9.setVisibility(View.GONE); // Hide textView9
                binding.textView3.setVisibility(View.GONE); // Hide textView3
                binding.textView4.setVisibility(View.GONE); // Hide textView3
                binding.profilephotoBtn.setVisibility(View.GONE); // Hide photo upload button for User
                binding.photoimage.setVisibility(View.GONE); // Hide photo image for User
                binding.socialEdt.setVisibility(View.GONE);
            }
        });
        binding.bdyEdt.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Inflate custom layout
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_date_picker, null);
            DatePicker datePicker = dialogView.findViewById(R.id.datePicker);

            // Set the initial date
            datePicker.init(year, month, day, null);

            // Set the minimum and maximum dates
            Calendar minDate = new GregorianCalendar(1900, Calendar.JANUARY, 1);
            Calendar maxDate = new GregorianCalendar(2006, Calendar.DECEMBER, 31);

            datePicker.setMinDate(minDate.getTimeInMillis());
            datePicker.setMaxDate(maxDate.getTimeInMillis());

            // Create DatePickerDialog with custom view
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setView(dialogView)
                    .setTitle("Please Select Your Date of Birth")
                    .setPositiveButton("OK", (dialog, which) -> {
                        int selectedYear = datePicker.getYear();
                        int selectedMonth = datePicker.getMonth();
                        int selectedDay = datePicker.getDayOfMonth();

                        // Format the date
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        binding.bdyEdt.setText(selectedDate); // Set the selected date to EditText
                    })
                    .setNegativeButton("Cancel", null);

            // Show the custom date picker dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });


        binding.profilephotoBtn.setOnClickListener(view -> {
            // Handle photo upload
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        binding.signupBtn.setOnClickListener(view -> {
            String email = binding.emailEdt.getText().toString().trim();
            String password = binding.passEdt.getText().toString().trim();
            String confirmPassword = binding.confirmpassEdt.getText().toString().trim(); // Get confirm password
            String fullName = binding.nameEdt.getText().toString().trim();
            String lastName = binding.lastnameEdt.getText().toString().trim();
            String contact = binding.contactEdt.getText().toString().trim();
            String description = binding.urselfEdit.getText().toString().trim();
            String link = binding.linkEdt.getText().toString().trim();
            String prc = binding.PRCEdt.getText().toString().trim(); // PRC Number
            String social = binding.socialEdt.getText().toString().trim(); // PRC Number
            String birthday = binding.bdyEdt.getText().toString().trim(); // Birthday
            String userType = binding.radioGroupUserType.getCheckedRadioButtonId() == R.id.radioButtonFoodtech
                    ? "Foodtech" : "User";

            // Validate fields
            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || contact.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(SignUpActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password
            if (password.length() < 8 || password.length() > 16) {
                Toast.makeText(SignUpActivity.this, "Password must be between 8 and 16 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

// Check for at least one letter and one digit
            boolean hasLetter = !password.equals(password.replaceAll("[a-zA-Z]", ""));
            boolean hasDigit = !password.equals(password.replaceAll("[0-9]", ""));
            if (!hasLetter || !hasDigit) {
                Toast.makeText(SignUpActivity.this, "Please use a password that includes letters and numbers.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]+").matcher(password).find()) {
                Toast.makeText(SignUpActivity.this, "Password must include at least one special character", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check contact number length (must be 11 digits)
            if (contact.length() != 11) {
                Toast.makeText(SignUpActivity.this, "Your contact number must be 11 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if link, prc, photo, and birthday are required and provided for Foodtech
            if ("Foodtech".equals(userType) && (link.isEmpty() || prc.isEmpty() || imageUri == null || birthday.isEmpty() || social.isEmpty())) {
                Toast.makeText(SignUpActivity.this, "Link, PRC, photo, social media and birthday fields are required for Foodtech", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show data privacy consent dialog
            showDataPrivacyConsentDialog(email, password, fullName, lastName, description, contact, link, prc, social, userType, birthday);
        });

        // Set up click listener for the login TextView
        binding.login.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the SignUpActivity
        });
    }

    private void showDataPrivacyConsentDialog(String email, String password, String fullName, String lastName, String description, String contact, String link, String prc, String social, String userType, String birthday) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Condition");
        builder.setMessage("1. You acknowledge that we collect and store your personal data in accordance with our privacy policy.\n" +
                "2. You understand that your data will be used to enhance and personalize your user experience.\n" +
                "3. You agree that we may use your data to communicate with you about updates and promotions.\n" +
                "Do you agree to these terms?");
        builder.setPositiveButton("Agree", (dialog, which) -> {
            // Proceed based on user type after agreeing to the privacy consent
            showUserTypeSpecificDialog(email, password, fullName, lastName, description, contact, link, prc, social, userType, birthday);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showUserTypeSpecificDialog(String email, String password, String fullName, String lastName, String description, String contact, String link, String prc, String social, String userType, String birthday) {
        if ("Foodtech".equals(userType)) {
            showTermsAndConditionsDialog(email, password, fullName, lastName, description, contact, link, prc, social, userType, birthday);
        } else {
            showDisclaimerDialog(email, password, fullName, lastName, contact, userType, birthday);
        }
    }

    private void showTermsAndConditionsDialog(String email, String password, String fullName, String lastName, String description, String contact, String link, String prc, String social, String userType, String birthday) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disclaimer");
        builder.setMessage("By using this platform, you acknowledge and agree to the following disclaimers:\n\n" +
                "1. You acknowledge that providing false information can result in legal action.\n" +
                "2. You understand that misuse of the platform can lead to account suspension.\n" +
                "3. You agree to maintain professional conduct at all times.\n" +
                "4. You consent to regular audits by the platform to ensure compliance.\n" +
                "5. You are aware that violation of terms can result in license revocation.\n\n" +
                "Do you accept this disclaimer?");
        builder.setPositiveButton("Agree", (dialog, which) -> {
            createUserWithEmailAndPassword(email, password, fullName, lastName, description, contact, link, prc,social, userType, birthday);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDisclaimerDialog(String email, String password, String fullName, String lastName, String contact, String userType, String birthday) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disclaimer");
        builder.setMessage("By signing up, you acknowledge that you have read and understood the disclaimer.\n\n" +
                "1. Recipes are provided for informational purposes only. The platform is not responsible for any health issues, including illness or allergic reactions, from using these recipes.\n" +
                "2. Users must ensure that ingredients are safe and properly handled. The platform is not liable for issues resulting from improper food preparation or storage.\n" +
                "3. The platform is not liable for any harm or loss resulting from recipe use, including health issues or financial losses.\n" +
                "4. You understand that any violation of the platform's policies may result in account suspension.\n\n" +
                "Do you agree to this disclaimer?");
        builder.setPositiveButton("Agree", (dialog, which) -> {
            createUserWithEmailAndPassword(email, password, fullName, lastName, null, contact, null, null, null, userType, birthday);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void createUserWithEmailAndPassword(String email, String password, String fullName, String lastName, String description, String contact, String link, String prc, String social, String userType, String birthday) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            boolean isVerified = "Foodtech".equals(userType) ? false : true;

                            // Save user data to Firestore
                            saveUserToFirestore(userId, email, fullName, lastName, contact, description, link, prc, social, userType, isVerified, birthday);

                            // Only send verification email for Users
                            if (!"Foodtech".equals(userType)) {
                                user.sendEmailVerification().addOnCompleteListener(emailVerificationTask -> {
                                    if (emailVerificationTask.isSuccessful()) {
                                        Log.d(TAG, "Email verification sent.");
                                        Toast.makeText(SignUpActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Custom handling for Foodtech
                                sendFoodtechValidationEmail(email);
                            }
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendFoodtechValidationEmail(String email) {
        // Implement email sending logic for Foodtech if required
    }

    private void saveUserToFirestore(String userId, String email, String fullName, String lastName, String contact, String description, String link, String prc, String social, String userType, boolean isVerified, String birthday) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("fullName", fullName);
        user.put("lastName", lastName);
        user.put("contact", contact);
        user.put("userType", userType);
        user.put("isVerified", isVerified);
        user.put("birthdate", birthday); // Add birthday to user data

        if ("Foodtech".equals(userType)) {
            user.put("link", link);
            user.put("prc", prc);
            user.put("social", social);
            user.put("description", description);

            if (imageUri != null) {
                StorageReference photoRef = storageReference.child("profile_photos/" + UUID.randomUUID().toString());
                photoRef.putFile(imageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        photoRef.getDownloadUrl().addOnCompleteListener(downloadUrlTask -> {
                            if (downloadUrlTask.isSuccessful()) {
                                String photoUrl = downloadUrlTask.getResult().toString();
                                user.put("photoUrl", photoUrl);
                                saveUserDocument(userId, userType, user);
                            } else {
                                Toast.makeText(SignUpActivity.this, "Failed to get photo URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                saveUserDocument(userId, userType, user);
            }
        } else {
            saveUserDocument(userId, userType, user);
        }
    }

    private void saveUserDocument(String userId, String userType, Map<String, Object> user) {
        String collection = "users";
        if ("Foodtech".equals(userType)) {
            collection = "foodtech";
        }

        db.collection(collection).document(userId).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "User registered successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to register user. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(binding.photoimage); // Display selected image
        }
    }

    private void toggleFields(int visibility) {
        binding.linkEdt.setVisibility(visibility);
        binding.PRCEdt.setVisibility(visibility);
        binding.urselfEdit.setVisibility(visibility);
    }
}
