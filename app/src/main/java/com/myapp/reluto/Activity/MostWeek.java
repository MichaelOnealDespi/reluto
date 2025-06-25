package com.myapp.reluto.Activity;

import android.app.Activity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MostWeek {

    private TextView recipeTitle;
    private boolean isFoodTechSelected;  // true for Food Technologists, false for Recipes
    private boolean isWeeklySelected;   // true for Weekly filter, false for other time filters

    // Constructor with the necessary flags
    public MostWeek(TextView recipeTitle, boolean isFoodTechSelected, boolean isWeeklySelected) {
        this.recipeTitle = recipeTitle;
        this.isFoodTechSelected = isFoodTechSelected;
        this.isWeeklySelected = isWeeklySelected;
    }

    // Main method that is called to display ratings
    public void displayFoodtechRatings(Map<String, Map<String, Object>> ratings) {
        StringBuilder ratingsDisplay = new StringBuilder();

        // Check if "Most Rated Food Technologists" or "Most Rated Recipes" is selected
        if (isFoodTechSelected) {
            // If "Most Rated Food Technologists" is selected, use the food technologist sorting method
            sortAndDisplayFoodTechRatings(ratings, ratingsDisplay);
        } else {
            // If "Most Rated Recipes" is selected, use the recipe sorting method
            sortAndDisplayRecipesRatings(ratings, ratingsDisplay);
        }

        // Display the result on the UI thread
        Activity activity = (Activity) recipeTitle.getContext();
        activity.runOnUiThread(() -> recipeTitle.setText(ratingsDisplay.toString()));
    }

    // Sort and display food technologists based on their ratings (adjusted for Weekly)
    private void sortAndDisplayFoodTechRatings(Map<String, Map<String, Object>> ratings, StringBuilder ratingsDisplay) {
        ArrayList<Map.Entry<String, Map<String, Object>>> sortedRatings = new ArrayList<>(ratings.entrySet());

        // Sort based on timestamp (most recent first)
        Collections.sort(sortedRatings, (entry1, entry2) -> {
            String timestamp1 = (String) entry1.getValue().get("timestamp");
            String timestamp2 = (String) entry2.getValue().get("timestamp");
            return timestamp2.compareTo(timestamp1);  // Sort descending (most recent first)
        });

        // Format and add to the result
        for (Map.Entry<String, Map<String, Object>> entry : sortedRatings) {
            String foodTechName = entry.getKey();  // FoodTech name
            String timestamp = (String) entry.getValue().get("timestamp");  // Timestamp
            ratingsDisplay.append(String.format("%-20s %s\n", foodTechName, timestamp));
        }
    }

    // Sort and display recipes based on their ratings (adjusted for Weekly)
    private void sortAndDisplayRecipesRatings(Map<String, Map<String, Object>> ratings, StringBuilder ratingsDisplay) {
        ArrayList<Map.Entry<String, Map<String, Object>>> sortedRatings = new ArrayList<>(ratings.entrySet());

        // Sort based on timestamp (most recent first)
        Collections.sort(sortedRatings, (entry1, entry2) -> {
            String timestamp1 = (String) entry1.getValue().get("timestamp");
            String timestamp2 = (String) entry2.getValue().get("timestamp");
            return timestamp2.compareTo(timestamp1);  // Sort descending (most recent first)
        });

        // Format and add to the result
        for (Map.Entry<String, Map<String, Object>> entry : sortedRatings) {
            String recipeName = entry.getKey();  // Recipe name
            String timestamp = (String) entry.getValue().get("timestamp");  // Timestamp
            ratingsDisplay.append(String.format("%-20s %s\n", recipeName, timestamp));
        }
    }

    // Method to display the recipe counts for FoodTechnologists
    private void displayFoodTechRecipeCounts(Map<String, Integer> foodTechRecipeCounts, boolean isWeeklySelected) {
        StringBuilder recipeCountsDisplay = new StringBuilder();

        // Sort the recipe counts based on the selected time range (Weekly or default descending)
        ArrayList<Map.Entry<String, Integer>> sortedRecipeCounts = new ArrayList<>(foodTechRecipeCounts.entrySet());

        // Weekly selected -> sort lowest to highest, otherwise highest to lowest
        if (isWeeklySelected) {
            sortedRecipeCounts.sort((entry1, entry2) -> Integer.compare(entry1.getValue(), entry2.getValue()));  // Sort ascending (lowest to highest)
        } else {
            sortedRecipeCounts.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));  // Sort descending (highest to lowest)
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

