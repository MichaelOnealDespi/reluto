package com.myapp.reluto.Activity;

import android.app.Activity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class MostYear {

    private TextView recipeTitle;
    private boolean isFoodTechSelected;  // true for Food Technologists, false for Recipes
    private boolean isYearlySelected;   // true for Yearly filter, false for other time filters

    // Constructor with the necessary flags
    public MostYear(TextView recipeTitle, boolean isFoodTechSelected, boolean isYearlySelected) {
        this.recipeTitle = recipeTitle;
        this.isFoodTechSelected = isFoodTechSelected;
        this.isYearlySelected = isYearlySelected;
    }

    // Main method to display ratings
    public void displayFoodtechRatings(Map<String, Float> ratings) {
        StringBuilder ratingsDisplay = new StringBuilder();

        if (isFoodTechSelected) {
            sortAndDisplayFoodTechRatings(ratings, ratingsDisplay);
        } else {
            sortAndDisplayRecipesRatings(ratings, ratingsDisplay);
        }

        // Update UI
        Activity activity = (Activity) recipeTitle.getContext();
        activity.runOnUiThread(() -> recipeTitle.setText(ratingsDisplay.toString()));
    }

    // Sort and display food technologists
    private void sortAndDisplayFoodTechRatings(Map<String, Float> ratings, StringBuilder ratingsDisplay) {
        ArrayList<Map.Entry<String, Float>> sortedRatings = new ArrayList<>(ratings.entrySet());

        if (isYearlySelected) {
            sortedRatings.sort((entry1, entry2) -> Float.compare(entry1.getValue(), entry2.getValue()));  // Ascending order
        } else {
            sortedRatings.sort((entry1, entry2) -> Float.compare(entry2.getValue(), entry1.getValue()));  // Descending order
        }

        // Format and display
        for (Map.Entry<String, Float> entry : sortedRatings) {
            ratingsDisplay.append(String.format("%-20s %.2f\n", entry.getKey(), entry.getValue()));
        }
    }

    // Sort and display recipes
    private void sortAndDisplayRecipesRatings(Map<String, Float> ratings, StringBuilder ratingsDisplay) {
        ArrayList<Map.Entry<String, Float>> sortedRatings = new ArrayList<>(ratings.entrySet());

        if (isYearlySelected) {
            sortedRatings.sort((entry1, entry2) -> Float.compare(entry1.getValue(), entry2.getValue()));  // Ascending order
        } else {
            sortedRatings.sort((entry1, entry2) -> Float.compare(entry2.getValue(), entry1.getValue()));  // Descending order
        }

        // Format and display
        for (Map.Entry<String, Float> entry : sortedRatings) {
            ratingsDisplay.append(String.format("%-20s %.2f\n", entry.getKey(), entry.getValue()));
        }
    }
    // Method to display the recipe counts for FoodTechnologists
    private void displayFoodTechRecipeCounts(Map<String, Integer> foodTechRecipeCounts, boolean isWeeklySelected) {
        StringBuilder recipeCountsDisplay = new StringBuilder();

        // Sort the recipe counts based on the selected time range (Weekly or default descending)
        ArrayList<Map.Entry<String, Integer>> sortedRecipeCounts = new ArrayList<>(foodTechRecipeCounts.entrySet());

        // Weekly selected -> sort lowest to highest, otherwise highest to lowest
        if (isYearlySelected) {
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