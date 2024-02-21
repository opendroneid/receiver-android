package org.opendroneid.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import org.opendroneid.android.R;
import org.opendroneid.android.app.network.manager.UserManager;

public class BoxBottomLeftView extends CustomGlowView {

    private Paint paint;
    private Path path;

    private Drawable urlIcon;
    private float iconUlrLeft;
    private float iconUrlTop;
    private float iconUrlRight;
    private float iconUrlBottom;
    private BoxBottomLeftView.IconUrlClickListener iconUrlClickListener;
    private UserManager userManager;
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

        userManager = new UserManager(getContext());

        urlIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_add_link);

        startGlowEffect(() -> glowAlpha = (glowAlpha + 1) % 256);
    }

    @Override
    protected void onCustomDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int shadowColor = getResources().getColor(R.color.paleSky);
        int iconTextSpacing = getResources().getDimensionPixelSize(R.dimen.icon_text_spacing);

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

        int centerY = height / 3;
        int totalIconHeight = urlIcon.getIntrinsicHeight() + iconTextSpacing + urlIcon.getIntrinsicHeight();

        iconUrlTop = centerY - (float) totalIconHeight / 4;

        Paint textPaint = new Paint();

        String token = "";
        try {
            token = userManager.getToken();
        } catch (Exception e) {
            //user is not logged in
        }
        if(token == null){
            // Draw url change icon
            if (urlIcon != null) {
                iconUlrLeft = (float) (width - urlIcon.getIntrinsicWidth()) / 2;
                iconUrlRight = iconUlrLeft + urlIcon.getIntrinsicWidth();
                iconUrlBottom = iconUrlTop + urlIcon.getIntrinsicHeight();
                urlIcon.setBounds((int) iconUlrLeft, (int) iconUrlTop, (int) iconUrlRight, (int) iconUrlBottom);
                urlIcon.draw(canvas);

                // Draw text below url icon
                String textUrl= getResources().getString(R.string.menu_change_url);
                textPaint.setColor(getResources().getColor(R.color.overcast));
                textPaint.setTextSize(36);
                float textUrlWidth = textPaint.measureText(textUrl);
                float textUrlX = (width - textUrlWidth) / 2;
                float textUrlY = iconUrlBottom + iconTextSpacing;
                canvas.drawText(textUrl, textUrlX, textUrlY, textPaint);
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
            if (isTouchInsideUrlIcon(x, y)) {
                if (iconUrlClickListener != null) {
                    iconUrlClickListener.onUrlIconClicked();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchInsideUrlIcon(float x, float y) {
        return x >= iconUlrLeft && x <= iconUrlRight && y >= iconUrlTop && y <= iconUrlBottom;
    }

    public void setUrlClickListener(BoxBottomLeftView.IconUrlClickListener listener) {
        this.iconUrlClickListener = listener;
    }

    public interface IconUrlClickListener {
        void onUrlIconClicked();
    }
}