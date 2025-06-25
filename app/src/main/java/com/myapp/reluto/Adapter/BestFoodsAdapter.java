package com.myapp.reluto.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.myapp.reluto.Activity.DetailActivity;
import com.myapp.reluto.Domain.Foods;
import com.myapp.reluto.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class BestFoodsAdapter extends RecyclerView.Adapter<BestFoodsAdapter.ViewHolder> {
    ArrayList<Foods> items;
    Context context;
    FirebaseFirestore firestore;
    DatabaseReference userLikesReference;

    public BestFoodsAdapter(ArrayList<Foods> items) {
        this.items = items;
        firestore = FirebaseFirestore.getInstance();
        userLikesReference = FirebaseDatabase.getInstance().getReference("UserLikes");
    }

    @NonNull
    @Override
    public BestFoodsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_best_deal, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BestFoodsAdapter.ViewHolder holder, int position) {
        Foods food = items.get(position);
        holder.titleTxt.setText(food.getTitle());

        Glide.with(context)
                .load(food.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        // Load user likes and set rating
        loadUserLikes(food.getRecipeId(), holder.likeCountsTxt, holder.ratingBar);

        // Set the timer
        String timer = food.getTimerId();
        holder.timerTxt.setText("Cooking Time: " + (timer != null ? timer : "N/A"));

        // Fetch and display the uploader's full name
        displayUserFullName(food.getUserId(), holder.whoTxt);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", food);
            context.startActivity(intent);
        });
    }

    private void loadUserLikes(String recipeId, TextView likeCountsTxt, RatingBar ratingBar) {
        userLikesReference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalLikes = 0;
                int userCount = 0;

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

                // Calculate average rating
                float averageLikes = userCount > 0 ? (float) totalLikes / userCount : 0;
                String formattedLikes = String.format("%.2f", averageLikes);
                likeCountsTxt.setText(formattedLikes);  // Display the average rating

                // Set the RatingBar to the calculated average rating
                ratingBar.setRating(averageLikes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur during the Firebase read operation
                Log.e("Firebase", "Error loading user likes: " + error.getMessage());
            }
        });
    }



    private void displayUserFullName(String userId, TextView nameTextView) {
        firestore.collection("foodtech").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            String lastName = document.getString("lastName");
                            nameTextView.setText("By: " + (fullName != null ? fullName : "Unknown") + " " + (lastName != null ? lastName : ""));
                        } else {
                            nameTextView.setText("By: Unknown");
                        }
                    } else {
                        nameTextView.setText("By: Unknown");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        TextView likeCountsTxt; // To display the average rating and like count
        TextView timerTxt;
        ImageView pic;
        TextView whoTxt; // TextView for displaying uploader's name
        RatingBar ratingBar; // RatingBar for displaying the average rating

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            likeCountsTxt = itemView.findViewById(R.id.likeCountsTxt);
            timerTxt = itemView.findViewById(R.id.asdadasda); // Make sure this ID is correct
            whoTxt = itemView.findViewById(R.id.whoTxt);
            pic = itemView.findViewById(R.id.pic);
            ratingBar = itemView.findViewById(R.id.ratingBar); // Initialize the RatingBar
        }
    }
}
