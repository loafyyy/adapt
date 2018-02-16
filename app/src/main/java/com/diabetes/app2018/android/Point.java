package com.diabetes.app2018.android;

import android.support.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Jackie on 2017-12-06.
 */

// helps sort the data points in order based on x value
public class Point extends DataPoint implements Comparable {

    public Point(double x, double y) {
        super(x, y);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return Double.compare(this.getX(), ((Point) o).getX());
    }
}
