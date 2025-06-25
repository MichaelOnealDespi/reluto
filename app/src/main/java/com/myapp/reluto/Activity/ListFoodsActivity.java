package com.myapp.reluto.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.reluto.Adapter.FoodListAdapter;
import com.myapp.reluto.Domain.Foods;
import com.myapp.reluto.R;
import com.myapp.reluto.databinding.ActivityListFoodsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListFoodsActivity extends BaseActivity {
    ActivityListFoodsBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private int categoryId;
    private String searchText;
    private boolean isSearch;
    private String categoryName;
    private List<String> selectedIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();

        // Only load list if not Others
        if (!"Others".equalsIgnoreCase(categoryName)) {
            initList();
        }

        setVariable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only load list if not Others
        if (!"Others".equalsIgnoreCase(categoryName)) {
            initList();
        }
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);
        selectedIngredients = getIntent().getStringArrayListExtra("Ingredients");

        // Capitalize the first letter if categoryName is not null
        if (categoryName != null && !categoryName.isEmpty()) {
            String capitalized = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1);
            binding.titleTxt.setText(capitalized);
        } else {
            binding.titleTxt.setText("Food List");
        }

        binding.backBtn.setOnClickListener(view -> finish());
    }

    private void initList() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);

        ArrayList<String> filteredFoodIds = getIntent().getStringArrayListExtra("filteredFoodIds");
        Set<String> uniqueFoodKeys = new HashSet<>();
        ArrayList<Foods> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);
                        String foodKey = issue.getKey();

                        if (food != null) {
                            boolean isInFilteredIds = filteredFoodIds == null || filteredFoodIds.contains(foodKey);
                            boolean isUnique = !uniqueFoodKeys.contains(foodKey);

                            if (isInFilteredIds && isUnique) {
                                uniqueFoodKeys.add(foodKey);

                                boolean matchesCategory = matchesMultipleCategories(food);
                                boolean matchesIngredients = matchesIngredients(food);
                                boolean matchesSearch = true;

                                if (isSearch && searchText != null && !searchText.isEmpty()) {
                                    String[] searchTerms = searchText.split("\\s+");
                                    String title = food.getTitle();
                                    String ingredients = food.getIngredients();
                                    matchesSearch = (title != null && containsAllTerms(title, searchTerms)) ||
                                            (ingredients != null && containsAllTerms(ingredients, searchTerms));
                                }

                                if (matchesCategory && matchesIngredients && matchesSearch) {
                                    list.add(food);
                                }
                            }
                        }
                    }
                    updateRecyclerView(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean matchesMultipleCategories(Foods food) {
        if (categoryId != 0 || (categoryName != null && !categoryName.isEmpty())) {
            List<String> foodCategories = new ArrayList<>();
            if (food.getCategoryName() != null) {
                String[] categoriesArray = food.getCategoryName().split(",\\s*");
                foodCategories.addAll(Arrays.asList(categoriesArray));
            }

            boolean matchesCategoryId = categoryId != 0 && food.getCategoryId() == categoryId;
            boolean matchesCategoryName = foodCategories.contains(categoryName);

            return matchesCategoryId || matchesCategoryName;
        }
        return true;
    }

    private boolean containsAllTerms(String text, String[] searchTerms) {
        for (String term : searchTerms) {
            if (!text.toLowerCase().contains(term.trim().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesIngredients(Foods food) {
        if (selectedIngredients == null || selectedIngredients.isEmpty()) {
            return true;
        }

        String foodIngredients = food.getIngredients().toLowerCase();

        for (String selectedIngredient : selectedIngredients) {
            String lowerCaseSelectedIngredient = selectedIngredient.toLowerCase();
            if (foodIngredients.contains(lowerCaseSelectedIngredient)) {
                return true;
            }
        }

        return false;
    }

    private void setVariable() {
        if ("Others".equalsIgnoreCase(categoryName)) {
            showCategorySelectionDialog();
        }
    }

    private void updateRecyclerView(ArrayList<Foods> list) {
        if (list.size() > 0) {
            binding.foodListView.setVisibility(View.VISIBLE);
            binding.noResultTxt.setVisibility(View.GONE);
            binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this, 2));
            adapterListFood = new FoodListAdapter(list);
            binding.foodListView.setAdapter(adapterListFood);
        } else {
            binding.foodListView.setVisibility(View.GONE);
            binding.noResultTxt.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    }

    private void showCategorySelectionDialog() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Foods");
        Set<String> uniqueCategories = new HashSet<>();

        // Categories to exclude
        List<String> excludedCategories = Arrays.asList(
                "Breakfast", "Dinner", "Snacks", "Appetizer", "Side Dish", "Dessert", "Lunch"
        );

        // Categories to always include
        List<String> alwaysIncludeCategories = Arrays.asList("Soup", "Vegetarian", "Vegan", "Beverages", "Salad", "Others");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot foodSnap : snapshot.getChildren()) {
                    Foods food = foodSnap.getValue(Foods.class);
                    if (food != null && food.getCategoryName() != null) {
                        String[] categories = food.getCategoryName().split(",\\s*");
                        for (String category : categories) {
                            if (!excludedCategories.contains(category)) {
                                uniqueCategories.add(category);
                            }
                        }
                    }
                }

                // Add permanently visible categories
                for (String alwaysInclude : alwaysIncludeCategories) {
                    if (!excludedCategories.contains(alwaysInclude)) {
                        uniqueCategories.add(alwaysInclude);
                    }
                }

                List<String> categoryList = new ArrayList<>(uniqueCategories);
                categoryList.remove("Others"); // remove 'Others' before sorting
                categoryList.sort(String::compareTo); // sort alphabetically
                categoryList.add("Others"); // add 'Others' at the end

                String[] categoryArray = categoryList.toArray(new String[0]);

                new android.app.AlertDialog.Builder(ListFoodsActivity.this)
                        .setTitle("Select Other Category")
                        .setItems(categoryArray, (dialog, which) -> {
                            categoryName = categoryArray[which];

                            // Capitalize and update title
                            String capitalized = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1);
                            binding.titleTxt.setText(capitalized);

                            initList();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            finish(); // Return to MainActivity if user cancels
                        })
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optionally handle error
            }
        });
    }

}
