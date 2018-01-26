package com.example.jackie.graphtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DateFormat;
import java.util.Arrays;
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

    private PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
    private List<DataPoint> data = new LinkedList<>();

    private String errorMessage = "entry must be numeric";
    private Context mContext;

    // high glucose level - user will get notification
    private double glucoseHigh = 130;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // find the views
        graph = (GraphView) findViewById(R.id.graph_view);
        //initGraph(graph);
        yEntry = (EditText) findViewById(R.id.y_entry);
        setCloseEditTextOnEnter(yEntry);
        addButton = (Button) findViewById(R.id.add_button);

        // handle adding new data points
        final Calendar calendar = Calendar.getInstance();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get x and y values from edit text
                Date x = calendar.getTime();
                double y;
                try {
                    y = Double.parseDouble(yEntry.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                data.add(new DataPoint(x, y));
                DataPoint[] dataArr = new DataPoint[data.size()];
                dataArr = data.toArray(dataArr);
//                if (dataArr.length > 1) {
//                    Arrays.sort(dataArr);
//                }
                series.resetData(dataArr);
                yEntry.setText("");

                // create notification if glucose is too high
                if (y >= glucoseHigh) {
                    createNotification();
                }

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


        // FORMATTING
        final DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance();
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mContext));


        // set x and y axis size
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(150);

        graph.getViewport().setXAxisBoundsManual(false);
//        graph.getViewport().setMinX(0);
//        graph.getViewport().setMaxX(10);

        // enable scaling and scrolling
        graph.getViewport().setScalableY(true);
    }

    // notification for when glucose is too high
    private void createNotification() {

        // create notification
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.error)
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentText(getResources().getString(R.string.notification_message));

        // add vibration
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        // opening notification goes to MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // associate notification builder with pending intent
        mBuilder.setContentIntent(pendingIntent);

        // build notification
        int mNotificationId = 1;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, mBuilder.build());
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
