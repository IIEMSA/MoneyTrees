package com.example.moneytrees1.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.moneytrees1.R;

public class Explosion {
    Bitmap explosion[] = new Bitmap[4];
    int explosionFrame = 0;
    int explosionX, explosionY;

    public Explosion(Context context){
        explosion[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), com.example.moneytrees1.R.drawable.explosion0), 240, 210, false);
        explosion[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion1), 240, 210, false);
        explosion[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion2), 240, 210, false);
        explosion[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion3), 240, 210, false);
    }

    public Bitmap getExplosion(int explosionFrame){
        return explosion[explosionFrame];
    }
}

