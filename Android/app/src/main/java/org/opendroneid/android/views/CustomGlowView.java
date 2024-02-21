package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public abstract class CustomGlowView extends View {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public CustomGlowView(Context context) {
        super(context);
    }

    public CustomGlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomGlowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void startGlowEffect(final Runnable glowRunnable) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                glowRunnable.run();
                invalidate();

                handler.postDelayed(this, 20);
            }
        });
    }

    protected abstract void onCustomDraw(Canvas canvas);
}

