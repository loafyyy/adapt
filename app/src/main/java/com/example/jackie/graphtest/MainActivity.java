package com.example.jackie.graphtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Views
    private EditText yEntry;
    private Button addButton;
    private Button resetButton;
    private GraphView graph;

    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
    private List<DataPoint> data = new LinkedList<>();

    private String errorMessage = "entry must be numeric";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // find the views
        graph = (GraphView) findViewById(R.id.graph_view);
        yEntry = (EditText) findViewById(R.id.y_entry);
        addButton = (Button) findViewById(R.id.add_button);

        // set parameters for views
        setCloseEditTextOnEnter(yEntry);

        // create boundaries for graph
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date min = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date max = cal.getTime();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get x value as a date
                Calendar curr = Calendar.getInstance();
                Date x = curr.getTime();

                // get y value
                double y;
                try {
                    y = Double.parseDouble(yEntry.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                //add data point
                data.add(new DataPoint(x, y));
                DataPoint[] dataArr = new DataPoint[data.size()];
                dataArr = data.toArray(dataArr);
                series.resetData(dataArr);
                yEntry.setText("");

            }
        });

        resetButton = (Button) findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reset data with empty array
                series.resetData(new DataPoint[0]);
                data.clear();
                yEntry.setText("");
            }
        });

        // add series to the graph
        graph.addSeries(series);

        // format series appearance
        series.setColor(Color.rgb(84, 199, 128));
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(5);
        series.setDrawBackground(true);
        series.setBackgroundColor(Color.argb(127, 101, 240, 154));

        // format graph labels
        SimpleDateFormat hourFormat = new SimpleDateFormat("hh a");
        graph.getGridLabelRenderer().setLabelFormatter(
                new DateAsXAxisLabelFormatter(graph.getContext(), hourFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);
        graph.setTitle("Blood Glucose Levels");

        // set x and y axis size
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(150);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(min.getTime());
        graph.getViewport().setMaxX(max.getTime());

        // enable scaling and scrolling
        graph.getViewport().setScalableY(true);
        graph.getGridLabelRenderer().setHumanRounding(false);
    }

    // allows keyboard to close when enter is pressed
    private void setCloseEditTextOnEnter(EditText editText) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && textView != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });
    }
}
