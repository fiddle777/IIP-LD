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
    // Paint = the "brush": colour, line thickness, smooth edges (ANTI_ALIAS).
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ToneGenerator tones; // plays the beeps, given by MainActivity

    private boolean showRectangle;     // false = draw a line, true = draw a rectangle
    private float shapeSize = 0.35f;   // shape size as a part of the screen (0.35 = 35%)
    private boolean fingerDown;        // is at least one finger touching the screen?
    private boolean usedTwoFingers;    // did this gesture use two fingers?
    private boolean drawingCircle;     // are we currently drawing the blue circle?
    private float swipeStartX;         // where the finger first touched (to detect a swipe)
    private float circleX;             // circle centre X
    private float circleY;             // circle centre Y
    private float diameter;            // circle diameter
    private Toast resultToast;         // the "Pataikei!/Nepataikei!" message

    public ShapeView(Context context, ToneGenerator tones) {
        super(context);
        this.tones = tones;
        setBackgroundColor(Color.WHITE);
        // STROKE = draw only the outline, not a filled shape.
        paint.setStyle(Paint.Style.STROKE);
        // Line thickness: 4dp (multiplied by density so it looks the same on every phone).
        paint.setStrokeWidth(4 * getResources().getDisplayMetrics().density);
    }

    // Draws everything on the screen. Runs every time we call invalidate().
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawingCircle) {
            // Two fingers are down: draw only the blue circle (radius = diameter / 2).
            paint.setColor(Color.BLUE);
            canvas.drawCircle(circleX, circleY, diameter / 2, paint);
        } else if (!fingerDown) {
            // No fingers on screen: draw the black shape in the middle.
            paint.setColor(Color.BLACK);
            // Centre of the screen.
            float x = getWidth() / 2f;
            float y = getHeight() / 2f;
            float width = shapeWidth();
            if (showRectangle) {
                // Rectangle that is twice as wide as it is tall, centred on (x, y).
                // drawRect(left, top, right, bottom)
                canvas.drawRect(x - width / 2, y - width / 4,
                        x + width / 2, y + width / 4, paint);
            } else {
                // Horizontal line centred on (x, y). drawLine(startX, startY, endX, endY)
                canvas.drawLine(x - width / 2, y, x + width / 2, y, paint);
            }
        }
    }

    // Called for every touch event. getActionMasked() tells us WHAT happened.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // The FIRST finger touched the screen - a new gesture starts.
                fingerDown = true;
                usedTwoFingers = false;
                // Hide the old result message, if any.
                if (resultToast != null) {
                    resultToast.cancel();
                }
                // Remember where it started, to check later if it was a swipe.
                swipeStartX = event.getX();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // ANOTHER finger touched the screen (now 2 fingers) - start the circle.
                if (!usedTwoFingers) {
                    usedTwoFingers = true;
                    drawingCircle = true;
                    updateCircle(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // Fingers moved - resize/move the circle.
                if (drawingCircle) {
                    updateCircle(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // One of the two fingers was lifted - the circle size is final.
                if (drawingCircle && event.getActionIndex() < 2) {
                    // The lifted finger still has a position in this event.
                    updateCircle(event);
                    drawingCircle = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                // The LAST finger was lifted - the gesture is over. What was it?
                if (usedTwoFingers) {
                    // Two-finger gesture -> check the circle size.
                    showResult();
                } else if (Math.abs(event.getX() - swipeStartX) > getWidth() / 8f) {
                    // Moved more than 1/8 of the screen width sideways -> it's a swipe.
                    // Switch shape and pick a random size between 25% and 50%.
                    showRectangle = !showRectangle;
                    shapeSize = 0.25f + (float) Math.random() * 0.25f;
                } else {
                    // Just a tap.
                    performClick();
                }
                fingerDown = false;
                drawingCircle = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                // The system cancelled the gesture - reset.
                fingerDown = false;
                drawingCircle = false;
                break;
        }
        // Redraw the screen with the new state.
        invalidate();
        // true = "we handled this touch".
        return true;
    }

    // Needed for accessibility when a view handles touches itself.
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    // Shape width in pixels: a part (shapeSize) of the smaller screen side.
    private float shapeWidth() {
        return Math.min(getWidth(), getHeight()) * shapeSize;
    }

    // Calculates the circle from the two finger positions.
    private void updateCircle(MotionEvent event) {
        // Finger 0 and finger 1 positions.
        float firstX = event.getX(0);
        float firstY = event.getY(0);
        float secondX = event.getX(1);
        float secondY = event.getY(1);
        // Centre = the middle point between the two fingers.
        circleX = (firstX + secondX) / 2;
        circleY = (firstY + secondY) / 2;
        // Diameter = distance between the fingers (Pythagoras: hypot = √(dx² + dy²)).
        diameter = (float) Math.hypot(secondX - firstX, secondY - firstY);
    }

    // Compares the circle with the shape and shows the result + beep.
    private void showResult() {
        // For a line, the circle diameter should equal the line length.
        float target = shapeWidth();
        if (showRectangle) {
            // The rectangle fits in a circle whose diameter is its diagonal.
            // Rectangle is width x width/2, so the diagonal = √(width² + (width/2)²).
            target = (float) Math.hypot(target, target / 2);
        }
        // Success if the difference is at most 15% of the target size.
        boolean success = Math.abs(diameter - target) <= target * 0.15f;
        int message = success ? R.string.shape_match : R.string.shape_miss;
        resultToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        resultToast.show();
        // ACK = "good" beep, NACK = "bad" beep.
        tones.startTone(success ? ToneGenerator.TONE_PROP_ACK : ToneGenerator.TONE_PROP_NACK);
    }
}
