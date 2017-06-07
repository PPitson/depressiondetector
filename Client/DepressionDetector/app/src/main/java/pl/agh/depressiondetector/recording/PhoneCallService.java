package pl.agh.depressiondetector.recording;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.utils.FileUtils;
import pl.agh.depressiondetector.utils.ToastUtils;

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
                        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && wasRinging) {
                            try {
                                recordPhoneCall();
                            } catch (IOException e) {
                                Log.e(TAG, "Recording phone call: " + e);
                            }
                        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                            wasRinging = false;
                            if (isRecording) {
                                isRecording = false;
                                if (mediaRecorder != null) {
                                    mediaRecorder.stop();
                                    new PostAudioTask(context).execute(outputFile);
                                }
                            }
                        }
                    }
                    break;
                case ACTION_OUT_CALL:
                    try {
                        recordPhoneCall();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }

        private void recordPhoneCall() throws IOException {
            File outputDir = FileUtils.getAudioDirectory();
            if (!FileUtils.createDirectory(outputDir))
                Log.e(TAG, "Recording phone call: cannot create a new directory.");

            String fileName = FileUtils.getPhoneCallFileName();
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

    private class PostAudioTask extends AsyncTask<File, Void, Integer> {

        private Context context;

        PostAudioTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(File... params) {
            return HttpClient.getInstance().postAudioFile(params[0]);
        }

        @Override
        protected void onPostExecute(Integer requestCode) {
            if (requestCode == 200)
                ToastUtils.show(context, "Record successfully sent");   // TODO Delete toast
            else
                ToastUtils.show(context, "There was an error while sending a record");   // TODO Delete toast
        }
    }
}
