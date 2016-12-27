package com.stimper.vincent.soccerprediction.charting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Vincent Stimper on 10.09.16.
 */
public class PieChart extends View {

    /* Helper attributes */
    private float paddingShare = 1; // Angle of the chart
    private float totalValue = 0; // Sum of the value of the items
    private float labelOpacity = 0;
    private ArrayList<PieChartItem> items = new ArrayList<PieChartItem>();


    /* Constructor */
    public PieChart(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* Getter and setter methods */
    public float getPaddingShare() {
        return paddingShare;
    }

    public void setPaddingShare(float inPaddingShare) {
        paddingShare = inPaddingShare;
        this.invalidate();
    }

    public float getLabelOpacity() {
        return labelOpacity;
    }

    public void setLabelOpacity(float inLabelOpacity) {
        labelOpacity = inLabelOpacity;
        this.invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float canvasLength = (float) canvas.getWidth(); // height == width
        Paint paintPie = new Paint();
        paintPie.setStyle(Paint.Style.FILL_AND_STROKE);
        paintPie.setStrokeWidth(1.5f);
        Paint paintFont = new Paint();
        paintFont.setTextAlign(Paint.Align.CENTER);
        paintFont.setColor(((int) Math.round(255 * labelOpacity)) * 0x1000000);
        paintFont.setTextSize(canvasLength / 190 * 15);
        float startAngle = -90;
        for (int i = 0; i < items.size(); i++) {
            PieChartItem item = items.get(i);
            float sweepAngle = item.value / totalValue * 360;
            paintPie.setColor(item.color);
            canvas.drawArc(new RectF(canvasLength / 2 * paddingShare, canvasLength / 2 * paddingShare, canvasLength * (1 - paddingShare), canvasLength * (1 - paddingShare)), startAngle, sweepAngle, true, paintPie);
            canvas.drawText(item.label, 0.5f * canvasLength * (1 + (1 - paddingShare) * ((float) Math.cos((startAngle + sweepAngle * 0.5f) * Math.PI / 180))) - paintFont.measureText(item.label),
                            0.5f * canvasLength * (1 + (1 - paddingShare) * ((float) Math.sin((startAngle + sweepAngle * 0.5f) * Math.PI / 180))) + (paintFont.descent() + paintFont.ascent()) / 2, paintFont);
            startAngle += sweepAngle;
        }
    }

    public void addItem(String label, double value, int color) {
        items.add(new PieChartItem(label, (float) value, color));
        totalValue += (float) value;
    }

    public void removeAllItems() {
        items = new ArrayList<PieChartItem>();
        totalValue = 0;
    }

}
