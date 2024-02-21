package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.opendroneid.android.R;
import org.opendroneid.android.app.network.manager.UserManager;

public class BoxBottomRightView extends CustomGlowView {

    private Paint paint;
    private Path path;
    private Drawable logOutIcon;
    private float iconLogOutLeft;
    private float iconLogOutTop;
    private float iconLogOutRight;
    private float iconLogOutBottom;

    private Drawable aboutIcon;
    private float iconAboutLeft;
    private float iconAboutTop;
    private float iconAboutRight;
    private float iconAboutBottom;
    private IconAboutClickListener iconAboutClickListener;
    private IconLogOutClickListener iconLogOutClickListener;

    private UserManager userManager;
    private int glowAlpha = 255;

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

        logOutIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_logout);
        aboutIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_about);

        userManager = new UserManager(getContext());

        startGlowEffect(() -> glowAlpha = (glowAlpha + 1) % 256);
    }

    @Override
    protected void onCustomDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int iconTextSpacing = getResources().getDimensionPixelSize(R.dimen.icon_text_spacing);
        int iconMargin = getResources().getDimensionPixelSize(R.dimen.icon_margin);
        int shadowColor = getResources().getColor(R.color.paleSky); // Darker color for the shadow

        // Draw first view
        path.reset();
        path.moveTo(0, height - 10);
        path.lineTo(width, height - 10);
        path.lineTo(width, 0);
        path.lineTo(0.0f, 120 - 12);
        path.close();

        paint.setColor(getResources().getColor(R.color.green));
        paint.setStyle(Paint.Style.FILL);

        paint.setAlpha(glowAlpha);
        canvas.drawPath(path, paint);

        // Draw the second view
        int secondWidth = width - 12;

        path.reset();
        path.moveTo(width - secondWidth, 120);
        path.lineTo(width - secondWidth, height);
        path.lineTo(width, height);
        path.lineTo(width, 10);
        path.close();

        paint.setColor(getResources().getColor(R.color.midnight));
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(10, 0, 0, shadowColor);

        canvas.drawPath(path, paint);

        int centerY = height / 3;
        int totalIconHeight = aboutIcon.getIntrinsicHeight() + iconTextSpacing + logOutIcon.getIntrinsicHeight();

        iconAboutTop = centerY - (float) totalIconHeight / 4;

        Paint textPaint = new Paint();

        // Draw about icon
        if (aboutIcon != null) {
            iconAboutLeft = (float) (width - aboutIcon.getIntrinsicWidth()) / 2;
            iconAboutRight = iconAboutLeft + aboutIcon.getIntrinsicWidth();
            iconAboutBottom = iconAboutTop + aboutIcon.getIntrinsicHeight();
            aboutIcon.setBounds((int) iconAboutLeft, (int) iconAboutTop, (int) iconAboutRight, (int) iconAboutBottom);
            aboutIcon.draw(canvas);

            // Draw text below about icon
            String textAbout= getResources().getString(R.string.menu_about);
            textPaint.setColor(getResources().getColor(R.color.overcast));
            textPaint.setTextSize(36);
            float textAboutWidth = textPaint.measureText(textAbout);
            float textAboutX = (width - textAboutWidth) / 2;
            float textAboutY = iconAboutBottom + iconTextSpacing;
            canvas.drawText(textAbout, textAboutX, textAboutY, textPaint);
        }

        iconLogOutTop = iconAboutBottom + iconMargin;

        String token = "";
        try {
            token = userManager.getToken();
        } catch (Exception e) {
            Toast.makeText(getContext(), getResources().getString(R.string.error_log_out), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if(token != null && !token.equals("")){
            // Draw logout icon
            if (logOutIcon != null) {
                iconLogOutLeft = (float) (width - logOutIcon.getIntrinsicWidth()) / 2;
                iconLogOutRight = iconLogOutLeft + logOutIcon.getIntrinsicWidth();
                iconLogOutBottom = iconLogOutTop + logOutIcon.getIntrinsicHeight();
                logOutIcon.setBounds((int) iconLogOutLeft, (int) iconLogOutTop, (int) iconLogOutRight, (int) iconLogOutBottom);

                logOutIcon.draw(canvas);

                // Draw text below logout icon
                String textLogOut = getResources().getString(R.string.menu_log_out);
                float textLogOutWidth = textPaint.measureText(textLogOut);
                float textLogOutX = (width - textLogOutWidth) / 2;
                float textLogOutY = iconLogOutBottom + iconTextSpacing;
                textPaint.setColor(getResources().getColor(R.color.overcast));

                canvas.drawText(textLogOut, textLogOutX, textLogOutY, textPaint);
            }
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
            if (isTouchInsideAboutIcon(x, y)) {
                if (iconAboutClickListener != null) {
                    iconAboutClickListener.onAboutIconClicked();
                    return true;
                }
            } else if (isTouchInsideUserIcon(x, y)) {
                if (iconLogOutClickListener != null) {
                    iconLogOutClickListener.onLogOutIconClicked();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchInsideAboutIcon(float x, float y) {
        return x >= iconAboutLeft && x <= iconAboutRight && y >= iconAboutTop && y <= iconAboutBottom;
    }

    private boolean isTouchInsideUserIcon(float x, float y) {
        return x >= iconLogOutLeft && x <= iconLogOutRight && y >= iconLogOutTop && y <= iconLogOutBottom;
    }

    public void setAboutIconClickListener(IconAboutClickListener listener) {
        this.iconAboutClickListener = listener;
    }

    public void setLogOutIconClickListener(IconLogOutClickListener listener) {
        this.iconLogOutClickListener = listener;
    }

    public interface IconAboutClickListener {
        void onAboutIconClicked();
    }

    public interface IconLogOutClickListener {
        void onLogOutIconClicked();
    }

}
