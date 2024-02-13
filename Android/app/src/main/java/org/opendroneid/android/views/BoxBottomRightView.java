package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import org.opendroneid.android.R;

public class BoxBottomRightView extends View {

    private Paint paint;
    private Path path;

    private int glowAlpha = 255;
    private final Handler handler = new Handler();

    public BoxBottomRightView(Context context) {
        super(context);
        init();
    }

    public BoxBottomRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoxBottomRightView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        int shadowColor = getResources().getColor(R.color.paleSky); // Darker color for the shadow

        // Reset path
        path.reset();
        // Move to bottom-left corner instead of top-left
        path.moveTo(0, height - 10);
        // Line to bottom-right corner remains unchanged
        path.lineTo(width, height - 10);
        // Line to the top-right corner is changed
        path.lineTo(width, 0);
        // Line to the top-left corner is changed
        path.lineTo(0.0f, 120 - 12);
        // Close the path
        path.close();

        // Draw shadow view using the same path
        paint.setColor(getResources().getColor(R.color.green));
        paint.setStyle(Paint.Style.FILL);

        // Draw the main view with the glowing effect and shadow
        paint.setAlpha(glowAlpha); // Set the alpha value for the glowing effect
        canvas.drawPath(path, paint);

        int secondWidth = width - 12; // Adjust the width to match the first set
        int secondHeight = height; // Set second view height to be the same as the canvas height

// Reset path
        path.reset();
// Move to top-right corner (with 10 pixels offset from the right)
        path.moveTo(width - secondWidth, 120); // Adjust the starting point to be aligned with the right side
// Line to bottom-left corner (mirrored)
        path.lineTo(width - secondWidth, height); // Adjust the starting point to be aligned with the right side
// Line to the bottom-right corner (mirrored)
        path.lineTo(width, height); // Adjust the endpoint to be aligned with the right side
// Line to the top-right corner (mirrored)
        path.lineTo(width, 10);
// Close the path
        path.close();

        paint.setColor(getResources().getColor(R.color.midnight)); // Set color to transparent
        paint.setStyle(Paint.Style.FILL);
// Apply shadow
        paint.setShadowLayer(10, 0, 0, shadowColor); // Add shadow with 10px radius, no offset

        canvas.drawPath(path, paint); // Draw the second view

        // Schedule a redraw with a delay to create the glowing effect
        handler.postDelayed(() -> {
            glowAlpha = (glowAlpha + 1) % 256; // Increment the alpha value (cyclic from 0 to 255)
            invalidate(); // Redraw the view
        }, 20); // Delay in milliseconds before redrawing
    }

}
