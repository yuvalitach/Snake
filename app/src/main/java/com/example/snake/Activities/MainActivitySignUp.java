package com.example.snake.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snake.Models.User;
import com.example.snake.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;

public class MainActivitySignUp extends AppCompatActivity {

    MaterialButton btnSignUp;
    CheckBox isPremium;
    TextInputEditText edtName, edtEmail, edtPassword;
    private Bundle bundle;
    TextView textViewTermsOfUse, textViewPrivacyPolicy;



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

        textViewTermsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHtmlTextDialog(MainActivitySignUp.this, "terms_of_use.html");
            }
        });
        textViewPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHtmlTextDialog(MainActivitySignUp.this, "privacy_policy.html");
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
        textViewPrivacyPolicy = findViewById(R.id.textViewPrivacyPolicy);
        textViewTermsOfUse = findViewById(R.id.textViewTermsOfUse);
    }

    public static void openHtmlTextDialog(Activity activity, String fileNameInAssets) {
        String str = "";
        InputStream is = null;

        try {
            is = activity.getAssets().open(fileNameInAssets);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(activity);
        materialAlertDialogBuilder.setPositiveButton("Close", null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            materialAlertDialogBuilder.setMessage(Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY));
        } else {
            materialAlertDialogBuilder.setMessage(Html.fromHtml(str));
        }

        AlertDialog al = materialAlertDialogBuilder.show();
        TextView alertTextView = al.findViewById(android.R.id.message);
        alertTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}