package pl.agh.depressiondetector.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhoneCallService extends Service {
    private static final String TAG = "PhoneCallService";

    private CallReceiver callReceiver;

    private static final String ACTION_IN_CALL = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT_CALL = "android.intent.action.NEW_OUTGOING_CALL";


    private class CallReceiver extends BroadcastReceiver {
        private static final String TAG = "CallReceiver";

        private MediaRecorder mediaRecorder = null;
        private File outputFile;

        private Boolean wasRinging = false;
        private Boolean isRecording = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            switch (intent.getAction()) {
                case ACTION_IN_CALL:
                    if (bundle != null) {
                        String state = bundle.getString(TelephonyManager.EXTRA_STATE);
                        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                            wasRinging = true;

                            // For debugging purposes only
                            // TO BE DELETED in a later state of the project
                            Toast.makeText(context, "Incoming call", Toast.LENGTH_LONG).show();
                        }
                        else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && wasRinging) {
                            // For debugging purposes only
                            // TO BE DELETED in a later state of the project
                            Toast.makeText(context, "Setting up recorder", Toast.LENGTH_LONG).show();
                            try {
                                recordPhoneCall();
                            } catch (IOException e) {
                                Log.e(TAG, "Recording phone call: " + e);
                            }
                        }
                        else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                            wasRinging = false;

                            // For debugging purposes only
                            // TO BE DELETED in a later state of the project
                            Toast.makeText(context, "Rejected or ended", Toast.LENGTH_LONG).show();
                            if (isRecording) {
                                isRecording = false;
                                if (mediaRecorder != null) {
                                    mediaRecorder.stop();
                                }

                                // For debugging purposes only
                                // TO BE DELETED in a later state of the project
                                Toast.makeText(context, "Recorder stopped working", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    break;
                case ACTION_OUT_CALL:
                    // For debugging purposes only
                    // TO BE DELETED in a later state of the project
                    Toast.makeText(context, "Outgoing call", Toast.LENGTH_LONG).show();
                    // TODO: recording on outgoing calls
                    break;
                default:
                    break;
            }
        }

        private void recordPhoneCall() throws IOException {
            File outputDir = new File(Environment.getExternalStorageDirectory(),
                    "/DepressionDetectorAudio");
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    Log.e(TAG, "Recording phone call: cannot create a new directory.");
                }
            }
            String dateString = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault())
                                    .format(new Date());
            String fileName = "phone_call" + dateString;
            outputFile = File.createTempFile(fileName, ".amr", outputDir);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        callReceiver = new CallReceiver();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_IN_CALL);
        intentFilter.addAction(ACTION_OUT_CALL);

        registerReceiver(callReceiver, intentFilter);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy");
        unregisterReceiver(callReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
