package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.snake.Activities.GameActivity;
import com.example.snake.Activities.MainActivitySignUp;
import com.google.android.material.button.MaterialButton;

public class GameOverActivity extends AppCompatActivity {

    private Bundle bundle,getDataBundle;

    public static  final String SCORE = "SCORE";
    public static  final String PREMIUM = "PREMIUM";
    public static  final String EMAIL = "EMAIL";
    MaterialButton btnPlayAgain, btnPurchaseLife;
    TextView scoreTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        findViews();
        bundle = new Bundle();
        getDataBundle = getIntent().getExtras();

        scoreTv.setText("Your score: "+getDataBundle.getInt(SCORE));
    btnPlayAgain.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bundle.putBoolean(GameActivity.PREMIUM,getDataBundle.getBoolean(PREMIUM));
            bundle.putString(GameActivity.EMAIL,getDataBundle.getString(EMAIL));
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    });

        btnPurchaseLife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putBoolean(GameActivity.PREMIUM,getDataBundle.getBoolean(PREMIUM));
                bundle.putString(GameActivity.EMAIL,getDataBundle.getString(EMAIL));
                bundle.putInt(GameActivity.SCORE,getDataBundle.getInt(SCORE));
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });


    }

    private void findViews() {
        btnPlayAgain = findViewById(R.id.Play_again_BTN);
        btnPurchaseLife = findViewById(R.id.Purchase_life_BTN);
        scoreTv = findViewById(R.id.scoreTv);
    }
}