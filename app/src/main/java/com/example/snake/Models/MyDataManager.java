package com.example.snake.Models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class MyDataManager{
    private final FirebaseStorage storage;
    private final FirebaseDatabase realTimeDB;
    private final FirebaseAuth firebaseAuth;


    private static MyDataManager single_instance = null;


    private MyDataManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        realTimeDB = FirebaseDatabase.getInstance();
        Log.d("pttt","realTimeDB " +realTimeDB.getReference().toString());
    }

    public static MyDataManager getInstance() {
        return single_instance;
    }

    public static MyDataManager initHelper() {
        if (single_instance == null) {
            single_instance = new MyDataManager();
        }
        return single_instance;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public FirebaseDatabase getRealTimeDB() {
        return realTimeDB;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static MyDataManager getSingle_instance() {
        return single_instance;
    }

    public boolean checkIfUserExists(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final boolean[] userExists = {false};
        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userExists[0] = dataSnapshot.exists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
        return userExists[0];
    }

//    public boolean checkIfPasswordValid(String userId, String password) {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        final boolean[] valid = {false};
//        userId = userId.replace(".",",");
//        DatabaseReference child = databaseReference.child("users/"+userId);
//        child.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child("password").equals(password))
//                    valid[0] = dataSnapshot.exists();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Handle error
//            }
//        });
//        return valid[0];
//    }
//
//    public boolean checkIfPasswordValid(String id, String password) {
//        final boolean[] valid = {false};
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        id = id.replace(".",",");
//        DatabaseReference userRef = rootRef.child("users/" + id);
//        ValueEventListener eventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String password1 = dataSnapshot.child("password").getValue().toString();
//                if (password1.equals(password))
//                    valid[0] = true;
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d("FirebaseError", databaseError.getMessage());
//            }
//        };
//        userRef.addListenerForSingleValueEvent(eventListener);
//        return valid[0];
//    }

    public boolean checkIfPasswordValid(String email, String password) {
        final boolean[] isExist = {false};
        DatabaseReference myRef = getInstance().getRealTimeDB().getReference("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String newEmail = email.replace(".",",");
                    if (dataSnapshot.child("email").getValue().toString().equals(email)) {
                        Log.d("email", "I am in first if ");
                        if (dataSnapshot.child("password").getValue().toString().equals(password)) {
                            Log.d("email", "I am in second if ");
                            isExist[0] = true;
                            Log.d("email", "isExist = "+isExist[0]);
                            break;
                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
        return isExist[0];
    }







}