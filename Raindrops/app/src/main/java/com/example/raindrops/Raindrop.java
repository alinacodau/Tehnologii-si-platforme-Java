package com.example.raindrops;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.raindrops.GameView.screenRatioX;
import static com.example.raindrops.GameView.screenRatioY;

public class Raindrop {
    public boolean wasResolved = true;
    int x, y, width, height, result;
    Bitmap raindrop;
    public int speed = 20;
    String text;
    Bitmap burstRaindrop;
    Bitmap raindropWithText;

    Raindrop(Resources res) {
        raindrop = BitmapFactory.decodeResource(res, R.drawable.raindrop);

        width = raindrop.getWidth();
        height = raindrop.getHeight();

        width = (int) (width * screenRatioX);
        height = (int) (height * screenRatioY);

        raindrop = Bitmap.createScaledBitmap(raindrop, width, height, false);

        burstRaindrop = BitmapFactory.decodeResource(res, R.drawable.burst_raindrop);

        int burstRaindropWidth = burstRaindrop.getWidth();
        int burstRaindropHeight = burstRaindrop.getHeight();

        burstRaindropWidth = (int) (burstRaindropWidth * screenRatioX);
        burstRaindropHeight = (int) (burstRaindropHeight * screenRatioY);

        burstRaindrop = Bitmap.createScaledBitmap(burstRaindrop, burstRaindropWidth, burstRaindropHeight, false);
    }

    Bitmap getRaindrop() {
        return raindrop;
    }

    Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}
