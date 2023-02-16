package com.example.snake.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.snake.Models.User;
import com.example.snake.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivitySignUp extends AppCompatActivity {

    MaterialButton btnSignUp;
    CheckBox isPremium;
    TextInputEditText edtName, edtEmail, edtPassword;
    private Bundle bundle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign_up);
        findViews();

        bundle = new Bundle();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString(MainActivityLogin.EMAIL, edtEmail.getText().toString());
                bundle.putString(MainActivityLogin.PASSWORD, edtPassword.getText().toString());

                User userToSture = new User().setEmail(edtEmail.getText().toString()).setPassword(edtPassword.getText().toString()).setPremium(isPremium.isChecked()).setName(edtName.getText().toString());
                saveDataToDatabase(userToSture);

            }
        });
    }

    private void saveDataToDatabase(User user) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(edtEmail.getText().toString().replace(".",",")).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data successfully saved
                        Intent intent = new Intent(MainActivitySignUp.this, MainActivityLogin.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save data
                        Toast.makeText(MainActivitySignUp.this, "Failed save user "+ e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void findViews() {
        edtEmail = findViewById(R.id.main_EDT_email);
        edtName = findViewById(R.id.main_EDT_name);
        edtPassword = findViewById(R.id.main_EDT_password);
        isPremium = findViewById(R.id.premium_account);
        btnSignUp = findViewById(R.id.btn_sign_up);
    }
}