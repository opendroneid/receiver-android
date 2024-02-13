package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import org.opendroneid.android.R;

public class BoxTopRightView extends View {

    private Paint paint;
    private Path path;

    private int glowAlpha = 255;
    private final Handler handler = new Handler();

    public BoxTopRightView(Context context) {
        super(context);
        init();
    }

    public BoxTopRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoxTopRightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int shadowColor = getResources().getColor(R.color.paleSky);

        //Draw first view
        path.reset();
        path.moveTo(width, 0);
        path.lineTo(width, height);
        path.lineTo(0, height - 120);
        path.lineTo(0, 0);
        path.close();

        paint.setColor(getResources().getColor(R.color.green));
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(glowAlpha);

        canvas.drawPath(path, paint);

        // Draw the second view
        int secondHeight = height - 10;

        path.reset();
        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(width, secondHeight);
        path.lineTo(12, secondHeight - 120);
        path.lineTo(12, 0);
        path.close();

        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(10, 0, 0, shadowColor);

        canvas.drawPath(path, paint);

        // Schedule a redraw with a delay to create the glowing effect
        handler.postDelayed(() -> {
            glowAlpha = (glowAlpha + 1) % 256;
            invalidate();
        }, 20);
    }




}
