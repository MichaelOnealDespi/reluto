package com.myapp.reluto.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.myapp.reluto.R;

public class ThemeActivity extends AppCompatActivity {
    private RadioGroup themeRadioGroup;
    private Button saveThemeButton;
    private ImageView backButton;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String THEME_KEY = "isDarkMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        themeRadioGroup = findViewById(R.id.theme_radio_group);
        saveThemeButton = findViewById(R.id.save_theme_button);
        backButton = findViewById(R.id.backbtn);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved theme preference
        boolean isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false);
        toggleTheme(isDarkMode);

        // Set the appropriate radio button based on the saved preference
        if (isDarkMode) {
            themeRadioGroup.check(R.id.radio_dark);
        } else {
            themeRadioGroup.check(R.id.radio_light);
        }

        // Save theme preference
        saveThemeButton.setOnClickListener(v -> {
            int selectedId = themeRadioGroup.getCheckedRadioButtonId();
            boolean isDark = selectedId == R.id.radio_dark;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(THEME_KEY, isDark);
            editor.apply();
            toggleTheme(isDark);
            finish(); // Close the activity after saving
        });

        // Set an OnClickListener for the back button
        backButton.setOnClickListener(v -> finish());
    }

    private void toggleTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
