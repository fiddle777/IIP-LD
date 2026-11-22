package com.example.iip_ld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.ToneGenerator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class ShapeView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ToneGenerator tones;

    private boolean showRectangle;
    private float shapeSize = 0.35f;
    private boolean fingerDown;
    private boolean usedTwoFingers;
    private boolean drawingCircle;
    private float swipeStartX;
    private float circleX;
    private float circleY;
    private float diameter;
    private Toast resultToast;

    public ShapeView(Context context, ToneGenerator tones) {
        super(context);
        this.tones = tones;
        setBackgroundColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4 * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawingCircle) {
            paint.setColor(Color.BLUE);
            canvas.drawCircle(circleX, circleY, diameter / 2, paint);
        } else if (!fingerDown) {
            paint.setColor(Color.BLACK);
            float x = getWidth() / 2f;
            float y = getHeight() / 2f;
            float width = shapeWidth();
            if (showRectangle) {
                canvas.drawRect(x - width / 2, y - width / 4,
                        x + width / 2, y + width / 4, paint);
            } else {
                canvas.drawLine(x - width / 2, y, x + width / 2, y, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                fingerDown = true;
                usedTwoFingers = false;
                if (resultToast != null) {
                    resultToast.cancel();
                }
                swipeStartX = event.getX();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (!usedTwoFingers) {
                    usedTwoFingers = true;
                    drawingCircle = true;
                    updateCircle(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (drawingCircle) {
                    updateCircle(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (drawingCircle && event.getActionIndex() < 2) {
                    // The lifted finger still has a position in this event.
                    updateCircle(event);
                    drawingCircle = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (usedTwoFingers) {
                    showResult();
                } else if (Math.abs(event.getX() - swipeStartX) > getWidth() / 8f) {
                    showRectangle = !showRectangle;
                    shapeSize = 0.25f + (float) Math.random() * 0.25f;
                } else {
                    performClick();
                }
                fingerDown = false;
                drawingCircle = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                fingerDown = false;
                drawingCircle = false;
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private float shapeWidth() {
        return Math.min(getWidth(), getHeight()) * shapeSize;
    }

    private void updateCircle(MotionEvent event) {
        float firstX = event.getX(0);
        float firstY = event.getY(0);
        float secondX = event.getX(1);
        float secondY = event.getY(1);
        circleX = (firstX + secondX) / 2;
        circleY = (firstY + secondY) / 2;
        diameter = (float) Math.hypot(secondX - firstX, secondY - firstY);
    }

    private void showResult() {
        float target = shapeWidth();
        if (showRectangle) {
            // The rectangle fits in a circle whose diameter is its diagonal.
            target = (float) Math.hypot(target, target / 2);
        }
        boolean success = Math.abs(diameter - target) <= target * 0.15f;
        int message = success ? R.string.shape_match : R.string.shape_miss;
        resultToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        resultToast.show();
        tones.startTone(success ? ToneGenerator.TONE_PROP_ACK : ToneGenerator.TONE_PROP_NACK);
    }
}
