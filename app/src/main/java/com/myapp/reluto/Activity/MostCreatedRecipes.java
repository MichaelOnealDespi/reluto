package com.myapp.reluto.Activity;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MostCreatedRecipes {

    private final DatabaseReference userLikesRef;
    private FirebaseDatabase database;
    private DatabaseReference recipesRef;
    private TextView recipeTitle;

    public MostCreatedRecipes(FirebaseDatabase database, DatabaseReference recipesRef, TextView recipeTitle) {
        this.database = database;
        this.recipesRef = recipesRef;
        this.userLikesRef = database.getReference("UserLikes");
        this.recipeTitle = recipeTitle;
    }

    public void fetchAndDisplayMostCreatedRecipes(boolean isWeeklySelected, boolean isMonthlySelected, boolean isYearlySelected) {
        final Map<String, Integer> foodTechRecipeCounts = new HashMap<>();  // Store FoodTech name and the number of recipes they created

        // Fetch all recipes from the database
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // To keep track of recipes being processed
                final int[] processedRecipes = {0};
                final int totalRecipes = (int) dataSnapshot.getChildrenCount(); // Total recipes to process

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String userId = recipeSnapshot.child("UserId").getValue(String.class);

                    // Fetch UserId for each FoodTech and count the number of recipes they created
                    if (userId != null) {
                        // Fetch the FoodTech name from Firestore using userId
                        FirebaseFirestore.getInstance().collection("foodtech").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String foodTechName = documentSnapshot.getString("fullName");
                                        if (foodTechName != null) {
                                            // Increment the recipe count for the FoodTech
                                            foodTechRecipeCounts.put(foodTechName,
                                                    foodTechRecipeCounts.getOrDefault(foodTechName, 0) + 1);
                                        } else {
                                            // Log for missing name
                                            Log.e("FoodTech", "FoodTech name not found for userId: " + userId);
                                        }
                                    } else {
                                        // If Firestore document does not exist
                                        Log.e("Firestore", "No document found for userId: " + userId);
                                    }

                                    // After processing all recipes, display the FoodTechs with their recipe counts
                                    processedRecipes[0]++;
                                    if (processedRecipes[0] == totalRecipes) {
                                        displayFoodTechRecipeCounts(foodTechRecipeCounts, isWeeklySelected, isMonthlySelected, isYearlySelected);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle Firestore error if name fetch fails
                                    Log.e("Firestore", "Failed to fetch name for userId: " + userId, e);
                                    processedRecipes[0]++;
                                });
                    } else {
                        // No UserId for this recipe, skip it
                        processedRecipes[0]++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(recipeTitle.getContext(), "Failed to load recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFoodTechRecipeCounts(Map<String, Integer> foodTechRecipeCounts,
                                             boolean isWeeklySelected, boolean isMonthlySelected, boolean isYearlySelected) {
        StringBuilder recipeCountsDisplay = new StringBuilder();

        // Sort the recipe counts based on the selected time range (Weekly, Monthly, or default descending)
        ArrayList<Map.Entry<String, Integer>> sortedRecipeCounts = new ArrayList<>(foodTechRecipeCounts.entrySet());

        // Default sort by recipe count in descending order
        sortedRecipeCounts.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));  // Descending order

        // Apply time range filter (optional)
        if (isWeeklySelected || isMonthlySelected || isYearlySelected) {
            sortedRecipeCounts.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));  // Still sort by recipe count descending
        }

        // Format and append to the result
        for (Map.Entry<String, Integer> entry : sortedRecipeCounts) {
            String foodTechName = entry.getKey();  // FoodTech name
            int recipeCount = entry.getValue();  // Number of recipes created by this FoodTech
            recipeCountsDisplay.append(String.format("%-20s %d\n", foodTechName, recipeCount));
        }

        // Display the result on the UI thread
        Activity activity = (Activity) recipeTitle.getContext();
        activity.runOnUiThread(() -> recipeTitle.setText(recipeCountsDisplay.toString()));
    }
}
