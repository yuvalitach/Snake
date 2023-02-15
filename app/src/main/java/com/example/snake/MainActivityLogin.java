package com.example.snake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.snake.Models.MyDataManager;
import com.example.snake.Models.SensorsEnum;
import com.example.snake.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MainActivityLogin extends AppCompatActivity {

    MaterialButton btnSignIn, btnPremium, btnPlayPremium;
    TextInputEditText edtName, edtEmail;
    private Bundle bundle;
    private PopupWindow popupWindow;

    //DB
    private final MyDataManager dataManager = MyDataManager.getInstance();
    private User userToStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        findViews();


        bundle = new Bundle();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString(GameActivity.NAME, edtName.getText().toString());
                bundle.putString(GameActivity.EMAIL, edtEmail.getText().toString());
                bundle.putBoolean(GameActivity.PREMIUM, false);
                bundle.putInt(GameActivity.SENSOR_TYPE, SensorsEnum.withoutSensors.getValue());

                Intent intent = new Intent(MainActivityLogin.this, GameActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        btnPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    bundle.putString(MainActivityPremium.NAME, edtName.getText().toString());
                    bundle.putString(MainActivityPremium.EMAIL, edtEmail.getText().toString());
                    Intent intent = new Intent(MainActivityLogin.this, MainActivityPremium.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
            }
        });

        btnPlayPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonShowPopupWindowClick(findViewById(R.id.activity_login));
            }
        });
    }

    public void onButtonShowPopupWindowClick(View view) {

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_window, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        MaterialButton popup_BTN_save =(MaterialButton) popupView.findViewById(R.id.popup_BTN_save);
        MaterialButton popup_BTN_cancel =(MaterialButton) popupView.findViewById(R.id.popup_BTN_cancel);
        TextInputEditText popup_LBL_password = (TextInputEditText) popupView.findViewById(R.id.popup_LBL_ID);;


        popup_BTN_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference myRef = dataManager.getRealTimeDB().getReference("users");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.child("email").getValue().toString().equals(edtEmail.getText().toString())) {
                                if (dataSnapshot.child("password").getValue().toString().equals(popup_LBL_password.getText().toString())) {
                                    popupWindow.dismiss();
                                    Intent intent = new Intent(MainActivityLogin.this, GameActivity.class);
                                    bundle.putString(GameActivity.NAME, edtName.getText().toString());
                                    bundle.putString(GameActivity.EMAIL, edtEmail.getText().toString());
                                    bundle.putBoolean(GameActivity.PREMIUM, true);
                                    bundle.putInt(GameActivity.SENSOR_TYPE, SensorsEnum.withSensors.getValue());
                                    bundle.putString(GameActivity.PASSWORD, popup_LBL_password.getText().toString());
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(MainActivityLogin.this, "Your Password is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }

                });

//                if (dataManager.checkIfPasswordValid(edtEmail.getText().toString(),popup_LBL_password.getText().toString())) {
//                popupWindow.dismiss();
//                Intent intent = new Intent(MainActivityLogin.this, GameActivity.class);
//                intent.putExtras(bundle);
//                startActivity(intent);
//                finish();
//            }
//            else {
//                Toast.makeText(MainActivityLogin.this, "Your Password is incorrect", Toast.LENGTH_SHORT).show();
//            }
            }
        });
    popup_BTN_cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            popupWindow.dismiss();
        }
    });


    }

    private void findViews() {
        btnSignIn = findViewById(R.id.main_BTN_sign_in);
        btnPremium = findViewById(R.id.main_BTN_premium);
        btnPlayPremium = findViewById(R.id.main_BTN_PlayPremium);
        edtName = findViewById(R.id.main_EDT_name);
        edtEmail = findViewById(R.id.main_EDT_email);
    }
}

