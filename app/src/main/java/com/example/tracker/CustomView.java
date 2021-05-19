package com.example.tracker;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomView extends View implements ValueAnimator.AnimatorUpdateListener {

    // create the variable for my custom view
    Paint linePainter, circlePainter, guidePainter, yLabelPainter;
    float padding, minLabel, maxLabel, x, y, radius, fraction, yLabelWidth;
    List<Float> series; // list of my number of speed
    Path path;
    ValueAnimator animator;

    // get the location Tbl
    ArrayList<Float> tbls = MainActivity.locationSpeed;

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // create the path
        path = new Path();

        // create the line
        linePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePainter.setStyle(Paint.Style.STROKE);
        linePainter.setStrokeWidth(5f);
        linePainter.setColor(Color.BLACK);

        // create the circle
        circlePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePainter.setStyle(Paint.Style.FILL);
        circlePainter.setColor(Color.BLUE);

        // create the guide
        guidePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        guidePainter.setStyle(Paint.Style.STROKE);
        guidePainter.setStrokeWidth(5f);
        guidePainter.setColor(Color.LTGRAY);

        // create the y label
        yLabelPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        yLabelPainter.setTextSize(30f);
        yLabelPainter.setTextAlign(Paint.Align.RIGHT);

        // padding and radius
        padding = 20f;
        radius = 10f;

        // create the series

        series = new ArrayList<>();
        minLabel = Float.MAX_VALUE;

        for (int i=0; i < tbls.size();i++){
            float num = tbls.get(i);
            series.add(num);
            if (maxLabel<num) maxLabel = num;
            if (num<minLabel) minLabel = num;
        }


        minLabel = (float)0;
        maxLabel = (float)10;

        animator = new ValueAnimator();
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(this);
        animator.setFloatValues(0f,1f);
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight(); // height of my window

        // guide of my graph for easy understanding
        drawGuide(canvas,height - 2*padding, getWidth() - padding);

        float gridHeight = getHeight() - 2*padding; // height of my grid
        float drawnHeight = maxLabel - minLabel;
        float space = (getWidth() - 2 * padding - yLabelWidth)/series.size(); // space between 2 points

        x = yLabelWidth + space;
        y = (height - padding - (series.get(0) - minLabel)*(gridHeight / drawnHeight)) * fraction;
        path.moveTo(x,y);

        // draw the circle and the path
        canvas.drawCircle(x,y,radius,circlePainter);

        for (int i = 1; i < series.size();i++) { // draw each points
            x += space; // space between the point
            // converter of the value y on the graph
            y = (height - padding - (series.get(i) - minLabel)*(gridHeight / drawnHeight))*fraction;
            path.lineTo(x,y); // draw the line
            canvas.drawCircle(x,y,radius,circlePainter); // draw the points
        }
        canvas.drawPath(path,linePainter); // draw the path
    }

    // create the line for easy understanding
    void drawGuide(Canvas canvas, float gridBottom, float gridRight) {
        float labelStep = (maxLabel - minLabel) / 9f; // the label step
        float currentLabel = maxLabel; // the max label
        float space = gridBottom/9f; // the space between the line
        float y;

        for (int i = 0; i < 10; i++){ // draw 10 lines
            y = padding + i*space;

            String yLabel = String.format("%.02f",currentLabel);
            float width = yLabelPainter.measureText(yLabel); // the text
            Rect bound = new Rect();
            yLabelPainter.getTextBounds(yLabel,0,yLabel.length(),bound);

            if (yLabelWidth < width) yLabelWidth = width;

            // draw the number
            canvas.drawText(yLabel,padding + yLabelWidth,y+bound.height()/2,yLabelPainter);
            // draw the line
            canvas.drawLine(padding + yLabelWidth+10f,y,gridRight,y,guidePainter);

            currentLabel -= labelStep;
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        fraction = animation.getAnimatedFraction();
        path.reset();
        invalidate();
    }
}
