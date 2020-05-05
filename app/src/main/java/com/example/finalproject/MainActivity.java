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
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//The home screen.
public class MainActivity extends Activity {

    //our screen
    Canvas canvas;

    //The Head of the Snake
    //Blinks on screen while the player is on the main screen
    Bitmap headAnimation;

    SnakeAnimView snakeAnimView;

    //The place we have to put things on
    Rect rectToBeDrawn;

    //Dimensions
    int frameHeight = 64;
    int frameWidth = 64;
    int numFrames  = 6;
    int frameNumber;

    int screenWidth;
    int screenHeight;

    //Basic Game information
    long lastFrameTime;
    int framesPerSecond;
    int highScore;

    //Gets information in order to start the game.
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting the width and height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        //This creates the head of the snake
        headAnimation = BitmapFactory.decodeResource(getResources(), R.drawable.ilogo);

        snakeAnimView = new SnakeAnimView(this);
        setContentView(snakeAnimView);

        i = new Intent(this, GameActivity.class);
    }

    class SnakeAnimView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder tohold;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeAnimView(Context context) {
            super(context);
            tohold = getHolder();
            paint = new Paint();
            frameHeight= headAnimation.getHeight();
            frameWidth = headAnimation.getWidth();
        }

        //Starts the game
        @Override
        public void run() {
            while (playingSnake){
                update();
                Draw();
                controlFPS();
            }
        }

        //Determines how fast everything goes
        private void controlFPS() {

            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 500 - timeThisFrame;
            //how fast the face blinks
            if (timeThisFrame > 0) {
                framesPerSecond = (int) (1000 / timeThisFrame);
            }

            if (timeToSleep > 0){
                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e){

                }
            }
            lastFrameTime = System.currentTimeMillis();
        }

        private void Draw() {

            //Creates the Main Screen
            if (tohold.getSurface().isValid()) {
                canvas = tohold.lockCanvas();
                canvas.drawColor(Color.WHITE);
                paint.setColor(Color.argb(255,255,255,255));

                //sets the main Title Size
                paint.setColor(Color.BLACK);
                paint.setTextSize(150);
                canvas.drawText("Snake",screenWidth / 2 - 225,200, paint);

                //Sets the subtitle size
                paint.setTextSize(50);
                canvas.drawText("Created by: Paulina & Aliya for CS125 Spring 2020", 10,
                        screenHeight - 40, paint);

                //Draws the snake head and makes it as big as you want
                //the destRect also allows it to flash while on the homescreen
                Rect destRect = new Rect(screenWidth / 2 - 250,screenHeight / 2 - 250,
                        screenWidth / 2 + 200,screenHeight / 2 + 200);
                canvas.drawBitmap(headAnimation, rectToBeDrawn, destRect, paint);

                tohold.unlockCanvasAndPost(canvas);
            }
        }

        private void update() {

            //which frame should be drawn
            rectToBeDrawn = new Rect((frameNumber * frameWidth) - 1,0,
                    (frameNumber * frameWidth+frameWidth) - 1, frameHeight);

            //now the next frame
            frameNumber++;

            //don't try and draw frames that don't exist
            if (frameNumber == numFrames) {
                //back to the beginning
                frameNumber = 0;
            }
        }


        //pauses the game.
        public void pause() {
            playingSnake = false;
            try {
                ourThread.join();
            } catch (InterruptedException e){

            }
        }

        //Create a new Game on Resume
        //When pausing a game (onbackbutton) and reentering, it will start anew
        public void resume(){
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        //starts the game whenever the main screen is touched
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            startActivity(i);
            return true;
        }
    }

    //for back press, this goes to the home screen and cancels the current game
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            snakeAnimView.pause();
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        while (true) {
            snakeAnimView.pause();
            break;
        }

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeAnimView.resume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        snakeAnimView.pause();
    }
}

