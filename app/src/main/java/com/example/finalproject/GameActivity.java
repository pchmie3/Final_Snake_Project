package com.example.finalproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.util.Random;

public class GameActivity extends Activity {

    Canvas gameArea;
    MainView snakeView;

    Bitmap head;
    Bitmap body;
    Bitmap tail;
    Bitmap apple;

    //for snake movement
    int travelDirection = 0;
    //0 = up, 1 = right, 2 = down, 3 = left


    int width;
    int height;
    int infoBox;

    //stats
    long lastFrameTime;
    int framesPerSecond;
    int score;
    int highScore;

    //Game objects
    int [] snakeXCoordinate;
    int [] snakeYCoordinate;
    int snakeLength;
    int appleX;
    int appleY;

    //The size in pixels of a place on the game board
    int pixelSize;
    int pixelLength;
    int pixelHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //loadSound();
        configureDisplay();
        snakeView = new MainView(this);
        setContentView(snakeView);

    }

    class MainView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder hold;
        volatile boolean playingSnake;
        Paint paint;
        Paint paint1;

        public MainView(Context context) {
            super(context);
            hold = getHolder();
            paint = new Paint();
            paint1 = new Paint();

            snakeXCoordinate = new int[200];
            snakeYCoordinate = new int[200];

            //need to get the snake into view
            getSnake();
            //get an apple to munch
            getApple();
        }

        public void getSnake(){
            snakeLength = 3;
            //start snake head in the middle of screen
            snakeXCoordinate[0] = pixelLength /2;
            snakeYCoordinate[0] = pixelHeight /2;

            //Then the body
            snakeXCoordinate[1] = snakeXCoordinate[0] - 1;
            snakeYCoordinate[1] = snakeYCoordinate[0];

            //And the tail
            snakeXCoordinate[1] = snakeXCoordinate[1] - 1;
            snakeYCoordinate[1] = snakeYCoordinate[0];
        }

        public void getApple(){
            Random random = new Random();
            appleX = random.nextInt(pixelLength - 1) + 1;
            appleY = random.nextInt(pixelHeight - 1) + 1;
        }

        //starts game
        @Override
        public void run() {
            while (playingSnake) {
                updateGame();
                drawGame();
                controlFPS();
            }
        }

        public void updateGame() {
            //Did the player get the apple
            if (snakeXCoordinate[0] == appleX && snakeYCoordinate[0] == appleY){
                //grow the snake
                snakeLength++;
                //get a new apple
                getApple();
                //add to the score 1
                score = score + 1;

                //TRYING TO GET HIGH SCORE
                //works but need to find a way to get it to the home screen
                if (score > highScore) {
                    highScore = score;
                }
            }

            //move the body - starting at the back
            for (int i = snakeLength; i > 0 ; i--) {
                snakeXCoordinate[i] = snakeXCoordinate[i - 1];
                snakeYCoordinate[i] = snakeYCoordinate[i - 1];
            }

            //Move the head in the appropriate direction
            switch (travelDirection){
                    //direction is up
                case 0://up
                    snakeYCoordinate[0]--;
                    break;

                    //direction is right
                case 1:
                    snakeXCoordinate[0]++;
                    break;

                    //direction is down
                case 2:
                    snakeYCoordinate[0]++;
                    break;

                    //direction is left
                case 3:
                    snakeXCoordinate[0]--;
                    break;
            }

            //Have we had an accident? Let's find out.
            boolean playerDEAD = false;

            //Did we hit any wall?
            if(snakeXCoordinate[0] == -1) {
                playerDEAD = true;
            }
            if(snakeXCoordinate[0] >= pixelLength) {
                playerDEAD = true;
            }
            if(snakeYCoordinate[0] == -1) {
                playerDEAD = true;
            }
            if(snakeYCoordinate[0] == pixelHeight) {
                playerDEAD = true;
            }

            //Hit part of ourselves?
            for (int i = snakeLength-1; i > 0; i--) {
                if ((i > 4) && (snakeXCoordinate[0] == snakeXCoordinate[i])
                        && (snakeYCoordinate[0] == snakeYCoordinate[i])) {
                    playerDEAD = true;
                }
            }

            if (playerDEAD){
                //start again
                score = 0;
                getSnake();
            }

        }

        public void drawGame() {
            if (hold.getSurface().isValid()) {
                gameArea = hold.lockCanvas();

                //background
                gameArea.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255, 255, 255, 255));

                paint.setTextSize(infoBox / 2);
                paint.setColor(Color.WHITE);
                gameArea.drawText("Score: " + score + "  High Score: " + highScore,
                        10, infoBox - 10, paint);

                paint1.setColor(Color.DKGRAY);

                //Draw the GAME BORDER
                paint.setStrokeWidth(4);
                gameArea.drawLine(1, infoBox,width - 1,infoBox, paint1);
                gameArea.drawLine(width - 1, infoBox,width - 1,
                        infoBox + (pixelHeight * pixelSize), paint1);
                gameArea.drawLine(width - 1,infoBox + (pixelHeight * pixelSize),
                        1,infoBox + (pixelHeight * pixelSize), paint1);
                gameArea.drawLine(1, infoBox, 1,
                        infoBox + (pixelHeight * pixelSize), paint1);

                //Draw the snake
                gameArea.drawBitmap(head, snakeXCoordinate[0] * pixelSize,
                        (snakeYCoordinate[0] * pixelSize) + infoBox, paint);

                //Draw the body
                for (int i = 1; i < snakeLength - 1; i++){
                    gameArea.drawBitmap(body, snakeXCoordinate[i] * pixelSize,
                            (snakeYCoordinate[i] * pixelSize) + infoBox, paint);
                }

                //draw the tail
                gameArea.drawBitmap(tail, snakeXCoordinate[snakeLength-1] * pixelSize,
                        (snakeYCoordinate[snakeLength - 1] * pixelSize) + infoBox, paint);

                //draw the apple
                gameArea.drawBitmap(apple, appleX * pixelSize,
                        (appleY * pixelSize) + infoBox, paint);

                hold.unlockCanvasAndPost(gameArea);
            }
        }

        public void controlFPS() {
            //controls how fast the snake goes
            long frameVelocity = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 100 - frameVelocity;

            //Attempting to make it go faster when you get a score over 5;
            //NOT WORKING AT THE MOMENT
            if (frameVelocity > 0) {
                if (score <= 5) {
                    framesPerSecond = (int) (20 / frameVelocity);
                } else if (score > 5) {
                    framesPerSecond = (int) (200 / frameVelocity);
                }
            }

            //the game is not going
            if (timeToSleep > 0) {
                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    //Print an error message to the console
                    Log.e("error", "failed to load sound files");
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }

        //right now, no pause button
        //we only have an action when there is a press on the back key
        public void pause() {
            playingSnake = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {

            }

        }

        //goes to the main screen and then follows the information on MainActivity
        public void resume() {
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        //Depends on where you press the screen to change direction
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            int divWidth = width / 2;

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if (motionEvent.getX() >= divWidth) {
                        //turn right
                        travelDirection++;

                        //there is no 4 so they loop
                        if(travelDirection == 4) {
                            //loop back to 0 and continue again
                            travelDirection = 0;
                        }

                    } else {
                        //turn left
                        travelDirection--;
                        if(travelDirection == -1) {
                            //no such direction
                            //loop back to 0(up)
                            travelDirection = 3;
                        }
                    }
            }
            return true;
        }
    }

    public void configureDisplay() {
        //find out the width and height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        infoBox = height / 15;

        //Determine the size of each block/place on the game board
        pixelSize = width / 15;

        //Determine how many game blocks will fit into the height and width
        //Leave one block for the score at the top
        pixelLength = 15;
        pixelHeight = ((height - infoBox)) / pixelSize;

        //Create the snake and scale of each element (like head, body, tail, and the apple)
        head = BitmapFactory.decodeResource(getResources(), R.drawable.crying);
        head = Bitmap.createScaledBitmap(head, pixelSize, pixelSize, false);

        body = BitmapFactory.decodeResource(getResources(), R.drawable.cs125);
        body = Bitmap.createScaledBitmap(body, pixelSize, pixelSize, false);

        tail = BitmapFactory.decodeResource(getResources(), R.drawable.cs125);
        tail = Bitmap.createScaledBitmap(tail, pixelSize, pixelSize, false);

        apple = BitmapFactory.decodeResource(getResources(), R.drawable.ilogo);
        apple = Bitmap.createScaledBitmap(apple, pixelSize, pixelSize, false);
    }


    @Override
    protected void onStop() {
        super.onStop();

        while (true) {
            snakeView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //pauses the game, but at the same time gets rid of it
            snakeView.pause();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            //finish();

            return true;
        }
        return false;
    }

}
