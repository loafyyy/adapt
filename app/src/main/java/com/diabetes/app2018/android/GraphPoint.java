package com.diabetes.app2018.android;

import java.util.Date;

/**
 * Created by Jackie on 2018-04-12.
 */

public class GraphPoint {

    private Date date;
    private double y;
    private String note;

    // required empty constructor for database
    public GraphPoint() {

    }

    public GraphPoint(Date date, double y, String note) {
        this.date = date;
        this.y = y;
        this.note = note;
    }

    public Date getDate() {
        return this.date;
    }

    public double getY() {
        return this.y;
    }

    public String getNote() {
        return this.note;
    }
}
