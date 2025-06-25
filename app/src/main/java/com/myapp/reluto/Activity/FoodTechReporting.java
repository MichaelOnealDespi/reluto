package com.myapp.reluto.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.myapp.reluto.MostRatedFoodtech;
import com.myapp.reluto.MostRatedRecipes;
import com.myapp.reluto.R;
import com.myapp.reluto.food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class FoodTechReporting extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageView signOutBtns;

    private TextView recipeTitle;
    private TextView filterTextView, filter2;

    private FirebaseDatabase database;
    private DatabaseReference recipesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_tech_reporting);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        recipesRef = database.getReference("Foods");

        // Initialize the TextViews
        recipeTitle = findViewById(R.id.recipeTitle);
        filterTextView = findViewById(R.id.filterTextView);
        filter2 = findViewById(R.id.filterviewtext);

        // Set Text Sizes Programmatically
        setTextSize(recipeTitle, 13);  // Header size (18sp)
        setTextSize(filterTextView, 15); // Regular text size (12sp)
        setTextSize(filter2, 15); // Regular text size (12sp)

        // Initially hide filter2
        filter2.setVisibility(TextView.GONE);

        // Fetch and display recipes from Firebase
        fetchRecipesFromDatabase();

        // Set filter click listener
        filterTextView.setOnClickListener(v -> showFilterDialog());
        filter2.setOnClickListener(v -> showDateDialog());

        // Sign-out button
        signOutBtns = findViewById(R.id.signOutBtns);
        signOutBtns.setOnClickListener(v -> showSignOutConfirmationDialog());

        TextView authenticationacts = findViewById(R.id.authenticationacts);
        authenticationacts.setOnClickListener(view -> {
            Intent intent = new Intent(FoodTechReporting.this, food.class);
            startActivity(intent);
            finish();
        });
    }

    private void setTextSize(TextView textView, int sizeInSp) {
        // Convert sp to pixels
        float scale = getResources().getDisplayMetrics().density;
        int sizeInPx = (int) (sizeInSp * scale + 0.5f);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeInPx);
    }

    private void fetchRecipesFromDatabase() {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder recipesList = new StringBuilder();

                final int columnWidth = 50;
                final String headerFormat = "%-" + columnWidth + "s %-" + columnWidth + "s %-" + columnWidth + "s\n";
                recipesList.append(String.format(headerFormat, "FoodTech", "Title", "Rating"))
                        .append("\n")
                        .append("__________________________________________________________________________________________________________________\n");

                if (dataSnapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        String title = recipeSnapshot.child("Title").getValue(String.class);
                        String userId = recipeSnapshot.child("UserId").getValue(String.class);
                        String recipeId = recipeSnapshot.getKey();

                        fetchFoodtechNameAndRating(userId, title, recipeId, recipesList, columnWidth);
                    }
                } else {
                    Toast.makeText(FoodTechReporting.this, "No recipes available.", Toast.LENGTH_SHORT).show();
                }

                // Display the recipes list
                recipeTitle.setText(recipesList.length() > 0 ? recipesList.toString() : "No recipes available.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FoodTechReporting.this, "Failed to load recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFoodtechNameAndRating(String userId, String title, String recipeId, StringBuilder recipesList, int columnWidth) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("foodtech").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String foodTechName = documentSnapshot.exists() ? documentSnapshot.getString("fullName") : "N/A";
                    fetchAverageRating(recipeId, foodTechName, title, recipesList, columnWidth);
                });
    }

    private void fetchAverageRating(String recipeId, String foodTechName, String title, StringBuilder recipesList, int columnWidth) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("UserLikes").child(recipeId);

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalRating = 0;
                int ratingCount = 0;

                for (DataSnapshot ratingSnapshot : dataSnapshot.getChildren()) {
                    if (ratingSnapshot.getKey().equals("timestamp")) continue;

                    Integer rating = ratingSnapshot.child("rating").getValue(Integer.class);
                    if (rating != null) {
                        totalRating += rating;
                        ratingCount++;
                    }
                }

                float averageRating = ratingCount > 0 ? (float) totalRating / ratingCount : 0;
                String formattedRating = String.format("%.2f", averageRating);

                final String rowFormat = "%-" + columnWidth + "s %-" + columnWidth + "s %-" + columnWidth + "s\n";
                recipesList.append("\n")  // Add extra space before each new entry
                        .append(String.format(rowFormat, foodTechName, title, formattedRating))

                        .append("__________________________________________________________________________________________________________________\n");

                runOnUiThread(() -> recipeTitle.setText(recipesList.toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FoodTechReporting.this, "Failed to load ratings.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showFilterDialog() {
        // Options for the filter, excluding "Most Created Recipes"
        final String[] filterOptions = {"Most Rated Recipes", "Most Rated Food Technologists"};

        // Variable to store the index of the selected option
        final int[] selectedOptionIndex = {-1};  // Default to -1 for no selection

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  // Make sure 'this' is the Activity context
        builder.setTitle("Select Filter Option")
                .setSingleChoiceItems(filterOptions, selectedOptionIndex[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Store the index of the selected option
                        selectedOptionIndex[0] = which;
                    }
                })
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // When "Apply" is clicked, get the selected option
                        if (selectedOptionIndex[0] == -1) {
                            Toast.makeText(getApplicationContext(), "No filter selected", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String selectedFilter = filterOptions[selectedOptionIndex[0]];

                        // Save the selected filter in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("selectedFilter", selectedFilter);
                        editor.apply();  // Save the filter selection

                        // Handle the selected filter
                        if ("Most Rated Recipes".equals(selectedFilter) || "Most Rated Food Technologists".equals(selectedFilter)) {
                            // Show filter2 (time range) option only if a "Most Rated" filter is selected
                            filter2.setVisibility(TextView.VISIBLE);

                            if ("Most Rated Recipes".equals(selectedFilter)) {
                                FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
                                MostRatedRecipes mostRatedRecipes = new MostRatedRecipes(database, recipesRef, firestoreDb, recipeTitle);
                                mostRatedRecipes.fetchAndDisplayMostRatedRecipes(false, false, false);
                            } else {
                                MostRatedFoodtech mostRatedFoodtech = new MostRatedFoodtech(database, recipesRef, recipeTitle);
                                mostRatedFoodtech.fetchAndDisplayMostRatedFoodtech(false, false, false);
                            }
                        } else {
                            // Hide filter2 if "Most Rated" filter is not selected
                            filter2.setVisibility(TextView.GONE);
                        }
                    }
                })
                .setNegativeButton("Cancel", null);  // Simply dismiss the dialog

        // Show the dialog
        builder.create().show();
    }

    private void showDateDialog() {
        final String[] filterOptions = {"Weekly", "Monthly", "Yearly"};
        final int[] selectedOptionIndex = {-1};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Time Range")
                .setSingleChoiceItems(filterOptions, selectedOptionIndex[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOptionIndex[0] = which;
                    }
                })
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedOptionIndex[0] == -1) {
                            Toast.makeText(getApplicationContext(), "No time range selected", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String selectedTimeRange = filterOptions[selectedOptionIndex[0]];

                        // Save the selected time range
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("selectedTimeRange", selectedTimeRange);
                        editor.apply();

                        // Retrieve the selected filter
                        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                        String selectedFilter = preferences.getString("selectedFilter", "");

                        boolean isWeeklySelected = "Weekly".equals(selectedTimeRange);
                        boolean isMonthlySelected = "Monthly".equals(selectedTimeRange);
                        boolean isYearlySelected = "Yearly".equals(selectedTimeRange);

                        FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();  // Initialize Firestore here

                        // Call the appropriate method based on selected filter
                        if ("Most Rated Recipes".equals(selectedFilter)) {
                            MostRatedRecipes mostRatedRecipes = new MostRatedRecipes(database, recipesRef, firestoreDb, recipeTitle);
                            mostRatedRecipes.fetchAndDisplayMostRatedRecipes(isWeeklySelected, isMonthlySelected, isYearlySelected);

                        } else if ("Most Rated Food Technologists".equals(selectedFilter)) {
                            MostRatedFoodtech mostRatedFoodtech = new MostRatedFoodtech(database, recipesRef, recipeTitle);
                            mostRatedFoodtech.fetchAndDisplayMostRatedFoodtech(isWeeklySelected, isMonthlySelected, isYearlySelected);

                        } else if ("Most Created Recipes".equals(selectedFilter)) {
                            MostCreatedRecipes mostCreatedRecipes = new MostCreatedRecipes(database, recipesRef, recipeTitle);
                            mostCreatedRecipes.fetchAndDisplayMostCreatedRecipes(isWeeklySelected, isMonthlySelected, isYearlySelected);
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
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
}
