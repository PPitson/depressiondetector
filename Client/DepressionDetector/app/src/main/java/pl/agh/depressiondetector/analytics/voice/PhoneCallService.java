package pl.agh.depressiondetector.analytics.voice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import pl.agh.depressiondetector.utils.FileUtils;

import static pl.agh.depressiondetector.utils.FileUtils.copyFile;
import static pl.agh.depressiondetector.utils.FileUtils.getVoiceDirectory;

public class PhoneCallService extends Service {

    private static final String TAG = "PhoneCallService";

    private CallReceiver callReceiver;

    private static final String ACTION_IN_CALL = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT_CALL = "android.intent.action.NEW_OUTGOING_CALL";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        callReceiver = new CallReceiver();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_IN_CALL);
        intentFilter.addAction(ACTION_OUT_CALL);

        registerReceiver(callReceiver, intentFilter);

        return START_STICKY;
    }

    private class CallReceiver extends BroadcastReceiver {

        private Recorder recorder;

        private Boolean wasRinging = false;
        private Boolean isRecording = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received intent with action: " + intent.getAction());
            Bundle bundle = intent.getExtras();
            try {
                switch (intent.getAction()) {
                    case ACTION_IN_CALL:
                        if (bundle != null) {
                            String state = bundle.getString(TelephonyManager.EXTRA_STATE);
                            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                                wasRinging = true;
                            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && wasRinging) {
                                recordPhoneCall();
                            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                                wasRinging = false;
                                if (isRecording) {
                                    isRecording = false;
                                    if (recorder != null) {
                                        recorder.saveOutputFile();
                                        recorder.deleteOutputFile();
                                    }
                                }
                            }
                        }
                        break;
                    case ACTION_OUT_CALL:
                        recordPhoneCall();
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void recordPhoneCall() throws IOException {
            recorder = new Recorder(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            recorder.startRecording();
            isRecording = true;
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(callReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
