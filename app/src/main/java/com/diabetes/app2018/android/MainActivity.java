package com.diabetes.app2018.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    // Views
    private EditText xEntry;
    private EditText yEntry;
    private Button addButton;
    private Button resetButton;
    private Button settingsButton;
    private GraphView graph;
    private Button signOutButton;
    private TextView stepsTV;
    private Button stepsButton;

    // graphing
    private PointsGraphSeries<Point> series = new PointsGraphSeries<>();
    private List<Point> data = new LinkedList<>();
    // high glucose level - user will get notification
    private double glucoseHigh = 130;

    private String errorMessage = "entry must be numeric";
    private Context mContext;

    // Firebase
    private DatabaseReference database;
    private String username;

    // Pedometer/sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private StepDetector stepDetector;
    private boolean pedometerRunning = false;
    private int numSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetector = new StepDetector();
        stepDetector.registerListener(this);

        database = FirebaseDatabase.getInstance().getReference();
        username = "Penn";

        mContext = this;
        final Activity activity = (Activity) mContext;

        // find the views
        stepsTV = (TextView) findViewById(R.id.steps_tv);
        stepsButton = (Button) findViewById(R.id.steps_button);
        graph = (GraphView) findViewById(R.id.graph_view);
        xEntry = (EditText) findViewById(R.id.x_entry);
        setCloseEditTextOnEnter(xEntry);
        yEntry = (EditText) findViewById(R.id.y_entry);
        setCloseEditTextOnEnter(yEntry);
        stepsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pedometerRunning) {
                    startPedometer();
                    stepsButton.setText("Stop Steps");
                } else {
                    stopPedometer();
                    stepsButton.setText("Start Steps");
                }
            }
        });
        addButton = (Button) findViewById(R.id.add_button);
        settingsButton = (Button) findViewById(R.id.settings_button);
        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, SettingsActivity.class);
                startActivity(i);
            }
        });

        // handle adding new data points
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get x and y values from edit text
                double x;
                try {
                    x = Double.parseDouble(xEntry.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                double y;
                try {
                    y = Double.parseDouble(yEntry.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                data.add(new Point(x, y));
                database.child("users").child(username).setValue(x + ", " + y);

                Point[] dataArr = new Point[data.size()];
                dataArr = data.toArray(dataArr);
                if (dataArr.length > 1) {
                    Arrays.sort(dataArr);
                }
                series.resetData(dataArr);
                xEntry.setText("");
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
                series.resetData(new Point[0]);
                data.clear();
                xEntry.setText("");
                yEntry.setText("");
            }
        });

        // add series to the graph
        graph.addSeries(series);


        // FORMATTING
        // set x and y axis size
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(150);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
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

    private void startPedometer() {
        numSteps = 0;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        pedometerRunning = true;
    }

    private void stopPedometer() {
        sensorManager.unregisterListener(this);
        pedometerRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            stepDetector.updateAccel(sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        stepsTV.setText(String.valueOf(numSteps));
    }
}