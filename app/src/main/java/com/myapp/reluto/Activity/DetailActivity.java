package com.myapp.reluto.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.myapp.reluto.Domain.Foods;
import com.myapp.reluto.R;
import com.myapp.reluto.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private Foods foodItem;
    private Foods object;

    private DatabaseReference databaseReference;
    private DatabaseReference userLikesReference;
    private FirebaseFirestore firestore;
    private DatabaseReference commentSectionReference;  // Reference to the comments section

    private String userId;
    private TextView recipeEditButton;
    private TextView clearRating;
    private float previousRating = 0;
    private TextView commentCountTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recipeEditButton = findViewById(R.id.RecipeEdt);
        clearRating = findViewById(R.id.clearRating);

        databaseReference = FirebaseDatabase.getInstance().getReference("Foods");
        userLikesReference = FirebaseDatabase.getInstance().getReference("UserLikes");
        firestore = FirebaseFirestore.getInstance();

        // Add a reference to the comments section of the food item
        commentSectionReference = FirebaseDatabase.getInstance().getReference("CommentSections");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        retrieveIntentData();
        getIntentExtra();
        initializeUI();
        setupRatingBar();
        loadAverageRating();
        loadCreatorName();

        // Fetch and display the number of comments
        loadCommentCount();

        if (foodItem == null) {
            Log.e("DetailActivity", "foodItem is null!");
            Toast.makeText(DetailActivity.this, "Food item not available.", Toast.LENGTH_SHORT).show();
        }

        recipeEditButton.setOnClickListener(view -> {
            Intent intent = new Intent(DetailActivity.this, EditRecipeActivity.class);
            intent.putExtra("recipeId", foodItem.getRecipeId());
            startActivity(intent);
        });

        // Hide the Edit button if the user is not the creator of the food item
        if (!userId.equals(foodItem.getUserId())) {
            recipeEditButton.setVisibility(View.GONE);
        }

        clearRating.setOnClickListener(view -> unrateUser());
    }

    private void loadCommentCount() {
        if (foodItem != null) {
            String recipeId = foodItem.getRecipeId();
            Log.d("DetailActivity", "Fetching comment count for recipe ID: " + recipeId);

            // Fetch the number of comments from the database (assuming it's stored in "Comments/{recipeId}")
            commentSectionReference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Count the number of comments
                    long commentCount = snapshot.getChildrenCount();
                    Log.d("commentsRecyclerView", "Number of comments: " + commentCount);

                    // Update the comment count text view
                    binding.commentCountTxt.setText(commentCount + " Comments");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DetailActivity", "Failed to load comments count: " + error.getMessage());
                    Toast.makeText(DetailActivity.this, "Failed to load comments count.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reload the comment count when DetailActivity resumes
        loadCommentCount();
        loadAverageRating();

    }

    private void initializeUI() {
        binding.backBtn.setOnClickListener(view -> finish());

        if (foodItem != null) {
            Glide.with(DetailActivity.this)
                    .load(foodItem.getImagePath())
                    .into(binding.pic);

            binding.titleTxt.setText(foodItem.getTitle());
            binding.descriptionTxt.setText(foodItem.getDescription());
            binding.ingredientsTxt.setText(foodItem.getIngredients());
            binding.notesTxt.setText(foodItem.getComments());
            binding.procedureTxt.setText(foodItem.getProcedure());
            binding.equipmentTxt.setText(foodItem.getEquipment());
            binding.anotherTxt.setText(foodItem.getAnother());
            binding.notesaaTxt.setText(foodItem.getNotes());

            String categories = foodItem.getCategoryName();
            List<Integer> categoryIds = new ArrayList<>();
            StringBuilder formattedCategories = new StringBuilder();

            if (categories != null && !categories.isEmpty()) {
                String[] categoryArray = categories.split(",\\s*");
                List<String> categoryList = new ArrayList<>();
                boolean hasMore = false;

                for (String category : categoryArray) {
                    category = category.trim();
                    if (category.equalsIgnoreCase("More")) {
                        hasMore = true;
                    } else {
                        categoryList.add(category);
                        int categoryId = foodItem.getCategoryId();
                        categoryIds.add(categoryId);
                    }
                }

                if (!categoryList.isEmpty()) {
                    formattedCategories.append("Perfect for ").append(String.join(", ", categoryList));
                    if (hasMore) {
                        formattedCategories.append(", and More!");
                    } else {
                        formattedCategories.append("!");
                    }
                } else if (hasMore) {
                    formattedCategories.append("Perfect for More!");
                } else {
                    formattedCategories.append("No categories available.");
                }
            } else {
                formattedCategories.append("No categories available.");
            }

            binding.categoryTxt.setText(formattedCategories.toString());
        }
        loadCommentCount();
    }

    private void retrieveIntentData() {foodItem = (Foods) getIntent().getSerializableExtra("object");
    }

    private void getIntentExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }

    private void setupRatingBar() {
        // Check if the user is viewing their own recipe
        if (userId.equals(object.getUserId())) {
            binding.likebtn.setIsIndicator(true); // Make RatingBar non-clickable
        } else {
            binding.likebtn.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    if (rating != previousRating) {
                        updateUserLikes((int) rating); // Save the new rating
                        previousRating = rating;
                        clearRating.setVisibility(View.VISIBLE);
                    } else {
                        ratingBar.setRating(previousRating); // Keep the same rating
                    }
                }
            });
        }
    }



    private void loadAverageRating() {
        if (object != null) {
            userLikesReference.child(object.getRecipeId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalRating = 0;
                    int ratingCount = 0;
                    boolean userHasRated = false;

                    // Iterate over each userId node (which contains both rating and timestamp)
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Check if the current userSnapshot contains the "rating" field
                        Integer rating = userSnapshot.child("rating").getValue(Integer.class);
                        if (rating != null) {
                            totalRating += rating;  // Add to total rating sum
                            ratingCount++;  // Increment the rating count

                            // Check if the current user is the one who rated
                            if (userSnapshot.getKey().equals(userId)) {
                                userHasRated = true;
                            }
                        }
                    }

                    // Calculate the average rating
                    float averageRating = ratingCount > 0 ? (float) totalRating / ratingCount : 0;
                    String formattedAverageRating = String.format("%.2f", averageRating);

                    // Update the UI with the calculated average rating
                    binding.likecount.setText(formattedAverageRating);
                    binding.likebtn.setRating(averageRating); // Set the RatingBar with the average rating
                    previousRating = averageRating;

                    // Update the number of ratings (in parentheses)
                    binding.howmanycount.setText("(" + ratingCount + ")");

                    // Show or hide the "clearRating" button based on whether the user has rated
                    clearRating.setVisibility(userHasRated ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error loading average rating: " + error.getMessage());
                }
            });
        }
    }






    private void updateUserLikes(int rating) {
        if (object != null && userId != null) {
            // Get the current timestamp in milliseconds
            long timestamp = System.currentTimeMillis();

            // Format the timestamp to a human-readable date string
            String dateFormatted = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date(timestamp));  // Example: "Nov 14, 2024 00:13:29"

            // Reference to the recipe node in the Firebase database
            DatabaseReference recipeRef = userLikesReference.child(object.getRecipeId());

            // Save the rating directly under the userId
            recipeRef.child(userId).child("rating").setValue(rating)
                    .addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("Firebase", "Rating saved successfully");

                            // Save the timestamp under the `timestamp` node for this user
                            Map<String, Object> timestampData = new HashMap<>();
                            timestampData.put("0", dateFormatted);  // Add timestamp as key 0

                            // Save the timestamp under the `timestamp` node for this user
                            recipeRef.child(userId).child("timestamp").setValue(timestampData)
                                    .addOnCompleteListener(timestampTask -> {
                                        if (timestampTask.isSuccessful()) {
                                            Log.d("Firebase", "Timestamp saved successfully");
                                            Toast.makeText(DetailActivity.this, "You rated: " + rating, Toast.LENGTH_SHORT).show();
                                            loadAverageRating();  // Re-load the average rating
                                            clearRating.setVisibility(View.VISIBLE);
                                        } else {
                                            Log.e("Firebase", "Failed to save timestamp", timestampTask.getException());
                                            Toast.makeText(DetailActivity.this, "Failed to update Timestamp", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.e("Firebase", "Failed to save rating", updateTask.getException());
                            Toast.makeText(DetailActivity.this, "Failed to update Rating", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("Firebase", "Object or UserId is null, cannot update Firebase.");
        }
    }


    private void unrateUser() {
        if (object != null && userId != null) {
            userLikesReference.child(object.getRecipeId()).child(userId).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DetailActivity.this, "Rating removed!", Toast.LENGTH_SHORT).show();
                            loadAverageRating();  // Re-load the average rating
                            clearRating.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(DetailActivity.this, "Failed to remove rating", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadCreatorName() {
        if (foodItem != null) {
            firestore.collection("foodtech").document(foodItem.getUserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String creatorName = document.getString("fullName");
                        String creatorLname = document.getString("lastName");

                        if (userId.equals(foodItem.getUserId())) {
                            binding.foodtechname.setVisibility(View.INVISIBLE);
                        } else {
                            binding.foodtechname.setText("by: " + creatorName + " " + creatorLname);
                            // Set an OnClickListener to navigate to the creator's profile
                            binding.foodtechname.setOnClickListener(view -> {
                                Intent intent = new Intent(DetailActivity.this, FoodtechProfileActivity.class);
                                intent.putExtra("userId", foodItem.getUserId());  // Pass the userId of the creator
                                startActivity(intent);
                            });
                        }
                    } else {
                        binding.foodtechname.setText("Creator name not available");
                    }
                } else {
                    binding.foodtechname.setText("Failed to load creator name");
                }
            });
        }
    }


    public void openComments(View view) {
        Intent intent = new Intent(DetailActivity.this, CommentsActivity.class);
        intent.putExtra("object", foodItem);  // Passing the foodItem object to CommentsActivity
        startActivity(intent);
    }
}