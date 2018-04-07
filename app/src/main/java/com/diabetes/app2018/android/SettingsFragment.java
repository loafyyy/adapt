package com.diabetes.app2018.android;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SettingsFragment extends Fragment {

    private EditText etMessage;
    private EditText etTelNr;
    private Button smsButton;

    private Context mContext;
    private MainActivity mainActivity;

    private int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private PendingIntent sentPI, deliveredPI;
    private BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // find views
        smsButton = (Button) view.findViewById(R.id.sendSMS);
        etMessage = (EditText) view.findViewById(R.id.etMessage);
        etTelNr = (EditText) view.findViewById(R.id.etTelNr);

        sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(mContext, 0, new Intent(DELIVERED), 0);

        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString();
                String telNr = etTelNr.getText().toString();

                if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mainActivity, new String[]{android.Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(telNr, null, message, sentPI, deliveredPI);
                }
            }
        }) ;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(mContext, "SMS sent!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(mContext, "Generic failure!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(mContext, "No service!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(mContext, "Null PDU!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(mContext, "Radio off!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(mContext, "SMS delivered!", Toast.LENGTH_SHORT).show();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(mContext, "SMS not delivered!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        mContext.registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        mContext.registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(smsDeliveredReceiver);
        mContext.unregisterReceiver(smsSentReceiver);
    }
}
