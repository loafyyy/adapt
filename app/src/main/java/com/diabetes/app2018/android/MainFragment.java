package com.diabetes.app2018.android;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class MainFragment extends Fragment {

    // Views
    private EditText xEntry;
    private EditText yEntry;
    private Button addButton;
    private Button resetButton;
    private GraphView graph;
    private TextView stepsTV;
    private Button stepsButton;

    // BroadcastReceiver for PedometerService
    public static final String RECEIVE_SERVICE = "com.diabetes.app2018.android.RECEIVE_SERVICE";
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVE_SERVICE)) {
                int numSteps = intent.getIntExtra("numSteps", -1);
                stepsTV.setText(String.valueOf(numSteps));
            }
        }
    };
    private LocalBroadcastManager bManager;
    private boolean pedometerRunning = false;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // graphing
    private PointsGraphSeries<Point> series = new PointsGraphSeries<>();
    private List<Point> data = new LinkedList<>();
    // high glucose level - user will get notification
    private double glucoseHigh = 130;
    private String errorMessage = "entry must be numeric";

    // Firebase
    private DatabaseReference database;
    private String username;

    // Important
    private Context mContext;
    private MainActivity mainActivity;

    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mainActivity = (MainActivity) getActivity();

        // set up broadcast receiver
        bManager = LocalBroadcastManager.getInstance(mContext);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_SERVICE);
        bManager.registerReceiver(bReceiver, intentFilter);

        database = FirebaseDatabase.getInstance().getReference();
        username = "Penn";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // find the views
        stepsTV = (TextView) view.findViewById(R.id.steps_tv);
        stepsButton = (Button) view.findViewById(R.id.steps_button);
        stepsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pedometerRunning) {
                    startPedometer();
                    pedometerRunning = true;
                    stepsButton.setText("Stop Steps");
                } else {
                    stopPedometer();
                    pedometerRunning = false;
                    stepsButton.setText("Start Steps");
                }
            }
        });
        if (isMyServiceRunning(PedometerService.class)) {
            pedometerRunning = true;
            stepsButton.setText("Stop Steps");
        }
        graph = (GraphView) view.findViewById(R.id.graph_view);
        xEntry = (EditText) view.findViewById(R.id.x_entry);
        setCloseEditTextOnEnter(xEntry);
        yEntry = (EditText) view.findViewById(R.id.y_entry);
        setCloseEditTextOnEnter(yEntry);
        addButton = (Button) view.findViewById(R.id.add_button);

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

        resetButton = (Button) view.findViewById(R.id.reset_button);
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

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

    // notification for when glucose is too high
    private void createNotification() {

        // create notification
        Notification.Builder mBuilder =
                new Notification.Builder(mContext)
                        .setSmallIcon(R.drawable.error)
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentText(getResources().getString(R.string.notification_message));

        // add vibration
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        // opening notification goes to MainActivity
        Intent intent = new Intent(mContext, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // associate notification builder with pending intent
        mBuilder.setContentIntent(pendingIntent);

        // build notification
        int mNotificationId = 1;
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, mBuilder.build());
    }

    // allows keyboard to close when enter is pressed
    private void setCloseEditTextOnEnter(EditText editText) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && textView != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });
    }

    private void startPedometer() {
        Intent intent = new Intent(mContext, PedometerService.class);
        mContext.startService(intent);
    }

    private void stopPedometer() {
        Intent intent = new Intent(mContext, PedometerService.class);
        mContext.stopService(intent);
        // todo save number of steps
    }
}
