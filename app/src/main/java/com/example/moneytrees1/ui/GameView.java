package com.example.moneytrees1.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.example.moneytrees1.R;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    Bitmap background, ground, bunny;
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float Text_Size = 120;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    float bunnyX, bunnyY;
    float oldX;
    float oldBunnyX;
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;

    private boolean isGameOver = false;

    public GameView(Context context) {
        super(context);
        this.context = context;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        bunny = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bunny), 252, 315, false);

        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;

        rectBackground = new Rect(0, 0, dWidth, dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);

        handler = new Handler(Looper.getMainLooper());
        runnable = this::invalidate;

        textPaint.setColor(Color.rgb(255, 165, 0));
        textPaint.setTextSize(Text_Size);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));

        healthPaint.setColor(Color.GREEN);

        random = new Random();
        bunnyX = dWidth / 2f - bunny.getWidth() / 2f;
        bunnyY = dHeight - ground.getHeight() - bunny.getHeight();

        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            spikes.add(new Spike(context));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isGameOver) return;

        super.onDraw(canvas);

        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(bunny, bunnyX, bunnyY, null);

        for (Spike spike : spikes) {
            canvas.drawBitmap(spike.getSpike(spike.spikeFrame), spike.spikeX, spike.spikeY, null);
            spike.spikeFrame = (spike.spikeFrame + 1) % 3;
            spike.spikeY += spike.spikeVelocity;

            if (spike.spikeY + spike.getSpikeHeight() >= dHeight - ground.getHeight()) {
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spike.spikeX;
                explosion.explosionY = spike.spikeY;
                explosions.add(explosion);
                spike.resetPosition();
            }

            if (spike.spikeX + spike.getSpikeWidth() >= bunnyX &&
                    spike.spikeX <= bunnyX + bunny.getWidth() &&
                    spike.spikeY + spike.getSpikeHeight() >= bunnyY &&
                    spike.spikeY <= bunnyY + bunny.getHeight()) {

                life--;
                spike.resetPosition();

                if (life <= 0) {
                    triggerGameOver();
                    return;
                }
            }
        }

        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            if (explosion.explosionFrame < 4) {
                canvas.drawBitmap(explosion.getExplosion(explosion.explosionFrame), explosion.explosionX, explosion.explosionY, null);
                explosion.explosionFrame++;
            } else {
                explosions.remove(i);
            }
        }

        if (life == 2) {
            healthPaint.setColor(Color.YELLOW);
        } else if (life == 1) {
            healthPaint.setColor(Color.RED);
        }

        canvas.drawRect(dWidth - 200, 30, dWidth - 200 + 60 * life, 80, healthPaint);
        canvas.drawText("" + points, 20, Text_Size, textPaint);

        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    private void triggerGameOver() {
        isGameOver = true;
        handler.removeCallbacks(runnable);

        // Ensure activity transition happens on main thread
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                Intent intent = new Intent(context, GameOver.class);
                intent.putExtra("points", points);
                context.startActivity(intent);
                ((Activity) context).finish();
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver) return false;

        float touchX = event.getX();
        float touchY = event.getY();
        if (touchY >= bunnyY) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = touchX;
                oldBunnyX = bunnyX;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float shift = oldX - touchX;
                float newBunnyX = oldBunnyX - shift;
                bunnyX = Math.max(0, Math.min(newBunnyX, dWidth - bunny.getWidth()));
            }
        }
        return true;
    }
}
