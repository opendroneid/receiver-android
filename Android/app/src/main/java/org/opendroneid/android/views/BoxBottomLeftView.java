package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import org.opendroneid.android.R;

public class BoxBottomLeftView extends CustomGlowView {

    private Paint paint;
    private Path path;
    private int glowAlpha = 255;

    public BoxBottomLeftView(Context context) {
        super(context);
        init();
    }

    public BoxBottomLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoxBottomLeftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        startGlowEffect(() -> glowAlpha = (glowAlpha + 1) % 256);
    }

    @Override
    protected void onCustomDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int shadowColor = getResources().getColor(R.color.paleSky);

        // Draw first view
        path.reset();
        path.moveTo(0, height - 10);
        path.lineTo(width, height - 10);
        path.lineTo(width, 120 - 12);
        path.lineTo(0.0f, 0);
        path.close();

        paint.setColor(getResources().getColor(R.color.green));
        paint.setStyle(Paint.Style.FILL);

        paint.setAlpha(glowAlpha);
        canvas.drawPath(path, paint);

        // Draw the second view
        int secondWidth = width - 12;

        path.reset();
        path.moveTo(0, height);
        path.lineTo(secondWidth, height);
        path.lineTo(secondWidth, 120);
        path.lineTo(0.0f, 10);
        path.close();

        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(10, 0, 0, shadowColor);

        canvas.drawPath(path, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onCustomDraw(canvas);
    }
}