package com.myapp.reluto.Activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myapp.reluto.Domain.Foods;
import com.myapp.reluto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class FoodtechProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView createdView;
    private Button createRecipeBtn;
    private ImageView logoutBtn;
    private ImageView settingBtn;
    private PopupWindow popupWindow;
    private Button editProfileBtn;
    private TextView namesEdt, emailsEdt, contactsEdt, linksEdt, socialEdt, totalLikesTxt, textviewname, bdayedt;
    private TextView lnamesEdt, urselfid;
    private ImageView profileImageView;
    private ArrayList<Foods> recipesList;
    private CreatedRecipesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodtech_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views and other components
        editProfileBtn = findViewById(R.id.editProfileBtn);
        createRecipeBtn = findViewById(R.id.createrecipeBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        createdView = findViewById(R.id.createdView);
        settingBtn = findViewById(R.id.settingBtn);
        namesEdt = findViewById(R.id.namesEdt);
        emailsEdt = findViewById(R.id.emailsEdt);
        contactsEdt = findViewById(R.id.contactsEdt);
        linksEdt = findViewById(R.id.linksEdt);
        socialEdt = findViewById(R.id.socialEdt);
        lnamesEdt = findViewById(R.id.lnamesEdt);
        urselfid = findViewById(R.id.urselfid);

        profileImageView = findViewById(R.id.profile_pictures);
        totalLikesTxt = findViewById(R.id.totallikestxt);

        // Initialize the back button ImageView
        ImageView backToDetail = findViewById(R.id.backtodetail); // Assuming you have this ID in your layout

        // Set an OnClickListener to go back to the previous activity
        backToDetail.setOnClickListener(view -> {
            // This will take you back to the previous activity in the stack
            onBackPressed();
        });

        // Set up RecyclerView
        recipesList = new ArrayList<>();
        adapter = new CreatedRecipesAdapter(recipesList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        createdView.setLayoutManager(layoutManager);
        createdView.setAdapter(adapter);

        // Get the userId passed from the DetailActivity (or the logged-in user if no userId is passed)
        String userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            // If userId is passed, load that user's profile and recipes
            hideProfileButtons();
            loadUserProfile(userId);
            loadUserRecipes(userId);
        } else {
            // If no userId is passed, load the profile and recipes of the currently logged-in user
            backToDetail.setVisibility(View.INVISIBLE);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                loadUserProfile(user.getUid());
                loadUserRecipes(user.getUid());
            }
        }

        // Set OnClickListener for buttons
        editProfileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(FoodtechProfileActivity.this, profilefoodtechedit.class);
            startActivity(intent);
        });

        createRecipeBtn.setOnClickListener(view -> {
            Intent intent = new Intent(FoodtechProfileActivity.this, FoodtechActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(view -> showLogoutConfirmationDialog());

        settingBtn.setOnClickListener(view -> showPopupWindow(view));
    }

    private void hideProfileButtons() {
        // Hide the buttons (Set visibility to GONE or INVISIBLE)
        createRecipeBtn.setVisibility(View.GONE);
        editProfileBtn.setVisibility(View.GONE);
        logoutBtn.setVisibility(View.GONE);
        settingBtn.setVisibility(View.GONE);
    }

    private void showProfileButtons() {
        // Show the buttons (Set visibility to VISIBLE)
        createRecipeBtn.setVisibility(View.VISIBLE);
        editProfileBtn.setVisibility(View.VISIBLE);
        logoutBtn.setVisibility(View.VISIBLE);
        settingBtn.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Get the userId passed from the previous activity or the logged-in user
        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                userId = user.getUid();
            }
        }

        // Reload the user recipes to reflect updated ratings
        if (userId != null) {
            loadUserRecipes(userId);
        }
    }




    private void showPopupWindow(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_layout, null);

        popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setFocusable(true);

        TextView help = popupView.findViewById(R.id.popup_help);
        TextView contactUs = popupView.findViewById(R.id.popup_contact_us); // Contact Us
        TextView androidVersion = popupView.findViewById(R.id.popup_android_version_description);
        TextView theme = popupView.findViewById(R.id.popup_theme);
        TextView update = popupView.findViewById(R.id.popup_update);

        help.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(FoodtechProfileActivity.this, HelpActivity.class);
            startActivity(intent);
        });
        contactUs.setOnClickListener(v -> {
            // Handle Contact Us action
            popupWindow.dismiss();
            Intent intent = new Intent(FoodtechProfileActivity.this, ContactUsActivity.class);
            startActivity(intent);
        });
        androidVersion.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(FoodtechProfileActivity.this, AndroidVersionActivity.class);
            startActivity(intent);
        });
        theme.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(FoodtechProfileActivity.this, ThemeActivity.class);
            startActivity(intent);
        });
        update.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(FoodtechProfileActivity.this, UpdateActivity.class);
            startActivity(intent);
        });

        popupWindow.showAsDropDown(anchorView, 0, 0);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Signout")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(FoodtechProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadUserRecipes(String userId) {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("Foods");
        DatabaseReference userLikesReference = FirebaseDatabase.getInstance().getReference("UserLikes");

        recipesRef.orderByChild("UserId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipesList.clear();
                final float[] totalAverage = {0}; // To hold the sum of all recipe averages
                final int[] recipeCount = {0};   // To count how many recipes have ratings
                final int[] processedRecipes = {0}; // To track processed recipes

                // Iterate through all recipes
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Foods food = snapshot.getValue(Foods.class);
                    if (food != null) {
                        recipesList.add(food);
                        String recipeId = food.getRecipeId(); // Get the recipeId

                        // Now, get the ratings for this recipe
                        userLikesReference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot ratingSnapshot) {
                                int totalLikes = 0;
                                int userCount = 0; // To count the valid ratings

                                // Iterate over all the children in this recipe's rating data
                                for (DataSnapshot rating : ratingSnapshot.getChildren()) {
                                    // Skip the timestamp node if present
                                    if (rating.getKey().equals("timestamp")) {
                                        continue;
                                    }

                                    // Check for the rating key and get the value
                                    Integer ratingValue = rating.child("rating").getValue(Integer.class);
                                    if (ratingValue != null) {
                                        totalLikes += Math.min(ratingValue, 5);  // Cap the rating at 5
                                        userCount++;  // Only count valid ratings
                                    }
                                }

                                // Calculate the average rating for this recipe
                                if (userCount > 0) {
                                    float averageLikes = (float) totalLikes / userCount;
                                    totalAverage[0] += averageLikes;
                                    recipeCount[0]++;
                                }

                                // Increment the processed recipe count
                                processedRecipes[0]++;

                                // Once all recipes are processed, update the total average rating
                                if (processedRecipes[0] == dataSnapshot.getChildrenCount()) {
                                    float overallAverage = recipeCount[0] > 0 ? totalAverage[0] / recipeCount[0] : 0;
                                    totalLikesTxt.setText(String.format("Total Rating: %.2f", overallAverage));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle any errors
                            }
                        });
                    }
                }

                // Reverse the list to show in ascending order (if needed)
                Collections.reverse(recipesList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void loadUserProfile(String userId) {
        db.collection("foodtech").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("fullName");
                    String email = document.getString("email");
                    String contact = document.getString("contact");
                    String link = document.getString("link");
                    String social = document.getString("social");
                    String lastName = document.getString("lastName");
                    String description = document.getString("description");
                    String profileImageUrl = document.getString("photoUrl");

                    namesEdt.setText(name);
                    emailsEdt.setText(email);
                    contactsEdt.setText(contact);
                    linksEdt.setText(link);
                    socialEdt.setText(social);
                    lnamesEdt.setText(lastName);
                    urselfid.setText(description);

                    Glide.with(FoodtechProfileActivity.this)
                            .load(profileImageUrl)
                            .into(profileImageView);
                }
            }
        });
    }

    private class CreatedRecipesAdapter extends RecyclerView.Adapter<CreatedRecipesAdapter.ViewHolder> {

        private final ArrayList<Foods> recipesList;
        private final DatabaseReference userLikesReference;

        public CreatedRecipesAdapter(ArrayList<Foods> recipesList) {
            this.recipesList = recipesList;
            userLikesReference = FirebaseDatabase.getInstance().getReference("UserLikes");
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_created_recipes, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Foods food = recipesList.get(position);

            holder.titleTextView.setText(food.getTitle());

            // Load user likes for this food item
            loadUserLikes(food.getRecipeId(), holder.likeCountsTxt, holder.barrating);

            // Set the timer
            String timer = food.getTimerId();
            holder.timerTxt.setText("Cooking Time: " + (timer != null ? timer : "N/A"));

            Glide.with(holder.itemView.getContext())
                    .load(food.getImagePath())
                    .into(holder.picImageView);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(FoodtechProfileActivity.this, DetailActivity.class);
                intent.putExtra("object", food);
                startActivity(intent);
            });
        }

        private void loadUserLikes(String recipeId, TextView likeCountsTxt, RatingBar ratingBar) {
            userLikesReference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalLikes = 0;
                    int userCount = 0;

                    // Iterate over all children in the snapshot
                    for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                        // Check if the value is actually a rating, and not a timestamp or other data.
                        if (ratingSnapshot.child("rating").exists()) {
                            Integer rating = ratingSnapshot.child("rating").getValue(Integer.class);
                            if (rating != null) {
                                totalLikes += rating;  // Sum up the ratings
                                userCount++;  // Increment user count
                            }
                        }
                    }

                    // Calculate the average rating
                    float averageLikes = userCount > 0 ? (float) totalLikes / userCount : 0;
                    String formattedLikes = String.format("%.2f", averageLikes);
                    likeCountsTxt.setText(formattedLikes);  // Display the average rating in the TextView

                    // Set the RatingBar to the calculated average rating
                    ratingBar.setRating(averageLikes);  // Set the RatingBar to the average rating
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle any errors that occur during the Firebase read operation
                    Log.e("Firebase", "Error loading user likes: " + error.getMessage());
                }
            });
        }

        @Override
        public int getItemCount() {
            return recipesList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView titleTextView;
            public TextView likeCountsTxt;
            public TextView timerTxt;
            public ImageView picImageView;
            public RatingBar barrating; // RatingBar for displaying the average rating

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTxt);
                likeCountsTxt = itemView.findViewById(R.id.LikeCountsTxt);
                timerTxt = itemView.findViewById(R.id.asdadasda);
                picImageView = itemView.findViewById(R.id.pic);
                barrating = itemView.findViewById(R.id.likebtn); // Initialize the RatingBar
            }
        }
    }
}