package com.example.raindrops;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.john.waveview.WaveView;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    private Background background1, background2;
    private int screenX, screenY, score = 0;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private Paint paintStroke;
    private Raindrop[] raindrops;
    private Random random;
    private SharedPreferences prefs;
    public int insertedValue = -1;
    private GameActivity activity;
    private SoundPool soundPool;
    private int soundBurst;
    MediaPlayer soundBackground;

    //water level settings
    public static final int INITIAL_WATER_LEVEL = 150;
    public static final int INCREMENT_VALUE_WATER = 150;
    public static final int NR_LEVELS_WATER = 3;
    public int currentWaterLevel = 0;
    public WaveView waveView;
    boolean isWaterRising = false;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.activity = activity;

        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        soundBurst = soundPool.load(activity, R.raw.burst, 1);

        soundBackground = MediaPlayer.create(activity, R.raw.background);
        if (!prefs.getBoolean("isMute", false))
            soundBackground.start();

        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1080f / screenX;
        screenRatioY = 1920f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        background2.x = screenX;
        paint = new Paint();
        paint.setTextSize(80);
        paint.setColor(Color.WHITE);

        paintStroke = new Paint();
        paintStroke.setStrokeWidth(5);
        paintStroke.setColor(Color.WHITE);

        raindrops = new Raindrop[4];

        for (int i = 0; i < 4; i++) {
            Raindrop raindrop = new Raindrop(getResources());
            raindrop.x = 0;
            raindrop.y = screenY;
            raindrops[i] = raindrop;
        }

        random = new Random();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            if (isWaterRising) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) waveView.getLayoutParams();
                        params.height = INITIAL_WATER_LEVEL + currentWaterLevel * INCREMENT_VALUE_WATER;
                        isWaterRising = false;
                        waveView.setLayoutParams(params);
                        if (currentWaterLevel == NR_LEVELS_WATER) {
                            isGameOver = true;
                        }
                    }
                });
            }
            if (isGameOver) {
                isPlaying = false;
                saveIfHighScore();
                waitBeforeExiting();
            }
            sleep();
        }
    }

    private void waitBeforeExiting() {

        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void update() {
        background1.x -= 5 * screenRatioX;
        background2.x -= 5 * screenRatioX;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }
        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        for (Raindrop raindrop : raindrops) {
            if (raindrop.result == insertedValue) {
                score++;
                raindrop.wasResolved = true;
            } else if (Rect.intersects(raindrop.getCollisionShape(), getCollisionShapeForWater())) {
                isWaterRising = true;
                currentWaterLevel++;
            }
        }
        insertedValue = -1;

        for (Raindrop raindrop : raindrops) {
            raindrop.y += raindrop.speed;

            if (raindrop.y + raindrop.height > screenY) {
                int bound = (int) (30 * screenRatioY);
                raindrop.speed = random.nextInt(bound);

                if (raindrop.speed < 10 * screenRatioY) {
                    raindrop.speed = (int) (10 * screenRatioY);
                }

                raindrop.x = random.nextInt(screenX - raindrop.width); //sa nu iasa din ecran, ex:jumate inauntru,jumate afara
                raindrop.y = -raindrop.height; //sa porneasca de sus, sa se iveasca treptat,nu teleporat la 0
                raindrop.wasResolved = false;

                //textul si rezultatul calculului
                Calculation calculation = new Calculation();
                raindrop.text = calculation.getCalculationQuestion();
                raindrop.result = calculation.getCalculationResult();
                raindrop.raindropWithText = drawMultilineTextToBitmap(raindrop.getRaindrop(), raindrop.text);
            }
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            for (Raindrop raindrop : raindrops) {
                if (raindrop.wasResolved || isWaterRising) {
                    if (!prefs.getBoolean("isMute", false))
                        soundPool.play(soundBurst, 1, 1, 0, 0, 1);
                    canvas.drawBitmap(raindrop.burstRaindrop, raindrop.x, raindrop.y, paint);
                    raindrop.y = screenY;
                    raindrop.wasResolved = false;
                } else {
                    canvas.drawBitmap(raindrop.raindropWithText, raindrop.x, raindrop.y, paint);
                }

                int initialWaterLevelY = screenY - INITIAL_WATER_LEVEL;
                canvas.drawLine(0, initialWaterLevelY, 30, initialWaterLevelY, paintStroke);
                for (int i = 1; i <= NR_LEVELS_WATER; i++) {
                    canvas.drawLine(0, initialWaterLevelY - i * INCREMENT_VALUE_WATER, 30, initialWaterLevelY - i * INCREMENT_VALUE_WATER, paintStroke);
                }

                canvas.drawText(score + "", screenX / 2f, 164, paint);
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public Bitmap drawMultilineTextToBitmap(Bitmap bitmap,
                                            String gText) {

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);

        // new antialiased Paint
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        float scale = getResources().getDisplayMetrics().density;
        paint.setTextSize((int) (25 * scale));

        // set text width to canvas width minus 16dp padding
        int textWidth = canvas.getWidth() - (int) (16 * scale);

        // init StaticLayout for text
        StaticLayout textLayout = new StaticLayout(
                gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // get height of multiline text
        int textHeight = textLayout.getHeight();

        // get position of text's top left corner
        float x = (bitmap.getWidth() - textWidth) / 2;
        float y = (bitmap.getHeight() - textHeight) / 2;

        // draw text to the Canvas center
        canvas.save();
        canvas.translate(x, y);
        textLayout.draw(canvas);
        canvas.restore();

        return bitmap;
    }

    private Rect getCollisionShapeForWater() {
        return new Rect(0, screenY - INITIAL_WATER_LEVEL - currentWaterLevel * INCREMENT_VALUE_WATER, screenX, screenY);
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveIfHighScore() {
        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
            soundBackground.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
