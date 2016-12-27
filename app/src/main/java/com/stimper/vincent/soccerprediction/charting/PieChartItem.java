package com.stimper.vincent.soccerprediction.charting;

/**
 * Created by Vincent Stimper on 10.09.16.
 */
public class PieChartItem {

    public String label;
    public float value;
    public int color;

    PieChartItem(String inLabel, float inValue, int inColor) {
        label = inLabel;
        value = inValue;
        color = inColor;
    }

}
