package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.snake.Models.SensorsEnum;
import com.example.snake.Models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivityPremium extends AppCompatActivity {

    MaterialButton btnPremium;
    TextInputEditText edtName, edtEmail, edtPassword;
    private Bundle bundle;

    public static final String NAME = "NAME";
    public static  final String EMAIL = "EMAIL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_premium);
        findViews();

        Bundle loginBundle = getIntent().getExtras();
        edtName.setText(loginBundle.getString(NAME));
        edtEmail.setText(loginBundle.getString(EMAIL));

        bundle = new Bundle();

        btnPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString(GameActivity.NAME, edtName.getText().toString());
                bundle.putString(GameActivity.EMAIL, edtEmail.getText().toString());
                bundle.putBoolean(GameActivity.PREMIUM, true);
                bundle.putInt(GameActivity.SENSOR_TYPE, SensorsEnum.withSensors.getValue());
                bundle.putString(GameActivity.PASSWORD, edtPassword.getText().toString());


                Intent intent = new Intent(MainActivityPremium.this, GameActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }

    private void findViews() {
        edtEmail = findViewById(R.id.main_EDT_email);
        edtName = findViewById(R.id.main_EDT_name);
        edtPassword = findViewById(R.id.main_EDT_password);
        btnPremium = findViewById(R.id.mainPremium_Btn_premium);
    }
}