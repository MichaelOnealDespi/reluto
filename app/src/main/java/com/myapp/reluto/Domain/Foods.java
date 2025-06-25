package com.myapp.reluto.Domain;

import java.io.Serializable;
import java.util.Map;

public class Foods implements Serializable {
    private String RecipeId; // Add this field
    private String Title;
    private String Description;
    private String Ingredients;
    private String ImagePath;
    private double Star;
    private int CategoryId; // Existing field
    private String CategoryName; // New field for category name
    private int LocationId;
    private double Price;
    private int PriceId;
    private String TimerId;
    private int TypeId;
    private int Count;
    private String UserId;
    private String Procedure;
    private String Equipment;
    private String Comments;
    private String Another;
    private String Notes;
    private int Likes; // Field for likes
    private Map<String, Boolean> userLikes; // Add this field

    // Default constructor required for calls to DataSnapshot.getValue(Foods.class)
    public Foods() {
        // Initialize with default values if needed
    }

    public Foods(String RecipeId, String Title, String Description, String Ingredients, String ImagePath, double Star,
                 int CategoryId, String CategoryName, int LocationId, double Price, int PriceId, String TimerId,
                 int TypeId, int Count, String UserId, String Procedure, String Equipment, String Comments, String Another, String Notes,
                 int Likes, Map<String, Boolean> userLikes) {
        this.RecipeId = RecipeId;
        this.Title = Title;
        this.Description = Description;
        this.Ingredients = Ingredients;
        this.ImagePath = ImagePath;
        this.Star = Star;
        this.CategoryId = CategoryId; // Existing field
        this.CategoryName = CategoryName; // New field
        this.LocationId = LocationId;
        this.Price = Price;
        this.PriceId = PriceId;
        this.TimerId = TimerId;
        this.TypeId = TypeId;
        this.Count = Count;
        this.UserId = UserId;
        this.Procedure = Procedure;
        this.Equipment = Equipment;
        this.Comments = Comments;
        this.Another = Another;
        this.Notes = Notes;
        this.Likes = Likes;
        this.userLikes = userLikes; // Initialize this field
    }

    // Getters and setters for all fields
    public String getRecipeId() {
        return RecipeId;
    }

    public void setRecipeId(String RecipeId) {
        this.RecipeId = RecipeId;
    }

    public int getLikes() {
        return Likes;
    }

    public void setLikes(int Likes) {
        this.Likes = Likes;
    }

    public Map<String, Boolean> getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(Map<String, Boolean> userLikes) {
        this.userLikes = userLikes;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getIngredients() {
        return Ingredients;
    }

    public void setIngredients(String Ingredients) {
        this.Ingredients = Ingredients;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String ImagePath) {
        this.ImagePath = ImagePath;
    }

    public double getStar() {
        return Star;
    }

    public void setStar(double Star) {
        this.Star = Star;
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int CategoryId) {
        this.CategoryId = CategoryId;
    }

    public String getCategoryName() { // New getter for CategoryName
        return CategoryName;
    }

    public void setCategoryName(String CategoryName) { // New setter for CategoryName
        this.CategoryName = CategoryName;
    }

    public int getLocationId() {
        return LocationId;
    }

    public void setLocationId(int LocationId) {
        this.LocationId = LocationId;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double Price) {
        this.Price = Price;
    }

    public int getPriceId() {
        return PriceId;
    }

    public void setPriceId(int PriceId) {
        this.PriceId = PriceId;
    }

    public String getTimerId() {
        return TimerId;
    }

    public void setTimerId(String TimerId) {
        this.TimerId = TimerId;
    }

    public int getTypeId() {
        return TypeId;
    }

    public void setTypeId(int TypeId) {
        this.TypeId = TypeId;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int Count) {
        this.Count = Count;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String UserId) {
        this.UserId = UserId;
    }

    public String getProcedure() {
        return Procedure;
    }

    public void setProcedure(String Procedure) {
        this.Procedure = Procedure;
    }

    public String getEquipment() {
        return Equipment;
    }

    public void setEquipment(String Equipment) {
        this.Equipment = Equipment;
    }

    public String getComments() {
        return Comments;
    }

    public void setComments(String Comments) {
        this.Comments = Comments;
    }

    // Getter for Another
    public String getAnother() { return Another; }

    // Setter for Another
    public void setAnother(String Another) { this.Another = Another; }

    // Getter for Notes
    public String getNotes() { return Notes; }

    // Setter for Notes
    public void setNotes(String Notes) { this.Notes = Notes; }

}
