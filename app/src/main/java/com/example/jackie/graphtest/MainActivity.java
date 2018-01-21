package com.example.jackie.graphtest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Views
    EditText xEntry;
    EditText yEntry;
    Button addButton;
    Button resetButton;
    GraphView graph;

    EditText etMessage;
    EditText etTelNr;
    Button smsButton;


    PointsGraphSeries<Point> series = new PointsGraphSeries<>();
    List<Point> data = new LinkedList<>();

    String errorMessage = "entry must be numeric";
    Context mContext;
    int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        final Activity activity = (Activity) mContext;

        // find the views
        graph = (GraphView) findViewById(R.id.graph_view);
        xEntry = (EditText) findViewById(R.id.x_entry);
        setCloseEditTextOnEnter(xEntry);
        yEntry = (EditText) findViewById(R.id.y_entry);
        setCloseEditTextOnEnter(yEntry);
        addButton = (Button) findViewById(R.id.add_button);
        smsButton = (Button) findViewById(R.id.sendSMS);

        etMessage = (EditText) findViewById(R.id.etMessage);
        etTelNr = (EditText) findViewById(R.id.etTelNr);

        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

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
                Point[] dataArr = new Point[data.size()];
                dataArr = data.toArray(dataArr);
                if (dataArr.length > 1) {
                    Arrays.sort(dataArr);
                }
                series.resetData(dataArr);
                xEntry.setText("");
                yEntry.setText("");
            }
        });

        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString();
                String telNr = etTelNr.getText().toString();

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(telNr, null, message, sentPI, deliveredPI);
                }
            }
        }) ;

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
        graph.getViewport().setMaxY(10);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS sent!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "Generic failure!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "No service!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "Null PDU!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.this, "Radio off!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS delivered!", Toast.LENGTH_SHORT).show();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS not delivered!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSentReceiver);
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
