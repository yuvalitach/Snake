package com.example.snake.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snake.Models.MyDataManager;
import com.example.snake.Models.User;
import com.example.snake.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivityLogin extends AppCompatActivity {

    MaterialButton btnSignIn;
    TextInputEditText edtPass, edtEmail;
    TextView signUpTv;
    private Bundle bundle;
    private FrameLayout main_LAY_banner;
    public static final String PASSWORD = "PASSWORD";
    public static  final String EMAIL = "EMAIL";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        findViews();

        Bundle loginBundle = getIntent().getExtras();
        if (loginBundle!=null){
            edtEmail.setText(loginBundle.getString(EMAIL));
            edtPass.setText(loginBundle.getString(PASSWORD));
        }

        bundle = new Bundle();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString(GameActivity.EMAIL, edtEmail.getText().toString());

                checkIfDBContainUser(edtEmail.getText().toString(), edtPass.getText().toString());

            }
        });

        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(MainActivityLogin.this, MainActivitySignUp.class);
                    startActivity(intent);
                    finish();
            }
        });
    }

    private void checkIfDBContainUser(String email, String password){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("users").child(email.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.child("password").getValue().toString().equals(password)){
                        bundle.putBoolean(GameActivity.PREMIUM,(Boolean)dataSnapshot.child("premium").getValue());
                        Intent intent = new Intent(MainActivityLogin.this, GameActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                    else
                        Toast.makeText(MainActivityLogin.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivityLogin.this, "Wrong Email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // handle error
            }
        });
    }

    private void findViews() {
        btnSignIn = findViewById(R.id.main_BTN_sign_in);
        signUpTv = findViewById(R.id.textViewSignUp);
        edtPass = findViewById(R.id.main_EDT_password);
        edtEmail = findViewById(R.id.main_EDT_email);
        main_LAY_banner = findViewById(R.id.main_LAY_banner);
    }

    private void showBanner() {
        String UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
        AdView adView = new AdView(this);
        adView.setAdUnitId(UNIT_ID);
        main_LAY_banner.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}

