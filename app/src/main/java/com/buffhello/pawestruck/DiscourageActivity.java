package com.buffhello.pawestruck;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Comes up when a user tries to Filter based on Gender or Breed. Text is based on the Filter chosen
 */
public class DiscourageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discourage);

        TextView tvDiscourage = findViewById(R.id.discourage_text);
        boolean isGender = getIntent().getBooleanExtra("isGender", true);

        if (isGender) tvDiscourage.setText(R.string.discourage_gender);
        else tvDiscourage.setText(R.string.discourage_breed);

    }
}
