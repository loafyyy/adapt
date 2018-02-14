package com.example.jackie.graphtest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Views
    private EditText yEntry;
    private Button addButton;
    private Button resetButton;
    private GraphView graph;
    private Spinner timeSpinner;

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
        timeSpinner = (Spinner) findViewById(R.id.time_spinner);

        // close keyboard on enter
        setCloseEditTextOnEnter(yEntry);

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

                // add data point to graph
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

        // time spinner
        List<String> spinnerOptions = new ArrayList<>();
        spinnerOptions.add("Day");
        spinnerOptions.add("Week");
        spinnerOptions.add("Month");
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                final Calendar cal = Calendar.getInstance();

                Date min;
                Date max;

                DateFormat hourFormat;

                // day view
                if (i == 0) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    // set x from 12am to 12am of next day
                    min = cal.getTime();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    max = cal.getTime();
                    hourFormat = new SimpleDateFormat("h a");
                    graph.getGridLabelRenderer().setNumHorizontalLabels(2);
                }
                // week view
                else if (i == 1) {
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    min = cal.getTime();
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    max = cal.getTime();
                    hourFormat = new SimpleDateFormat("E");
                    graph.getGridLabelRenderer().setNumHorizontalLabels(7);
                }
                // month view
                else {
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    min = cal.getTime();
                    cal.add(Calendar.MONTH, 1);
                    max = cal.getTime();
                    hourFormat = new SimpleDateFormat("d");
                    graph.getGridLabelRenderer().setNumHorizontalLabels(15);
                }

                graph.getViewport().setXAxisBoundsManual(true);
                graph.getGridLabelRenderer().setHumanRounding(false);
                graph.getViewport().setMinX(min.getTime());
                graph.getViewport().setMaxX(max.getTime());
                LabelFormatter labelFormatter = new DateAsXAxisLabelFormatter(mContext, hourFormat);
                graph.getGridLabelRenderer().setLabelFormatter(labelFormatter);
                graph.getGridLabelRenderer().invalidate(false, false);
                //graph.refreshDrawableState();
                graph.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                graph.getGridLabelRenderer().invalidate(false, false);
                graph.invalidate();
            }
        });

        // add series to the graph
        graph.addSeries(series);

        // format series appearance
        series.setColor(Color.rgb(84, 199, 128));
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setDrawBackground(true);
        series.setBackgroundColor(Color.argb(127, 101, 240, 154));

        // format graph labels

        // set x axis size

        DateFormat hourFormat = new SimpleDateFormat("h a");
        LabelFormatter labelFormatter = new DateAsXAxisLabelFormatter(mContext, hourFormat);
        graph.getGridLabelRenderer().setNumHorizontalLabels(2);

        // create boundaries for graph - 12am to 12am of next day
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date min = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date max = cal.getTime();

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getViewport().setMinX(min.getTime());
        graph.getViewport().setMaxX(max.getTime());
        graph.getGridLabelRenderer().setLabelFormatter(labelFormatter);

        // set y axis size
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        // todo adjust so that it zooms out when higher number is added
        graph.getViewport().setMaxY(150);
        graph.getGridLabelRenderer().setNumVerticalLabels(16);

        // enable scaling and scrolling
        graph.getViewport().setScrollable(false);

        // graph title
        graph.setTitle("Blood Glucose Levels");
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
