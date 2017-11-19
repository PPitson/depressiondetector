package pl.agh.depressiondetector.analytics.phonecalls;

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
import static pl.agh.depressiondetector.utils.FileUtils.getPhoneCallFileName;
import static pl.agh.depressiondetector.utils.FileUtils.getPhoneCallsDirectory;
import static pl.agh.depressiondetector.utils.FileUtils.getTemporaryDirectory;

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

        private MediaRecorder mediaRecorder = null;
        private File outputFile;

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
                                    if (mediaRecorder != null) {
                                        mediaRecorder.stop();
                                        File phoneCallsDir = getPhoneCallsDirectory();
                                        FileUtils.createDirectory(phoneCallsDir);
                                        copyFile(outputFile, new File(phoneCallsDir, outputFile.getName()));
                                        outputFile.delete();
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
            File outputDir = getTemporaryDirectory();
            FileUtils.createDirectory(outputDir);

            outputFile = new File(outputDir, getPhoneCallFileName() + ".amr");
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