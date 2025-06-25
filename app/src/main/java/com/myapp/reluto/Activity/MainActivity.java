package com.myapp.reluto.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.reluto.Adapter.BestFoodsAdapter;
import com.myapp.reluto.Adapter.CategoryAdapter;
import com.myapp.reluto.Domain.Category;
import com.myapp.reluto.Domain.Foods;
import com.myapp.reluto.Domain.Location;
import com.myapp.reluto.R;
import com.myapp.reluto.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseFirestore firestore;
    private ImageView filterBtn;
    private ImageView settingBtn;
    private PopupWindow popupWindow;

    private Set<String> uniqueIngredients;

    // Variables for ingredient filtering
    private String[] ingredientsList = {
            "Soy Sauce", "Salt", "Vinegar", "Sugar", "Potato", "Bay Leaves", "Pepper", "Oil", "Water",
            "Garlic", "Onion", "Egg", "Ginger", "Tomato", "Fish Sauce", "Shrimp Paste", "Coconut Milk",
            "Chili Peppers", "Cabbage", "Butter", "Carrots", "Bell Peppers", "Mung Beans",
            "Rice", "Pork", "Chicken", "Beef", "Tofu", "Milk", "Cheese", "Flour", "Bread", "Cornstarch",
            "Ketchup", "Mayonnaise", "Hotdog", "Sardines", "Tuna", "Evaporated Milk", "Condensed Milk",
            "Banana", "Apple", "Calamansi", "Lemon", "Green Beans", "Eggplant", "Ampalaya", "Okra"
    };
    private boolean[] checkedIngredients;
    private ArrayList<String> selectedIngredients;
    private ArrayList<String> selectedLeftoverTypes; // Added
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String THEME_KEY = "isDarkMode";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        // Initialize filtering variables
        checkedIngredients = new boolean[ingredientsList.length];
        selectedIngredients = new ArrayList<>();
        selectedLeftoverTypes = new ArrayList<>(); // Initialize

        initLocation();
        initBestFood();
        initCategory();

        setVariable();
        displayUserFullName();

        // Initialize setting and filter buttons
        settingBtn = findViewById(R.id.settingBtn);
        filterBtn = findViewById(R.id.filterBtn);

        settingBtn.setOnClickListener(view -> showPopupWindow(view));
        filterBtn.setOnClickListener(view -> showLeftoverTypeDialog()); // Updated
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false);
        toggleTheme(isDarkMode);
    }
    private void toggleTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    private void showPopupWindow(View anchorView) {
        // Inflate the popup window layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_layout, null);

        // Create the PopupWindow
        popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Set background and focusable properties
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setFocusable(true);

        // Set onClick listeners for items in the popup
        TextView help = popupView.findViewById(R.id.popup_help);
        TextView contactUs = popupView.findViewById(R.id.popup_contact_us); // Contact Us
        TextView androidVersion = popupView.findViewById(R.id.popup_android_version_description);
        TextView theme = popupView.findViewById(R.id.popup_theme);
        TextView update = popupView.findViewById(R.id.popup_update);

        help.setOnClickListener(v -> {
            // Handle Help action
            popupWindow.dismiss();
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        contactUs.setOnClickListener(v -> {
            // Handle Contact Us action
            popupWindow.dismiss();
            Intent intent = new Intent(MainActivity.this, ContactUsActivity.class);
            startActivity(intent);
        });
        androidVersion.setOnClickListener(v -> {

            popupWindow.dismiss();
            Intent intent = new Intent(MainActivity.this, AndroidVersionActivity.class);
            startActivity(intent);
        });
        theme.setOnClickListener(v -> {

            popupWindow.dismiss();
            Intent intent = new Intent(MainActivity.this, ThemeActivity.class);
            startActivity(intent);
        });
        update.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
            startActivity(intent);
        });

        // Show the PopupWindow below the settings button
        popupWindow.showAsDropDown(anchorView, 0, 0);
    }



    private void setVariable() {
        binding.signoutBtn.setOnClickListener(view -> showSignOutConfirmationDialog());
        binding.searchBtn.setOnClickListener(view -> {
            String text = binding.searchEdit.getText().toString().trim();
            if (!text.isEmpty()) {
                String sanitizedSearchText = text.replace(",", " ");
                Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);
                intent.putExtra("text", sanitizedSearchText);
                intent.putExtra("isSearch", true);
                startActivity(intent);
            }
        });
    }

    private void showSignOutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Signout")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut(); // Sign out from Firebase
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void initBestFood() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Foods");
        binding.progressBarBest.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        Query query = myRef.orderByKey().limitToLast(10);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Foods.class));
                    }

                    // Reverse the list to show in ascending order
                    Collections.reverse(list);

                    if (!list.isEmpty()) {
                        binding.bestView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter adapter = new BestFoodsAdapter(list);
                        binding.bestView.setAdapter(adapter);
                    }
                }
                binding.progressBarBest.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarBest.setVisibility(View.GONE);
            }
        });
    }


    private void initCategory() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Category.class));
                    }
                    if (!list.isEmpty()) {
                        binding.CategoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
                        RecyclerView.Adapter adapter = new CategoryAdapter(list);
                        binding.CategoryView.setAdapter(adapter);
                    }
                }
                binding.progressBarCategory.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarCategory.setVisibility(View.GONE);
            }
        });
    }

    private void initLocation() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Location");
        ArrayList<Location> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Location.class));
                    }
                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.locationSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayUserFullName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            if (fullName != null) {
                                TextView nameLoginTxt = findViewById(R.id.namelogintxt);
                                nameLoginTxt.setText("Hello, " + fullName);
                            }
                        }
                    }
                });
    }

    private void showLeftoverTypeDialog() {
        selectedIngredients.clear();
        selectedLeftoverTypes.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What is your leftover type?");

        String[] leftoverTypes = {"Rice", "Pork", "Chicken", "Fish", "Soup", "Fried", "Vegetable", "Bread", "Canned", "Others"};
        final int[] selectedIndex = {-1};

        // Create layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 0);

        // Create input box for "Others"
        final EditText otherInput = new EditText(this);
        otherInput.setHint("Type your leftover");
        otherInput.setVisibility(View.GONE);
        layout.addView(otherInput);

        builder.setSingleChoiceItems(leftoverTypes, -1, (dialog, which) -> {
            selectedIndex[0] = which;
            if (leftoverTypes[which].equals("Others")) {
                otherInput.setVisibility(View.VISIBLE);
            } else {
                otherInput.setVisibility(View.GONE);
            }
        });

        builder.setView(layout);

        builder.setPositiveButton("Next", (dialog, which) -> {
            if (selectedIndex[0] == -1) {
                Toast.makeText(this, "Please select a leftover type", Toast.LENGTH_SHORT).show();
                return;
            }

            String selected = leftoverTypes[selectedIndex[0]];
            if (selected.equals("Others")) {
                String input = otherInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(this, "Please type your leftover", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedLeftoverTypes.add(input);
            } else {
                selectedLeftoverTypes.add(selected);
            }

            showIngredientsDialog();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }



    private void showIngredientsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Available on-hand Ingredients ");
        Toast.makeText(MainActivity.this, "Please select at least two ingredients.", Toast.LENGTH_LONG).show();

        builder.setMultiChoiceItems(ingredientsList, checkedIngredients, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedIngredients.add(ingredientsList[which]);
            } else {
                selectedIngredients.remove(ingredientsList[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (selectedIngredients.size() >= 2) {
                fetchRecipeBasedOnCookedTypeAndIngredients(); // Filter recipes based on current selections
            } else {
                Toast.makeText(MainActivity.this, "Please select at least two ingredients.", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void fetchRecipeBasedOnCookedTypeAndIngredients() {
        if (selectedIngredients.size() < 2) {
            Toast.makeText(MainActivity.this, "You must select more than 1 ingredient.", Toast.LENGTH_LONG).show();
            resetIngredientsSelection(); // Reset the selected and checked ingredients
            return;
        }

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Foods");
        Query query = myRef.orderByKey();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> filteredFoodIds = new ArrayList<>();
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    Foods food = foodSnapshot.getValue(Foods.class);
                    if (food != null) {
                        String ingredients = food.getIngredients().toLowerCase();
                        String name = food.getTitle().toLowerCase();

                        // Count the number of selected ingredients present in the food's ingredients
                        long matchingIngredientsCount = selectedIngredients.stream()
                                .map(String::toLowerCase)
                                .filter(selectedIngredient -> ingredients.contains(selectedIngredient))
                                .count();

                        // Check if at least 2 ingredients match
                        boolean ingredientsMatch = matchingIngredientsCount >= 2;

                        // Check if all of the selected ingredients are present in the food's ingredients
                        boolean ingredientsMatchAll = selectedIngredients.stream()
                                .map(String::toLowerCase)
                                .anyMatch(selectedIngredient -> ingredients.contains(selectedIngredient));

                        // Check if the title or description contains any of the selected leftover types
                        boolean leftoverTypeMatches = selectedLeftoverTypes.stream()
                                .map(String::toLowerCase)
                                .anyMatch(type -> name.contains(type));

                        // Check if the ingredients contain any of the selected leftover types
                        boolean leftoverTypeMatchIngredients = selectedLeftoverTypes.stream()
                                .map(String::toLowerCase)
                                .anyMatch(selectedIngredient -> ingredients.contains(selectedIngredient));

                        // Apply conditions
                        if (ingredientsMatch && ingredientsMatchAll && leftoverTypeMatches) {
                            filteredFoodIds.add(foodSnapshot.getKey());
                        } else if (ingredientsMatch && leftoverTypeMatchIngredients && ingredientsMatchAll) {
                            filteredFoodIds.add(foodSnapshot.getKey()); // Store the food ID
                        }
                    }
                }

                Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);

                if (!filteredFoodIds.isEmpty()) {
                    intent.putStringArrayListExtra("filteredFoodIds", filteredFoodIds);
                    intent.putStringArrayListExtra("Ingredients", new ArrayList<>(selectedIngredients));
                } else {
                    intent.putExtra("text", "No Result Found");
                    intent.putExtra("isSearch", true);
                }

                startActivity(intent);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load recipes.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetIngredientsSelection() {
        selectedIngredients.clear();
        Arrays.fill(checkedIngredients, false); // Reset all checkboxes to false
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Call the method to reload the data and ratings
        initBestFood();
    }
}
