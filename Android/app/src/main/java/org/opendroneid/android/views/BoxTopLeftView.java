package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.opendroneid.android.R;
import org.opendroneid.android.app.network.manager.UserManager;

import java.io.IOException;

public class BoxTopLeftView extends CustomGlowView {

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
    private UserManager userManager;
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

        homeIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_home);
        userIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_user);

        userManager = new UserManager(getContext());

        startGlowEffect(() -> glowAlpha = (glowAlpha + 1) % 256);
    }

    @Override
    protected void onCustomDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int iconTextSpacing = getResources().getDimensionPixelSize(R.dimen.icon_text_spacing);
        int iconMargin = getResources().getDimensionPixelSize(R.dimen.icon_margin);
        int shadowColor = getResources().getColor(R.color.paleSky);

        // Draw first view
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

        String token = "";
        try {
            token = userManager.getToken();
        } catch (Exception e) {
            Toast.makeText(getContext(), getResources().getString(R.string.error_sign_in), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Draw home icon
        if (homeIcon != null) {
            iconHomeLeft = (float) (width - homeIcon.getIntrinsicWidth()) / 2;
            iconHomeRight = iconHomeLeft + homeIcon.getIntrinsicWidth();
            iconHomeBottom = iconHomeTop + homeIcon.getIntrinsicHeight();
            homeIcon.setBounds((int) iconHomeLeft, (int) iconHomeTop, (int) iconHomeRight, (int) iconHomeBottom);
            homeIcon.draw(canvas);

            // Draw text below home icon
            String textHome = getResources().getString(R.string.menu_home);
            textPaint.setColor(getResources().getColor(R.color.overcast));
            textPaint.setTextSize(36);
            float textHomeWidth = textPaint.measureText(textHome);
            float textHomeX = (width - textHomeWidth) / 2;
            float textHomeY = iconHomeBottom + iconTextSpacing;
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
            if (token != null && !token.equals("")) {
                userIcon.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN));
            }else{
                userIcon.setColorFilter(null);
            }
            userIcon.draw(canvas);

            // Draw text below user icon
            String textUser = getResources().getString(R.string.menu_user);
            float textUserWidth = textPaint.measureText(textUser);
            float textUserX = (width - textUserWidth) / 2;
            float textUserY = iconUserBottom + iconTextSpacing;
            if (token != null && !token.equals("")) {
                textPaint.setColor(getResources().getColor(R.color.green));
            }
            canvas.drawText(textUser, textUserX, textUserY, textPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onCustomDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            if (isTouchInsideHomeIcon(x, y)) {
                if (iconHomeClickListener != null) {
                    iconHomeClickListener.onHomeIconClicked();
                    return true;
                }
            } else if (isTouchInsideUserIcon(x, y)) {
                if (iconUserClickListener != null) {
                    try {
                        iconUserClickListener.onUserIconClicked();
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchInsideHomeIcon(float x, float y) {
        return x >= iconHomeLeft && x <= iconHomeRight && y >= iconHomeTop && y <= iconHomeBottom;
    }

    private boolean isTouchInsideUserIcon(float x, float y) {
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
        void onUserIconClicked() throws IOException, ClassNotFoundException;
    }
}
