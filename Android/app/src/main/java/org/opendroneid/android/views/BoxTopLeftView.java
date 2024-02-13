package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import org.opendroneid.android.R;

public class BoxTopLeftView extends View {

    private final Handler handler = new Handler();
    private Paint paint;
    private Path path;
    private Drawable firstIcon;
    private Drawable secondIcon;
    private IconFirstClickListener iconFirstClickListener;
    private IconSecondClickListener iconSecondClickListener;
    private float iconFirstLeft;
    private float iconFirstTop;
    private float iconFirstRight;
    private float iconFirstBottom;
    private float iconSecondLeft;
    private float iconSecondTop;
    private float iconSecondRight;
    private float iconSecondBottom;
    private int glowAlpha = 255;

    public BoxTopLeftView(Context context) {
        super(context);
        init();
    }

    public BoxTopLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoxTopLeftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        // Fetch icons from resources
        firstIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_home);
        secondIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_user);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int iconTextSpacing = getResources().getDimensionPixelSize(R.dimen.icon_text_spacing);
        int iconMargin = getResources().getDimensionPixelSize(R.dimen.icon_margin);
        int shadowColor = getResources().getColor(R.color.paleSky);

        //Draw first view
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(width, height - 120);
        path.lineTo(0.0f, height);
        path.lineTo(0, height);
        path.close();

        paint.setColor(getResources().getColor(R.color.green));
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(glowAlpha);

        canvas.drawPath(path, paint);

        // Draw the second view
        int secondWidth = width - 12;
        int secondHeight = height - 10;

        path.reset();
        path.moveTo(0, 0);
        path.lineTo(secondWidth, 0);
        path.lineTo(secondWidth, secondHeight - 120);
        path.lineTo(0.0f, secondHeight);
        path.lineTo(0, secondHeight);
        path.close();

        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(10, 0, 0, shadowColor);

        canvas.drawPath(path, paint);

        int centerY = height / 3;
        int totalIconHeight = firstIcon.getIntrinsicHeight() + iconTextSpacing + secondIcon.getIntrinsicHeight();

        iconFirstTop = centerY - totalIconHeight / 2;

        Paint textPaint = new Paint();

        // Draw first icon
        if (firstIcon != null) {
            iconFirstLeft = (width - firstIcon.getIntrinsicWidth()) / 2;
            iconFirstRight = iconFirstLeft + firstIcon.getIntrinsicWidth();
            iconFirstBottom = iconFirstTop + firstIcon.getIntrinsicHeight();
            firstIcon.setBounds((int) iconFirstLeft, (int) iconFirstTop, (int) iconFirstRight, (int) iconFirstBottom);
            firstIcon.draw(canvas);

            // Draw text below first icon
            String text1 = "Home";
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(36);
            float text1Width = textPaint.measureText(text1);
            float text1X = (width - text1Width) / 2;
            float text1Y = iconFirstBottom + iconTextSpacing; // Place text below first icon
            canvas.drawText(text1, text1X, text1Y, textPaint);
        }

        // Calculate the top position for the second icon
        iconSecondTop = iconFirstBottom + iconMargin;

        // Draw second icon
        if (secondIcon != null) {
            iconSecondLeft = (width - secondIcon.getIntrinsicWidth()) / 2;
            iconSecondRight = iconSecondLeft + secondIcon.getIntrinsicWidth();
            iconSecondBottom = iconSecondTop + secondIcon.getIntrinsicHeight();
            secondIcon.setBounds((int) iconSecondLeft, (int) iconSecondTop, (int) iconSecondRight, (int) iconSecondBottom);
            secondIcon.draw(canvas);

            // Draw text below second icon
            String text2 = "User";
            float text2Width = textPaint.measureText(text2);
            float text2X = (width - text2Width) / 2;
            float text2Y = iconSecondBottom + iconTextSpacing; // Place text below second icon
            canvas.drawText(text2, text2X, text2Y, textPaint);
        }

        // Schedule a redraw with a delay to create the glowing effect
        handler.postDelayed(() -> {
            glowAlpha = (glowAlpha + 1) % 256;
            invalidate();
        }, 20);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            if (isTouchInsideIcon1(x, y)) {
                if (iconFirstClickListener != null) {
                    iconFirstClickListener.onIcon1Clicked();
                    return true; // Event consumed
                }
            } else if (isTouchInsideIcon2(x, y)) {
                if (iconSecondClickListener != null) {
                    iconSecondClickListener.onIcon2Clicked();
                    return true; // Event consumed
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchInsideIcon1(float x, float y) {
        // Determine if the touch event is inside the bounds of icon1
        return x >= iconFirstLeft && x <= iconFirstRight && y >= iconFirstTop && y <= iconFirstBottom;
    }

    private boolean isTouchInsideIcon2(float x, float y) {
        // Determine if the touch event is inside the bounds of icon2
        return x >= iconSecondLeft && x <= iconSecondRight && y >= iconSecondTop && y <= iconSecondBottom;
    }

    public void setIcon1ClickListener(IconFirstClickListener listener) {
        this.iconFirstClickListener = listener;
    }

    public void setIcon2ClickListener(IconSecondClickListener listener) {
        this.iconSecondClickListener = listener;
    }

    public interface IconFirstClickListener {
        void onIcon1Clicked();
    }

    public interface IconSecondClickListener {
        void onIcon2Clicked();
    }
}
