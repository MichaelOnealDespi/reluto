package com.myapp.reluto.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.myapp.reluto.Domain.Foods;
import com.myapp.reluto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodtechActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private StorageReference storageReference;
    private EditText titleEdt, descriptionEdt, ingredientsEdt, procedureEdt, commentsEdt, equipmentsEdt, timerEdt, minsEdt, secsEdt, anotherEdt, notesaEdt;
    private ImageView imageView;
    private Spinner categorySpinner;
    private Button uploadImageBtn, saveRecipeBtn, backBtn;
    private TextView selectedCategoryTextView;
    private List<Map<String, String>> selectedCategoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodtech);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        titleEdt = findViewById(R.id.titleEdt);
        timerEdt = findViewById(R.id.timerEdt);
        minsEdt = findViewById(R.id.minsEdt);
        secsEdt = findViewById(R.id.secsEdt);
        descriptionEdt = findViewById(R.id.descriptionEdt);
        ingredientsEdt = findViewById(R.id.ingredientsEdt);
        procedureEdt = findViewById(R.id.procedureEdt);
        commentsEdt = findViewById(R.id.commentsEdt);
        equipmentsEdt = findViewById(R.id.equipmentEdt);
        anotherEdt = findViewById(R.id.anotherEdt);
        notesaEdt = findViewById(R.id.notesaEdt); // new field
        imageView = findViewById(R.id.imageView);
        categorySpinner = findViewById(R.id.categorySpinner);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        saveRecipeBtn = findViewById(R.id.saveRecipeBtn);
        backBtn = findViewById(R.id.backBtn);
        selectedCategoryTextView = findViewById(R.id.selectedCategory);

        setCapitalizationWatcher(titleEdt, true);
        setCapitalizationWatcher(descriptionEdt, true);
        setCapitalizationWatcher(ingredientsEdt, false);
        setCapitalizationWatcher(procedureEdt, false);
        setCapitalizationWatcher(equipmentsEdt, false);
        setCapitalizationWatcher(commentsEdt, false);
        setCapitalizationWatcher(anotherEdt, false);
        setCapitalizationWatcher(notesaEdt, false); // watcher for new field

        selectedCategoriesList = new ArrayList<>();
        setupCategorySpinner();

        uploadImageBtn.setOnClickListener(view -> openFileChooser());
        saveRecipeBtn.setOnClickListener(view -> saveRecipe());
        backBtn.setOnClickListener(view -> navigateBack());

        // ✅ Upload "Breakfast" category ONCE with key "0"
      //  DatabaseReference categoryRef = database.getReference("Category");
      //  Map<String, Object> breakfastCategory = new HashMap<>();
      //  breakfastCategory.put("Id", 9);
      //  breakfastCategory.put("ImagePath", "btn_1");
      //  breakfastCategory.put("Name", "Soup");

