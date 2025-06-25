package com.myapp.reluto.Activity;
import android.os.Bundle;

import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.myapp.reluto.Domain.Foods;


import com.myapp.reluto.databinding.ActivityCommentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private ActivityCommentsBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference commentSectionReference;
    private Foods foodItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backbtn.setOnClickListener(v -> finish());


        // Initialize Firebase references
        firebaseDatabase = FirebaseDatabase.getInstance();
        commentSectionReference = FirebaseDatabase.getInstance().getReference("CommentSections");

        // Retrieve the recipe data passed from DetailActivity
        foodItem = (Foods) getIntent().getSerializableExtra("object");

        if (foodItem == null) {
            Toast.makeText(this, "Failed to load recipe data", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if foodItem is null
            return;
        }
        // Initialize views
        // Set up RecyclerView
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load comments for the specific recipe
        loadComments();

        loadCommentCount();

        // Fetch and set the current user's full name
        setUserName();

        // Handle adding a new comment
        binding.postCommentButton.setOnClickListener(v -> {
            String commentText = binding.commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addComment(commentText);  // Add the comment to Firebase
            } else {
                Toast.makeText(CommentsActivity.this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String userId = currentUser.getUid();

            // Fetch full name from "users" collection
            firestore.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDocument = task.getResult();
                            if (userDocument.exists()) {
                                String firstName = userDocument.getString("fullName");
                                String lastName = userDocument.getString("lastName");
                                String fullName = firstName + " " + lastName;
                                binding.userNameTextView.setText(fullName);
                            } else {
                                // If not found in "users," check the "foodtech" collection
                                firestore.collection("foodtech").document(userId)
                                        .get()
                                        .addOnCompleteListener(foodtechTask -> {
                                            if (foodtechTask.isSuccessful()) {
                                                DocumentSnapshot foodtechDocument = foodtechTask.getResult();
                                                if (foodtechDocument.exists()) {
                                                    String firstName = foodtechDocument.getString("fullName");
                                                    String lastName = foodtechDocument.getString("lastName");
                                                    String fullName = firstName + " " + lastName;
                                                    binding.userNameTextView.setText(fullName);
                                                } else {
                                                    // Handle case where user is not in either collection
                                                    binding.userNameTextView.setText("User");
                                                }
                                            } else {
                                                // Handle failure to fetch foodtech details
                                                Toast.makeText(CommentsActivity.this, "Failed to load food technologist name", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // Handle failure to fetch user details
                            Toast.makeText(CommentsActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If the user is not logged in, show a default name or prompt
            binding.userNameTextView.setText("Guest User");
        }
    }


    private void loadComments() {
        commentSectionReference.child(foodItem.getRecipeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> commentsList = new ArrayList<>();

                // Iterate through the comments and create Comment objects
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    String userId = commentSnapshot.child("userId").getValue(String.class);
                    String commentText = commentSnapshot.child("commentText").getValue(String.class);
                    String timestamp = commentSnapshot.child("timestamp").getValue(String.class);

                    if (userId != null) {
                        // Fetch the full name for the user who commented
                        fetchUserFullName(userId, fullName -> {
                            if ("Unknown User".equals(fullName)) {
                                // If not found in "users," check the "foodtech" collection
                                fetchFoodtechFullName(userId, foodTechFullName -> {
                                    if (foodTechFullName != null) {
                                        commentsList.add(new Comment(foodTechFullName, commentText, timestamp));
                                    } else {
                                        commentsList.add(new Comment("Unknown User", commentText, timestamp));
                                    }
                                    updateCommentsUI(commentsList); // Update the UI with the comment list
                                });
                            } else {
                                commentsList.add(new Comment(fullName, commentText, timestamp));
                                updateCommentsUI(commentsList); // Update the UI with the comment list
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCommentCount() {
        if (foodItem != null) {
            String recipeId = foodItem.getRecipeId();
            Log.d("CommentsActivity", "Fetching comment count for recipe ID: " + recipeId);

            // Fetch the number of comments from the database (assuming it's stored in "Comments/{recipeId}")
            commentSectionReference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Count the number of comments
                    long commentCount = snapshot.getChildrenCount();
                    Log.d("commentsRecyclerView", "Number of comments: " + commentCount);

                    // Update the comment count text view
                    binding.commentCountText.setText(commentCount + " Comments");
                    //binding.commentCountTextView.setText(commentCount + " Comments");
                    // TextView commentCountTextView = findViewById(R.id.commentCountTextView);
                    // commentCountTextView.setText(commentCount + " Comments");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DetailActivity", "Failed to load comments count: " + error.getMessage());
                    Toast.makeText(CommentsActivity.this, "Failed to load comments count.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchUserFullName(String userId, final FullNameCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("fullName");
                            String lastName = documentSnapshot.getString("lastName");

                            // Combine first name and last name
                            String fullName = firstName + " " + lastName;
                            callback.onCallback(fullName); // Pass the full name back to the calling method
                        } else {
                            callback.onCallback("Unknown User");
                        }
                    } else {
                        callback.onCallback("Error fetching name");
                    }
                });
    }

    private void fetchFoodtechFullName(String userId, final FullNameCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("foodtech").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("fullName");
                            String lastName = documentSnapshot.getString("lastName");

                            // Combine first name and last name
                            String fullName = firstName + " " + lastName;
                            callback.onCallback(fullName); // Pass the full name back to the calling method
                        } else {
                            callback.onCallback(null); // User not found
                        }
                    } else {
                        callback.onCallback(null); // Error fetching name
                    }
                });
    }


    public interface FullNameCallback {
        void onCallback(String fullName);
    }

    public void updateCommentsUI(List<Comment> commentsList) {
        // Set up your adapter to bind the list of comments
        CommentsAdapter commentsAdapter = new CommentsAdapter(commentsList);
        binding.commentsRecyclerView.setAdapter(commentsAdapter);
    }

    public void addComment(String commentText) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (foodItem != null && !commentText.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            String dateFormatted = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                    .format(timestamp);

            Map<String, Object> commentData = new HashMap<>();
            commentData.put("userId", currentUser.getUid());
            commentData.put("commentText", commentText);
            commentData.put("timestamp", dateFormatted);

            commentSectionReference.child(foodItem.getRecipeId()).push().setValue(commentData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CommentsActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                            loadComments(); // Refresh the comments list after adding a new comment
                            binding.commentEditText.setText("");  // Clear the comment input field
                        } else {
                            Toast.makeText(CommentsActivity.this, "Failed to add comment.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        loadCommentCount();
    }
}