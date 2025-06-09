package com.example.moneytrees1.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneytrees1.R;

public class GameOver extends AppCompatActivity {

    TextView tvPoints;
    TextView tvHighest;
    TextView tvSaveGoal;
    SharedPreferences sharedPreferences;
    ImageView ivNewHighest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        // Initialize views
        tvPoints = findViewById(R.id.tvPoints);
        tvHighest = findViewById(R.id.tvHighest);
        tvSaveGoal = findViewById(R.id.tvSaveGoal);
        ivNewHighest = findViewById(R.id.ivNewHighest);

        // Get points safely
        Intent intent = getIntent();
        int points = intent != null ? intent.getIntExtra("points", 0) : 0;
        tvPoints.setText(String.valueOf(points));

        sharedPreferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        int highest = sharedPreferences.getInt("highest", 0);

        // If new high score
        if (points > highest) {
            ivNewHighest.setVisibility(View.VISIBLE);
            // Resize to half size
            ivNewHighest.getLayoutParams().width = 150;
            ivNewHighest.getLayoutParams().height = 154;
            ivNewHighest.requestLayout();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("highest", points);
            editor.apply();
        }

        tvHighest.setText(String.valueOf(highest));
        tvSaveGoal.setText("R " + points);
    }

    public void restart(View view) {
        // 🔁 Make sure this matches your main game launcher!
        Intent intent = new Intent(GameOver.this, LeaderboardActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view) {
        finish(); // 👋 Exit GameOver
    }
}
