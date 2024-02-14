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
    private Drawable homeIcon;
    private Drawable userIcon;
    private IconHomeClickListener iconHomeClickListener;
    private IconUserClickListener iconUserClickListener;
    private float iconHomeLeft;
    private float iconHomeTop;
    private float iconHomeRight;
    private float iconHomeBottom;
    private float iconUserLeft;
    private float iconUserTop;
    private float iconUserRight;
    private float iconUserBottom;
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
        homeIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_home);
        userIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_user);
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
        int totalIconHeight = homeIcon.getIntrinsicHeight() + iconTextSpacing + userIcon.getIntrinsicHeight();

        iconHomeTop = centerY - (float) totalIconHeight / 2;

        Paint textPaint = new Paint();

        // Draw home icon
        if (homeIcon != null) {
            iconHomeLeft = (float) (width - homeIcon.getIntrinsicWidth()) / 2;
            iconHomeRight = iconHomeLeft + homeIcon.getIntrinsicWidth();
            iconHomeBottom = iconHomeTop + homeIcon.getIntrinsicHeight();
            homeIcon.setBounds((int) iconHomeLeft, (int) iconHomeTop, (int) iconHomeRight, (int) iconHomeBottom);
            homeIcon.draw(canvas);

            // Draw text below home icon
            String textHome = getResources().getString(R.string.menu_home);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(36);
            float textHomeWidth = textPaint.measureText(textHome);
            float textHomeX = (width - textHomeWidth) / 2;
            float textHomeY = iconHomeBottom + iconTextSpacing; // Place text below first icon
            canvas.drawText(textHome, textHomeX, textHomeY, textPaint);
        }

        // Calculate the top position for the user icon
        iconUserTop = iconHomeBottom + iconMargin;

        // Draw user icon
        if (userIcon != null) {
            iconUserLeft = (float) (width - userIcon.getIntrinsicWidth()) / 2;
            iconUserRight = iconUserLeft + userIcon.getIntrinsicWidth();
            iconUserBottom = iconUserTop + userIcon.getIntrinsicHeight();
            userIcon.setBounds((int) iconUserLeft, (int) iconUserTop, (int) iconUserRight, (int) iconUserBottom);
            userIcon.draw(canvas);

            // Draw text below user icon
            String textUser = getResources().getString(R.string.menu_user);
            float textUserWidth = textPaint.measureText(textUser);
            float textUserX = (width - textUserWidth) / 2;
            float textUserY = iconUserBottom + iconTextSpacing; // Place text below second icon
            canvas.drawText(textUser, textUserX, textUserY, textPaint);
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
            if (isTouchInsideHomeIcon(x, y)) {
                if (iconHomeClickListener != null) {
                    iconHomeClickListener.onHomeIconClicked();
                    return true; // Event consumed
                }
            } else if (isTouchInsideUserIcon(x, y)) {
                if (iconUserClickListener != null) {
                    iconUserClickListener.onUserIconClicked();
                    return true; // Event consumed
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchInsideHomeIcon(float x, float y) {
        // Determine if the touch event is inside the bounds of home icon
        return x >= iconHomeLeft && x <= iconHomeRight && y >= iconHomeTop && y <= iconHomeBottom;
    }

    private boolean isTouchInsideUserIcon(float x, float y) {
        // Determine if the touch event is inside the bounds of user icon
        return x >= iconUserLeft && x <= iconUserRight && y >= iconUserTop && y <= iconUserBottom;
    }

    public void setHomeIconClickListener(IconHomeClickListener listener) {
        this.iconHomeClickListener = listener;
    }

    public void setUserIconClickListener(IconUserClickListener listener) {
        this.iconUserClickListener = listener;
    }

    public interface IconHomeClickListener {
        void onHomeIconClicked();
    }

    public interface IconUserClickListener {
        void onUserIconClicked();
    }
}
