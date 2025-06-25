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
import java.util.List;
import java.util.Map;

public class MostRatedRecipes {

    private FirebaseDatabase database;
    private DatabaseReference recipesRef;
    private FirebaseFirestore firestoreDb;
    private TextView recipeTitle;

    public MostRatedRecipes(FirebaseDatabase database, DatabaseReference recipesRef, FirebaseFirestore firestoreDb, TextView recipeTitle) {
        this.database = database;
        this.recipesRef = recipesRef;
        this.firestoreDb = firestoreDb;
        this.recipeTitle = recipeTitle;
    }

    // Fetch and display the most-rated recipes
    public void fetchAndDisplayMostRatedRecipes(boolean isWeeklySelected, boolean isMonthlySelected, boolean isYearlySelected) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Map<String, Object>> recipesList = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        String title = recipeSnapshot.child("Title").getValue(String.class);
                        String userId = recipeSnapshot.child("UserId").getValue(String.class);
                        String recipeId = recipeSnapshot.getKey();

                        // Fetch FoodTech name and rating for each recipe
                        fetchFoodtechNameAndRating(userId, title, recipeId, recipesList, isWeeklySelected, isMonthlySelected, isYearlySelected);
                    }
                } else {
                    Toast.makeText(recipeTitle.getContext(), "No recipes available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(recipeTitle.getContext(), "Failed to load recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch the FoodTech name and average rating for the recipe
    private void fetchFoodtechNameAndRating(String userId, String title, String recipeId, List<Map<String, Object>> recipesList,
                                            boolean isWeeklySelected, boolean isMonthlySelected, boolean isYearlySelected) {
        firestoreDb.collection("foodtech").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String foodTechName = documentSnapshot.exists() ? documentSnapshot.getString("fullName") : "N/A";
                    fetchAverageRating(recipeId, foodTechName, title, recipesList, isWeeklySelected, isMonthlySelected, isYearlySelected);
                })
                .addOnFailureListener(e -> {
                    // Handle the case where fetching FoodTech name fails
                    Log.e("MostRatedRecipes", "Failed to fetch FoodTech name: " + e.getMessage());
                });
    }

    private boolean isRatingWithinTimeRange(long timestamp, boolean isWeeklySelected, boolean isMonthlySelected, boolean isYearlySelected) {
        long currentTime = System.currentTimeMillis();

        // Get the time ranges in milliseconds
        long weekMillis = 7 * 24 * 60 * 60 * 1000L; // 7 days
        long monthMillis = 30 * 24 * 60 * 60 * 1000L; // 30 days
        long yearMillis = 365 * 24 * 60 * 60 * 1000L; // 365 days

        if (isWeeklySelected) {
            return timestamp >= currentTime - weekMillis;
        } else if (isMonthlySelected) {
            return timestamp >= currentTime - monthMillis;
        } else if (isYearlySelected) {
            return timestamp >= currentTime - yearMillis;
        }
        return true; // If no time range is selected, treat it as always valid (for default sorting)
    }


    // Fetch average rating for a given recipe
    private void fetchAverageRating(String recipeId, String foodTechName, String title, List<Map<String, Object>> recipesList,
                                    boolean isWeeklySelected, boolean isMonthlySelected, boolean isYearlySelected) {
        DatabaseReference ratingsRef = database.getReference("UserLikes").child(recipeId);

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalRating = 0;
                int ratingCount = 0;

                // Iterate over all ratings for this recipe
                for (DataSnapshot ratingSnapshot : dataSnapshot.getChildren()) {
                    // Skip the timestamp node
                    if (ratingSnapshot.getKey().equals("timestamp")) {
                        continue;
                    }

                    // Get the user's rating (assuming "rating" key holds the rating value)
                    Integer rating = ratingSnapshot.child("rating").getValue(Integer.class);
                    if (rating != null) {
                        // Get the timestamp of the rating
                        String timestampStr = ratingSnapshot.child("timestamp").child("0").getValue(String.class);
                        if (timestampStr != null) {
                            // Convert the timestamp to a long value
                            try {
                                long timestamp = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault())
                                        .parse(timestampStr).getTime();

                                // Check if the rating is within the selected time range
                                if (isRatingWithinTimeRange(timestamp, isWeeklySelected, isMonthlySelected, isYearlySelected)) {
                                    totalRating += rating;
                                    ratingCount++;
                                }
                            } catch (Exception e) {
                                Log.e("Firebase", "Error parsing timestamp", e);
                            }
                        }
                    }
                }

                // Calculate the average rating
                float averageRating = ratingCount > 0 ? (float) totalRating / ratingCount : 0;

                // Prepare recipe data with the calculated average rating
                Map<String, Object> recipeData = new HashMap<>();
                recipeData.put("title", title);
                recipeData.put("foodTechName", foodTechName);
                recipeData.put("rating", averageRating);  // Use the calculated average rating

                // Add the recipe data to the list
                recipesList.add(recipeData);

                // Debug log for added recipes
                Log.d("RecipeData", "Added recipe: " + recipeData);

                // Sort and display the recipes based on rating and selected time range
                sortAndDisplayRecipes(recipesList, isWeeklySelected, isMonthlySelected, isYearlySelected);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(recipeTitle.getContext(), "Failed to load ratings.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sortAndDisplayRecipes(List<Map<String, Object>> recipesList, boolean isWeeklySelected, boolean isMonthlySelected,
                                       boolean isYearlySelected) {
        // Separate sorting logic for Weekly, Monthly, and Yearly selections
        if (isWeeklySelected || isMonthlySelected || isYearlySelected) {
            // Sort for weekly, monthly, or yearly selection (highest to lowest rating)
            sortByRatingDescending(recipesList);  // Use descending order here
        } else {
            // Default sort (highest to lowest rating)
            sortByRatingDescending(recipesList);
        }

        // Format and append to the result
        StringBuilder sortedRecipes = new StringBuilder();
        final int columnWidth = 50;
        final String headerFormat = "%-" + columnWidth + "s %-" + columnWidth + "s %-" + columnWidth + "s\n\n";
        sortedRecipes.append(String.format(headerFormat, "FoodTech", "Title", "Rating"))
                .append("_____________________________________________________________________________________________________________\n\n");

        // Add each recipe to the display output
        for (Map<String, Object> recipe : recipesList) {
            String formattedRating = String.format("%.2f", recipe.get("rating")); // Format rating to 2 decimal places
            String rowFormat = "%-" + columnWidth + "s %-" + columnWidth + "s %-" + columnWidth + "s\n";
            sortedRecipes.append(String.format(rowFormat, recipe.get("foodTechName"), recipe.get("title"), formattedRating))
                    .append("_____________________________________________________________________________________________________________\n\n");
        }

        // Update UI on the main thread
        Activity activity = (Activity) recipeTitle.getContext();
        activity.runOnUiThread(() -> recipeTitle.setText(sortedRecipes.toString()));
    }

    // Method to sort recipes by rating in descending order
    private void sortByRatingDescending(List<Map<String, Object>> recipesList) {
        recipesList.sort((r1, r2) -> {
            float rating1 = (float) r1.get("rating");
            float rating2 = (float) r2.get("rating");
            return Float.compare(rating2, rating1); // Sort by rating in descending order
        });
    }


}