// Set the key explicitly to "0"
     //   categoryRef.child("8").setValue(breakfastCategory);

     //   uploadImageBtn.setOnClickListener(view -> openFileChooser());
      //  saveRecipeBtn.setOnClickListener(view -> saveRecipe());
       // backBtn.setOnClickListener(view -> finish());

       // setupCategorySpinner();

    }

    private void setCapitalizationWatcher(EditText editText, boolean isTitleOrDescription) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this);
                String text = s.toString();
                String[] lines = text.split("\n");
                StringBuilder formattedText = new StringBuilder();
                int cursorPosition = editText.getSelectionStart();

                for (String line : lines) {
                    if (line.isEmpty()) {
                        formattedText.append("\n");
                        continue;
                    }
                    boolean isBulletLine = line.startsWith("•");
                    String capitalizedLine;
                    if (isTitleOrDescription) {
                        capitalizedLine = Character.toUpperCase(line.charAt(0)) + line.substring(1);
                    } else {
                        if (!isBulletLine) {
                            capitalizedLine = "• " + Character.toUpperCase(line.charAt(0)) + line.substring(1);
                        } else {
                            if (line.length() > 2 && Character.isLowerCase(line.charAt(2))) {
                                capitalizedLine = "• " + Character.toUpperCase(line.charAt(2)) + line.substring(3);
                            } else {
                                capitalizedLine = line;
                            }
                        }
                    }
                    formattedText.append(capitalizedLine).append("\n");
                }
                if (formattedText.length() > 0 && formattedText.charAt(formattedText.length() - 1) == '\n') {
                    formattedText.setLength(formattedText.length() - 1);
                }
                if (!formattedText.toString().equals(text)) {
                    editText.setText(formattedText.toString());
                    int bulletPosition = formattedText.indexOf("•", cursorPosition > 0 ? cursorPosition - 3 : 0);
                    if (bulletPosition != -1) {
                        int positionAfterBullet = bulletPosition + 3;
                        editText.setSelection(Math.min(positionAfterBullet, formattedText.length()));
                    } else {
                        editText.setSelection(Math.min(cursorPosition, formattedText.length()));
                    }
                } else {
                    editText.setSelection(Math.min(cursorPosition, formattedText.length()));
                }

                if (s.length() > 0 && s.charAt(s.length() - 1) == '\n') {
                    editText.setText(s.toString() + "• ");
                    editText.setSelection(editText.getText().length());
                }

                String[] updatedLines = editText.getText().toString().split("\n");
                if (updatedLines.length > 0) {
                    String lastLine = updatedLines[updatedLines.length - 1].trim();
                    if (lastLine.equals("•") || lastLine.isEmpty()) {
                        StringBuilder newText = new StringBuilder();
                        for (int i = 0; i < updatedLines.length - 1; i++) {
                            newText.append(updatedLines[i]).append("\n");
                        }
                        editText.setText(newText.toString());
                        editText.setSelection(editText.getText().length());
                    }
                }

                editText.addTextChangedListener(this);
            }
        });
    }

    private void saveRecipe() {
        String title = titleEdt.getText().toString();
        String hours = timerEdt.getText().toString().trim();
        String minutes = minsEdt.getText().toString().trim();
        String seconds = secsEdt.getText().toString().trim();
        String timer = String.format("%s%s%s",
                hours.isEmpty() || hours.equals("0") ? "" : hours + (Integer.parseInt(hours) > 1 ? " Hours " : " Hour "),
                minutes.isEmpty() || minutes.equals("0") ? "" : minutes + (Integer.parseInt(minutes) > 1 ? " Minutes " : " Minute "),
                seconds.isEmpty() || seconds.equals("0") ? "" : seconds + (Integer.parseInt(seconds) > 1 ? " Seconds" : " Second"));
        String description = descriptionEdt.getText().toString();
        String ingredients = ingredientsEdt.getText().toString();
        String procedure = procedureEdt.getText().toString();
        String equipment = equipmentsEdt.getText().toString();
        String comments = commentsEdt.getText().toString();
        String another = anotherEdt.getText().toString();
        String notesa = notesaEdt.getText().toString();

        if (title.isEmpty() || timer.isEmpty() || description.isEmpty() || ingredients.isEmpty() || imageUri == null || procedure.isEmpty() || equipment.isEmpty() || comments.isEmpty() || another.isEmpty() || notesa.isEmpty() || selectedCategoriesList.isEmpty()) {
            Toast.makeText(FoodtechActivity.this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase validation then:
        showConfirmationDialog(title, description, ingredients, procedure, equipment, comments, another, notesa, timer);
    }

    private void showConfirmationDialog(String title, String description, String ingredients, String procedure, String equipment, String comments, String another, String notesa, String timer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Save Recipe");
        builder.setMessage("Do you want to save this recipe?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            proceedToSaveRecipe(title, description, ingredients, procedure, equipment, comments, another, notesa, timer);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void proceedToSaveRecipe(String title, String description, String ingredients, String procedure, String equipment, String comments, String another, String notesa, String timer) {
        final DatabaseReference recipeRef = database.getReference("Foods").push();
        final String recipeKey = recipeRef.getKey();
        final StorageReference fileReference = storageReference.child("images/" + recipeKey);
        UploadTask uploadTask = fileReference.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
            String imagePath = uri.toString();
            String userId = auth.getCurrentUser().getUid();
            Map<String, String> selectedCategory = selectedCategoriesList.get(0);
            int categoryId = Integer.parseInt(selectedCategory.get("CategoryId"));

            String categoryNames = "";
            for (Map<String, String> category : selectedCategoriesList) {
                if (!categoryNames.isEmpty()) {
                    categoryNames += ", ";
                }
                categoryNames += category.get("CategoryName");
            }

            Foods food = new Foods(recipeKey, title, description, ingredients, imagePath, 4.5, categoryId, categoryNames, 1, 10.99, 1, timer, 1, 1, userId, procedure, equipment, comments, another, notesa, 0, new HashMap<>());

            Map<String, Object> foodMap = new HashMap<>();
            foodMap.put("Title", food.getTitle());
            foodMap.put("Description", food.getDescription());
            foodMap.put("Ingredients", food.getIngredients());
            foodMap.put("ImagePath", food.getImagePath());
            foodMap.put("Star", food.getStar());
            foodMap.put("CategoryId", food.getCategoryId());
            foodMap.put("CategoryName", categoryNames);
            foodMap.put("LocationId", food.getLocationId());
            foodMap.put("Price", food.getPrice());
            foodMap.put("PriceId", food.getPriceId());
            foodMap.put("TimerId", food.getTimerId());
            foodMap.put("TypeId", food.getTypeId());
            foodMap.put("Count", food.getCount());
            foodMap.put("UserId", food.getUserId());
            foodMap.put("Procedure", food.getProcedure());
            foodMap.put("Equipment", food.getEquipment());
            foodMap.put("Comments", food.getComments());
            foodMap.put("Another", food.getAnother());
            foodMap.put("Notes", food.getNotes());
            foodMap.put("Likes", food.getLikes());
            foodMap.put("RecipeId", food.getRecipeId());

            recipeRef.setValue(foodMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(FoodtechActivity.this, "Recipe saved successfully", Toast.LENGTH_SHORT).show();
                    navigateToDetailActivity(recipeKey);
                } else {
                    Toast.makeText(FoodtechActivity.this, "Failed to save recipe", Toast.LENGTH_SHORT).show();
                }
            });
        }));
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Breakfast", "Lunch", "Dinner", "Dessert", "Appetizer",
                "Snacks", "Side Dish", "Vegetarian", "Soup", "Vegan", "Beverages", "Salad", "Others"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Category"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showMultiSelectDialog(categories);
                return true;
            }
            return false;
        });
    }

    private void showMultiSelectDialog(String[] categories) {
        boolean[] checkedItems = new boolean[categories.length];

        for (int i = 0; i < categories.length; i++) {
            for (Map<String, String> category : selectedCategoriesList) {
                if (category.get("CategoryName").equals(categories[i])) {
                    checkedItems[i] = true;
                    break;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Categories")
                .setMultiChoiceItems(categories, checkedItems, (dialog, which, isChecked) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    ListView listView = alertDialog.getListView();
                    String selectedCategory = categories[which];

                    if (selectedCategory.equals("Others")) {
                        if (isChecked) {
                            for (int i = 0; i < categories.length; i++) {
                                if (!categories[i].equals("Others")) {
                                    listView.setItemChecked(i, false);
                                    checkedItems[i] = false;
                                }
                            }
                            selectedCategoriesList.clear();
                            selectedCategoriesList.add(createCategoryMap(which, selectedCategory));
                        } else {
                            selectedCategoriesList.removeIf(cat -> cat.get("CategoryName").equals("Others"));
                        }
                    } else {
                        int othersIndex = Arrays.asList(categories).indexOf("Others");
                        listView.setItemChecked(othersIndex, false);
                        checkedItems[othersIndex] = false;
                        selectedCategoriesList.removeIf(cat -> cat.get("CategoryName").equals("Others"));

                        if (isChecked) {
                            Map<String, String> category = createCategoryMap(which, selectedCategory);
                            if (!selectedCategoriesList.stream().anyMatch(cat -> cat.get("CategoryId").equals(String.valueOf(which + 1)))) {
                                selectedCategoriesList.add(category);
                            }
                        } else {
                            selectedCategoriesList.removeIf(cat -> cat.get("CategoryId").equals(String.valueOf(which + 1)));
                        }
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> updateSelectedCategoriesText())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private Map<String, String> createCategoryMap(int index, String name) {
        Map<String, String> category = new HashMap<>();
        category.put("CategoryId", String.valueOf(index + 1));
        category.put("CategoryName", name);
        return category;
    }

    private void updateSelectedCategoriesText() {
        StringBuilder selectedCategories = new StringBuilder("Selected Category:\n");
        for (Map<String, String> category : selectedCategoriesList) {
            selectedCategories.append("• ").append(category.get("CategoryName")).append("\n");
        }
        selectedCategoryTextView.setText(selectedCategories.toString().trim());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void navigateToDetailActivity(String recipeKey) {
        Intent intent = new Intent(FoodtechActivity.this, DetailActivity.class);
        intent.putExtra("RecipeKey", recipeKey);
        startActivity(intent);
    }

    private void navigateBack() {
        finish();
    }
}
