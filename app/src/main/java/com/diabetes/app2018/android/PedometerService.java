package com.diabetes.app2018.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jackie on 2018-03-10.
 */

public class PedometerService extends Service implements SensorEventListener, StepListener {

    // Pedometer/sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private StepDetector stepDetector;
    private boolean pedometerRunning = false;
    private int numSteps;

    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("PedometerService", "onCreate");
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

        // sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetector = new StepDetector();
        stepDetector.registerListener(this);
        numSteps = 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("PedometerService", "onStartCommand");
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        pedometerRunning = true;
        startForeground(NOTIFICATION_ID, createNotification());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("PedometerService", "onDestroy");
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        sensorManager.unregisterListener(this);
        pedometerRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && pedometerRunning) {
            stepDetector.updateAccel(sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

            // send broadcast with updated numSteps
            Intent intent = new Intent(MainActivity.RECEIVE_SERVICE);
            intent.putExtra("numSteps", numSteps);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;
    }

    private Notification createNotification() {

        // create notification
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.error)
                        .setContentTitle("PedometerService")
                        .setContentText("Notification");

        // opening notification goes to MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        101,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // associate notification builder with pending intent
        mBuilder.setContentIntent(pendingIntent);

        // build notification
        return mBuilder.build();
    }
}
