package com.myapp.reluto.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

public class EditRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private StorageReference storageReference;
    private String originalTitle, originalDescription, originalIngredients, originalProcedure;

    private TextView CategorySelectedText;
    private EditText titleEdt, descriptionEdt, ingredientsEdt, procedureEdt, commentsEdt, anotherEdt, notesaEdt, equipmentEdt, timerEdt, minsEdt, secsEdt;
    private Spinner categorySpinner;
    private ImageView imageView;
    private Button saveRecipeBtn, deleteRecipeBtn, backBtn, uploadImageBtn;

    private String recipeId;
    private List<Map<String, String>> selectedCategoriesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("images");

        titleEdt = findViewById(R.id.titleEdt);
        timerEdt = findViewById(R.id.timerEdt);
        minsEdt = findViewById(R.id.minsEdt);
        secsEdt = findViewById(R.id.secsEdt);
        descriptionEdt = findViewById(R.id.descriptionEdt);
        ingredientsEdt = findViewById(R.id.ingredientsEdt);
        procedureEdt = findViewById(R.id.procedureEdt);
        commentsEdt = findViewById(R.id.commentsEdt);
        anotherEdt = findViewById(R.id.anotherEdt);
        notesaEdt = findViewById(R.id.notesaEdt); // new field
        equipmentEdt = findViewById(R.id.equipmentEdt);
        categorySpinner = findViewById(R.id.categorySpinner);
        imageView = findViewById(R.id.imageView);
        deleteRecipeBtn = findViewById(R.id.deleteRecipeBtn);
        backBtn = findViewById(R.id.backBtn);
        saveRecipeBtn = findViewById(R.id.saveRecipeBtn);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        CategorySelectedText = findViewById(R.id.CategorySelected);

        setCapitalizationWatcher(titleEdt, true);
        setCapitalizationWatcher(descriptionEdt, true);
        setCapitalizationWatcher(ingredientsEdt, false);
        setCapitalizationWatcher(procedureEdt, false);
        setCapitalizationWatcher(equipmentEdt, false);
        setCapitalizationWatcher(commentsEdt, false);
        setCapitalizationWatcher(anotherEdt, false);
        setCapitalizationWatcher(notesaEdt, false); // apply watcher

        populateCategorySpinner();
        selectedCategoriesList = new ArrayList<>();

        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId != null) loadRecipe(recipeId);

        saveRecipeBtn.setOnClickListener(v -> updateRecipe());
        deleteRecipeBtn.setOnClickListener(v -> showDeleteConfirmationDialog());

        backBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, FoodtechProfileActivity.class));
            finish();
        });

        imageView.setOnClickListener(v -> openFileChooser());
        uploadImageBtn.setOnClickListener(v -> openFileChooser());
    }

    private void setCapitalizationWatcher(EditText editText, boolean isTitleOrDescription) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
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
                    String capitalizedLine = isTitleOrDescription
                            ? Character.toUpperCase(line.charAt(0)) + line.substring(1)
                            : (!isBulletLine
                            ? "• " + Character.toUpperCase(line.charAt(0)) + line.substring(1)
                            : (line.length() > 2 && Character.isLowerCase(line.charAt(2))
                            ? "• " + Character.toUpperCase(line.charAt(2)) + line.substring(3)
                            : line));
                    formattedText.append(capitalizedLine).append("\n");
                }

                if (formattedText.length() > 0 && formattedText.charAt(formattedText.length() - 1) == '\n')
                    formattedText.setLength(formattedText.length() - 1);

                if (!formattedText.toString().equals(text)) {
                    editText.setText(formattedText.toString());
                    int bulletPosition = formattedText.indexOf("•", cursorPosition > 0 ? cursorPosition - 3 : 0);
                    int positionAfterBullet = bulletPosition != -1 ? bulletPosition + 3 : cursorPosition;
                    editText.setSelection(Math.min(positionAfterBullet, formattedText.length()));
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
                        for (int i = 0; i < updatedLines.length - 1; i++) newText.append(updatedLines[i]).append("\n");
                        editText.setText(newText.toString());
                        editText.setSelection(editText.getText().length());
                    }
                }

                editText.addTextChangedListener(this);
            }
        });
    }

    private void loadRecipe(String recipeId) {
        DatabaseReference recipeRef = database.getReference("Foods").child(recipeId);
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                Foods food = dataSnapshot.getValue(Foods.class);
                if (food != null) {
                    titleEdt.setText(food.getTitle());
                    originalTitle = food.getTitle();

                    String[] timeParts = food.getTimerId() != null ? food.getTimerId().split(" ") : new String[0];
                    String hours = "", minutes = "", seconds = "";
                    for (int i = 0; i < timeParts.length; i++) {
                        if (i > 0) {
                            if (timeParts[i].equalsIgnoreCase("Hour") || timeParts[i].equalsIgnoreCase("Hours")) hours = timeParts[i - 1];
                            if (timeParts[i].equalsIgnoreCase("Minute") || timeParts[i].equalsIgnoreCase("Minutes")) minutes = timeParts[i - 1];
                            if (timeParts[i].equalsIgnoreCase("Second") || timeParts[i].equalsIgnoreCase("Seconds")) seconds = timeParts[i - 1];
                        }
                    }

                    timerEdt.setText(hours);
                    minsEdt.setText(minutes);
                    secsEdt.setText(seconds);
                    descriptionEdt.setText(food.getDescription());
                    originalDescription = food.getDescription();
                    ingredientsEdt.setText(food.getIngredients());
                    originalIngredients = food.getIngredients();
                    procedureEdt.setText(food.getProcedure());
                    originalProcedure = food.getProcedure();
                    commentsEdt.setText(food.getComments());
                    anotherEdt.setText(food.getAnother());
                    notesaEdt.setText(food.getNotes());
                    equipmentEdt.setText(food.getEquipment());

                    String categoryNamesString = food.getCategoryName();
                    if (categoryNamesString != null) {
                        for (String categoryName : categoryNamesString.split(",\\s*")) {
                            Map<String, String> category = new HashMap<>();
                            category.put("CategoryId", String.valueOf(getCategoryId(categoryName.trim())));
                            category.put("CategoryName", categoryName.trim());
                            selectedCategoriesList.add(category);
                        }
                        updateSelectedCategoriesText();
                    }

                    if (food.getImagePath() != null)
                        Glide.with(EditRecipeActivity.this).load(food.getImagePath()).into(imageView);
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateRecipe() {
        String title = titleEdt.getText().toString().trim();
        String hours = timerEdt.getText().toString().trim();
        String minutes = minsEdt.getText().toString().trim();
        String seconds = secsEdt.getText().toString().trim();
        String timer = String.format("%s%s%s",
                hours.isEmpty() || hours.equals("0") ? "" : hours + (Integer.parseInt(hours) > 1 ? " Hours " : " Hour "),
                minutes.isEmpty() || minutes.equals("0") ? "" : minutes + (Integer.parseInt(minutes) > 1 ? " Minutes " : " Minute "),
                seconds.isEmpty() || seconds.equals("0") ? "" : seconds + (Integer.parseInt(seconds) > 1 ? " Seconds" : " Second"));
        String description = descriptionEdt.getText().toString().trim();
        String ingredients = ingredientsEdt.getText().toString().trim();
        String procedure = procedureEdt.getText().toString().trim();
        String comments = commentsEdt.getText().toString().trim();
        String another = anotherEdt.getText().toString().trim();
        String notesa = notesaEdt.getText().toString().trim();
        String equipment = equipmentEdt.getText().toString().trim();

        if (title.isEmpty() || timer.isEmpty() || description.isEmpty() || ingredients.isEmpty() ||
                procedure.isEmpty() || comments.isEmpty() || another.isEmpty() || notesa.isEmpty() || equipment.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showConfirmationDialog(title, description, ingredients, procedure, equipment, comments, another, notesa, timer);
    }

    private void showConfirmationDialog(String title, String description, String ingredients, String procedure, String equipment, String comments, String another, String notesa, String timer) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Do you want to update this recipe?")
                .setPositiveButton("Yes", (dialog, which) -> proceedToUpdateRecipe(title, timer, description, ingredients, procedure, comments, another, notesa, equipment))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void proceedToUpdateRecipe(String title, String timer, String description, String ingredients, String procedure, String comments, String another, String notesa, String equipment) {
        if (recipeId != null) {
            DatabaseReference ref = database.getReference("Foods").child(recipeId);
            StringBuilder categoryNames = new StringBuilder();
            int categoryId = 0;

            for (Map<String, String> category : selectedCategoriesList) {
                if (categoryNames.length() > 0) categoryNames.append(", ");
                categoryNames.append(category.get("CategoryName"));
                if (categoryId == 0) categoryId = Integer.parseInt(category.get("CategoryId"));
            }

            Map<String, Object> foodMap = new HashMap<>();
            foodMap.put("Title", title);
            foodMap.put("TimerId", timer);
            foodMap.put("Description", description);
            foodMap.put("Ingredients", ingredients);
            foodMap.put("Procedure", procedure);
            foodMap.put("Comments", comments);
            foodMap.put("Another", another);
            foodMap.put("Notes", notesa);
            foodMap.put("Equipment", equipment);
            foodMap.put("CategoryId", categoryId);
            foodMap.put("CategoryName", categoryNames.toString());
            foodMap.put("Star", 4.5);
            foodMap.put("LocationId", 1);
            foodMap.put("Price", 10.99);
            foodMap.put("PriceId", 1);
            foodMap.put("TimeId", 1);
            foodMap.put("TypeId", 1);
            foodMap.put("Count", 1);
            foodMap.put("UserId", auth.getCurrentUser().getUid());

            ref.updateChildren(foodMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imagePath = uri.toString();
                        database.getReference("Foods").child(recipeId).child("ImagePath").setValue(imagePath);
                        Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void populateCategorySpinner() {
        String[] categories = {"Breakfast", "Lunch", "Dinner", "Dessert", "Appetizer", "Snacks", "Side Dish", "Vegetarian", "Soup","Vegan", "Beverages", "Salad", "Others"};
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

        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
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
                            if (!selectedCategoriesList.stream().anyMatch(cat -> cat.get("CategoryId").equals(String.valueOf(which + 1)))) {
                                selectedCategoriesList.add(createCategoryMap(which, selectedCategory));
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
        StringBuilder selected = new StringBuilder("Selected Category:\n");
        for (Map<String, String> category : selectedCategoriesList) {
            selected.append("• ").append(category.get("CategoryName")).append("\n");
        }
        if (selected.length() > 0) selected.setLength(selected.length() - 1);
        CategorySelectedText.setText(selected.toString().trim());
    }

    private int getCategoryId(String categoryName) {
        switch (categoryName) {
            case "Breakfast": return 1;
            case "Lunch": return 2;
            case "Dinner": return 3;
            case "Dessert": return 4;
            case "Appetizer": return 5;
            case "Snacks": return 6;
            case "Side Dish": return 7;
            case "Others": return 8;
            case "Vegetarian": return 9;
            case "Soup": return 10;
            case "Vegan": return 11;
            case "Beverages": return 12;
            case "Salad": return 13;
            default: return 0;
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Do you wish to delete this recipe?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (recipeId != null)
                        database.getReference("Foods").child(recipeId).removeValue();
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
