package com.myapp.reluto;

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

public class MostRatedFoodtech {

    private FirebaseDatabase database;
    private DatabaseReference recipesRef;
    private DatabaseReference userLikesRef;
    private TextView recipeTitle;

    public MostRatedFoodtech(FirebaseDatabase database, DatabaseReference recipesRef, TextView recipeTitle) {
        this.database = database;
        this.recipesRef = recipesRef;
        this.userLikesRef = database.getReference("UserLikes");
        this.recipeTitle = recipeTitle;
    }

    public void fetchAndDisplayMostRatedFoodtech(boolean isWeeklyFilter, boolean isMonthlyFilter, boolean isYearlyFilter) {
        final Map<String, Float> foodTechRatings = new HashMap<>();  // Store FoodTech name and accumulated ratings
        final Map<String, Integer> foodTechRatingCounts = new HashMap<>();  // Track the number of rated recipes for each FoodTech

        // Fetch all recipes from the database
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // To keep track of recipes being processed
                final int[] processedRecipes = {0};
                final int totalRecipes = (int) dataSnapshot.getChildrenCount(); // Total recipes to process

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    String userId = recipeSnapshot.child("UserId").getValue(String.class);

                    // Fetch UserId for each FoodTech and calculate ratings for their recipes
                    if (userId != null) {
                        // For each FoodTech, get the ratings of their recipe
                        userLikesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot ratingSnapshot) {
                                int totalLikes = 0;
                                int userCount = 0;

                                // Sum the ratings for this recipe, ignoring "timestamp"
                                for (DataSnapshot rating : ratingSnapshot.getChildren()) {
                                    // Skip timestamp node
                                    if (rating.getKey().equals("timestamp")) {
                                        continue;
                                    }

                                    // Get the rating value (assuming it's stored under "rating" key)
                                    Integer ratingValue = rating.child("rating").getValue(Integer.class);
                                    if (ratingValue != null) {
                                        // Get the timestamp of the rating
                                        String timestampStr = rating.child("timestamp").child("0").getValue(String.class);
                                        if (timestampStr != null) {
                                            // Convert the timestamp to a long value
                                            try {
                                                long timestamp = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault())
                                                        .parse(timestampStr).getTime();

                                                // Check if the rating is within the selected time range
                                                if (isRatingWithinTimeRange(timestamp, isWeeklyFilter, isMonthlyFilter, isYearlyFilter)) {
                                                    totalLikes += Math.min(ratingValue, 5);  // Cap the rating at 5
                                                    userCount++;
                                                }
                                            } catch (Exception e) {
                                                Log.e("Firebase", "Error parsing timestamp", e);
                                            }
                                        }
                                    }
                                }

                                // Only calculate average if there are ratings
                                if (userCount > 0) {
                                    float averageRating = (float) totalLikes / userCount;

                                    // Fetch the FoodTech name from Firestore using userId
                                    FirebaseFirestore.getInstance().collection("foodtech").document(userId)
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    String foodTechName = documentSnapshot.getString("fullName");
                                                    if (foodTechName != null) {
                                                        // Add the average rating of this recipe to the overall rating for the FoodTech
                                                        if (foodTechRatings.containsKey(foodTechName)) {
                                                            // Accumulate the total rating and the count of rated recipes for the FoodTech
                                                            foodTechRatings.put(foodTechName, foodTechRatings.get(foodTechName) + averageRating);
                                                            foodTechRatingCounts.put(foodTechName, foodTechRatingCounts.get(foodTechName) + 1);
                                                        } else {
                                                            foodTechRatings.put(foodTechName, averageRating);
                                                            foodTechRatingCounts.put(foodTechName, 1);  // First rated recipe
                                                        }
                                                    } else {
                                                        // Log for missing name
                                                        Log.e("FoodTech", "FoodTech name not found for userId: " + userId);
                                                    }
                                                } else {
                                                    // If Firestore document does not exist
                                                    Log.e("Firestore", "No document found for userId: " + userId);
                                                }

                                                processedRecipes[0]++;

                                                // After processing all recipes, calculate the average rating for each FoodTech
                                                if (processedRecipes[0] == totalRecipes) {
                                                    // After processing all recipes, calculate the final average for each FoodTech
                                                    for (Map.Entry<String, Float> entry : foodTechRatings.entrySet()) {
                                                        String foodTechName = entry.getKey();
                                                        float totalRating = entry.getValue();
                                                        int ratedRecipeCount = foodTechRatingCounts.get(foodTechName);

                                                        // Calculate the final average rating
                                                        float finalAverageRating = totalRating / ratedRecipeCount;
                                                        foodTechRatings.put(foodTechName, finalAverageRating);  // Update with final average
                                                    }

                                                    // Call display method with the filter selected
                                                    displayFoodtechRatings(foodTechRatings, isWeeklyFilter, isMonthlyFilter, isYearlyFilter);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle Firestore error if name fetch fails
                                                Log.e("Firestore", "Failed to fetch name for userId: " + userId, e);
                                                processedRecipes[0]++;
                                            });
                                } else {
                                    // No ratings for this recipe, skip
                                    processedRecipes[0]++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle possible errors
                                Toast.makeText(recipeTitle.getContext(), "Error fetching ratings", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(recipeTitle.getContext(), "Failed to load recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to check if the rating is within the selected time range
    private boolean isRatingWithinTimeRange(long timestamp, boolean isWeeklyFilter, boolean isMonthlyFilter, boolean isYearlyFilter) {
        long currentTime = System.currentTimeMillis();

        // Get the time ranges in milliseconds
        long weekMillis = 7 * 24 * 60 * 60 * 1000L; // 7 days
        long monthMillis = 30 * 24 * 60 * 60 * 1000L; // 30 days
        long yearMillis = 365 * 24 * 60 * 60 * 1000L; // 365 days

        if (isWeeklyFilter) {
            return timestamp >= currentTime - weekMillis;
        } else if (isMonthlyFilter) {
            return timestamp >= currentTime - monthMillis;
        } else if (isYearlyFilter) {
            return timestamp >= currentTime - yearMillis;
        }
        return true; // If no time range is selected, treat it as always valid (for default sorting)
    }

    // Display FoodTech ratings sorted by highest average rating (descending order)
    // Display FoodTech ratings sorted by highest average rating (descending order)
    private void displayFoodtechRatings(Map<String, Float> foodTechRatings, boolean isWeeklyFilter, boolean isMonthlyFilter, boolean isYearlyFilter) {
        StringBuilder ratingsDisplay = new StringBuilder();

        // Define column width for alignment
        final int columnWidth = 20;
        final String headerFormat = "%-" + columnWidth + "s %-10s\n";  // Adjusted for alignment
        ratingsDisplay.append(String.format(headerFormat, "FoodTech Name", "Rating"))
                .append("_____________________________________________\n\n");

        // Convert Map to List of Entries for sorting
        ArrayList<Map.Entry<String, Float>> sortedRatings = new ArrayList<>(foodTechRatings.entrySet());

        // Sort by highest to lowest average rating (descending order)
        sortedRatings.sort((entry1, entry2) -> Float.compare(entry2.getValue(), entry1.getValue()));  // Descending order

        // Format and append the sorted ratings
        for (Map.Entry<String, Float> entry : sortedRatings) {
            String foodTechName = entry.getKey();  // FoodTech name
            float averageRating = entry.getValue();  // Average rating

            ratingsDisplay.append(String.format("%-" + columnWidth + "s %.2f\n", foodTechName, averageRating))
                    .append("_____________________________________________\n\n");  // Add separator and space
        }

        // Update the UI on the main thread
        Activity activity = (Activity) recipeTitle.getContext();
        activity.runOnUiThread(() -> recipeTitle.setText(ratingsDisplay.toString()));
    }


}
