package com.example.snake;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.snake.Models.MyDataManager;
import com.example.snake.Models.Sensors;
import com.example.snake.Models.SensorsEnum;
import com.example.snake.Models.SnakePoints;
import com.example.snake.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class GameActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    //list of snake points / snake length
    private final List<SnakePoints> snakePointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTv;

    //getting holder to draw snake on surfac's canvas
    private SurfaceHolder surfaceHolder;

    //snake moving position. values must be right, left, bottom;
    //by default snake move to right
    private String movingPosition = "right";

    //score
    private int score = 0;

    //snake size / point size
    //you can change this value to maje bigger size snake
    private static final int pointSize = 28;
    //default snake tale
    private static final int defaultTalPoints = 3;
    //snake color
    private static final int snakeColor = Color.YELLOW;
    //snake moving speed. value must be lie between 1-1000;
    private static final int snakeMovingSpeed = 800;
    //random point position coordinates on surfaceView
    private int positionX, positionY;
    //timer to move snake / change snake position after every a specific time (snakeMovingSpeed)
    private Timer timer;

    //canvas to draw snake and show on surface view
    private Canvas canvas = null;

    //point color / single point color of snake 
    private Paint pointColor = null;

    //sensors
    public static final String SENSOR_TYPE = "SENSOR_TYPE";
    private int sensorType;
    private Sensors mySensors;
    private SensorManager sensorManager;


    //finals
    public static final String NAME = "NAME";
    public static final String EMAIL = "EMAIL";
    public static final String PREMIUM = "PREMIUM";
    public static final String PASSWORD = "PASSWORD";
    private User userToStore;

    //DB
    private final MyDataManager dataManager = MyDataManager.getInstance();
    private final FirebaseDatabase realtimeDB = dataManager.getRealTimeDB();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //getting surfaceView and score textView from xml file
        surfaceView = findViewById(R.id.surfaceView);
        scoreTv = findViewById(R.id.scoreTv);

        getDataFromLogin();

        //getting ImageButtons from xml files
        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);
        final AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);

        //adding callback to surfaceView
        surfaceView.getHolder().addCallback(this);

        User user = new User().setEmail("test");

        //sensors
        if (sensorType == SensorsEnum.withSensors.getValue()) {
            initSensors();
            mySensors.getSensorManager().registerListener(accSensorEventListener, mySensors.getAccSensor(), SensorManager.SENSOR_DELAY_NORMAL);
            leftBtn.setVisibility(View.INVISIBLE);
            topBtn.setVisibility(View.INVISIBLE);
            rightBtn.setVisibility(View.INVISIBLE);
            bottomBtn.setVisibility(View.INVISIBLE);
        } else {
            topBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //check if previous moving position in not bottom. snake can't move
                    //For example if snake moving to bottom then snake can't directly start moving to top
                    //snake must tak right of left first then top
                    if (!movingPosition.equals("bottom")) {
                        movingPosition = "top";
                        Log.d("pttt", "my");

                    }
                }
            });

            leftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!movingPosition.equals("left")) {
                        movingPosition = "right";
                    }
                }
            });

            rightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!movingPosition.equals("right")) {
                        movingPosition = "left";
                    }
                }
            });

            bottomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!movingPosition.equals("top")) {
                        movingPosition = "bottom";
                    }
                }
            });
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        //when surface is created get surfaceHolder from it and assign to surfaceHolder
        this.surfaceHolder = holder;

        //init data for snake / surfaceView
        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    private void init() {

        //clear snake point / snake length
        snakePointsList.clear();
        //set score as 0
        scoreTv.setText("0");

        //make score 0
        score = 0;

        //setting default moving position
        movingPosition = "right";

        //default snake starting position on the screen
        int startPositionX = (pointSize) * defaultTalPoints;

        //making snake's default length / points
        for (int i = 0; i < defaultTalPoints; i++) {

            //adding points to snake's tale
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoints);

            //increasing value for next point as snake's tale
            startPositionX = startPositionX - (pointSize * 2);
        }

        //add random point on the screen to be eaten by the snake
        addPoint();

        //start moving snake / start game
        moveSnake();
    }

    private void addPoint() {

        //getting surfaceView width and height to add point on surface to be eaten by the snake
        int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

        int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);

        //check if randomXPosition is even or odd value. we need only even number
        if ((randomXPosition % 2) != 0) {
            randomXPosition = randomXPosition + 1;
        }

        if (randomYPosition % 2 != 0) {
            randomYPosition = randomYPosition + 1;
        }

        positionX = (pointSize * randomXPosition) + pointSize;
        positionY = (pointSize * randomYPosition) + pointSize;
    }

    private void moveSnake() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //getting head position
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                //check if snake eaten a point
                if (headPositionX == positionX && positionY == headPositionY) {
                    //grow snake after eaten point
                    growSnake();

                    //add another random point on the screen
                    addPoint();
                }

                //check of which side snake is moving
                switch (movingPosition) {
                    case "right":
                        //move snake's head to right
                        //other point follow snake's head point to move the snake
                        snakePointsList.get(0).setPositionX(headPositionX + (pointSize * 2));
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "left":
                        //move snake's head to left
                        //other point follow snake's head point to move the snake
                        snakePointsList.get(0).setPositionX(headPositionX - (pointSize * 2));
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "top":
                        //move snake's head to top.
                        //other point follow snake's head point to move the snake
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY - (pointSize * 2));
                        break;
                    case "bottom":
                        //move snake's head to bottom
                        //other point follow snake's head point to move the snake
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY + (pointSize * 2));
                        break;
                }
                //check if game over. whethear snake touch edges or snake itself
                if (checkGameOver(headPositionX, headPositionY)) {
                    //stop timer / stop moving snake
                    timer.purge();
                    timer.cancel();

                    checkIfDBContainUser(userToStore, score);
                    //show game over dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                    builder.setMessage("Your score = " + score);
                    builder.setTitle("Game Over");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Start Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //restart game / re-init data
                            init();
                        }
                    });

                    //timer runs in backround so we need to show dialog on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                } else {
                    canvas = new Canvas();
                    //lock canvas on surfaceHolder to draw on it
                    canvas = surfaceHolder.lockCanvas();

                    //clear canvas with white color
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

                    //change snake's head position, other snake points will follow snake's head
                    canvas.drawCircle(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY(), pointSize, createPointColor());

                    //draw random point circle on the surface to be eaten by the snake
                    canvas.drawCircle(positionX, positionY, pointSize, createPointColor());

                    //other points is following snake's head. position 0 is head of snake
                    for (int i = 1; i < snakePointsList.size(); i++) {
                        int getTempositionX = snakePointsList.get(i).getPositionX();
                        int getTempPositionY = snakePointsList.get(i).getPositionY();

                        //move point accross the head
                        snakePointsList.get(i).setPositionX(headPositionX);
                        snakePointsList.get(i).setPositionY(headPositionY);
                        canvas.drawCircle(snakePointsList.get(i).getPositionX(), snakePointsList.get(i).getPositionY(), pointSize, createPointColor());

                        //change head position
                        headPositionX = getTempositionX;
                        headPositionY = getTempPositionY;
                    }

                    //unlock canvas to draw on surfaceView
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void growSnake() {
        //create new snake point
        SnakePoints snakePoints = new SnakePoints(0, 0);

        //add point to the snake's tale
        snakePointsList.add(snakePoints);

        //increase score
        score++;

        //setting score to TextViews
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTv.setText(String.valueOf(score));
            }
        });
    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {
        boolean gameOver = false;

        //check if snake's head touches edges
        if (snakePointsList.get(0).getPositionX() < 0 ||
                snakePointsList.get(0).getPositionY() < 0 ||
                snakePointsList.get(0).getPositionX() >= surfaceView.getWidth() ||
                snakePointsList.get(0).getPositionY() >= surfaceView.getHeight()) {
            gameOver = true;
        } else {
            //check if snakes head touches anke itself
            for (int i = 0; i < snakePointsList.size(); i++) {
                if (headPositionX == snakePointsList.get(i).getPositionX() &&
                        headPositionY == snakePointsList.get(i).getPositionY()) {
                    gameOver = true;
                    break;
                }
            }
        }
        return gameOver;
    }

    private Paint createPointColor() {
        //check if color not defined before
        if (pointColor == null) {
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true); //smoothness
        }
        return pointColor;
    }

    private void getDataFromLogin() {
        Bundle bundle = getIntent().getExtras();
        sensorType = bundle.getInt(SENSOR_TYPE);
        String email = bundle.getString(EMAIL).replace(".",",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                 userToStore = dataSnapshot.getValue(User.class);
                }
                else {
                    userToStore = new User();
                    userToStore.setName(bundle.getString(NAME)).setPremium(bundle.getBoolean(PREMIUM)).setEmail(bundle.getString(EMAIL)).setPassword(bundle.getString(PASSWORD));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
        }

    //sensors
    private void initSensors() {
        mySensors = new Sensors();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mySensors.setSensorManager(sensorManager);
        mySensors.initSensor();
    }

    private SensorEventListener accSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];

            if (x < -5) {// move right
                movingPosition = "right";
            } else if (x > 5) {// move left
                movingPosition = "left";
            } else if (y < -3) {// move up
                movingPosition = "top";
            } else if (y > 5) {// move down
                movingPosition = "bottom";
            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.d("pttt", "onAccuracyChanged");
        }

    };

//    private void checkIfDBContainUser(User userToStore, int score) {
//        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference userNameRef = rootRef.child("Users").child(userToStore.getEmail().replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.exists()) {
//                    rootRef.child("Users").child(userToStore.getEmail()).setValue(userToStore);
//                } else {
//                    userToStore.getRecords().add(score);
//                    rootRef.child("Users").child(userToStore.getEmail()).setValue(userToStore);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }

    private void checkIfDBContainUser(User userToStore, int score){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("users").child(userToStore.getEmail().replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // save user data
                    userToStore.getRecords().add(score);
                    rootRef.child("users").child(userToStore.getEmail().replace(".",",")).setValue(userToStore);
                }
                else {
                    userToStore.getRecords().add(score);
                    rootRef.child("users").child(userToStore.getEmail().replace(".",",")).setValue(userToStore);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // handle error
            }
        });
    }

}
