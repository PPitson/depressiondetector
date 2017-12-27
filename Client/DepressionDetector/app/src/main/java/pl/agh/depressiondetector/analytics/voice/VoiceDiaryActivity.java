package pl.agh.depressiondetector.analytics.voice;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.utils.ToastUtils;


public class VoiceDiaryActivity extends AppCompatActivity {
    private Recorder recorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_diary);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT < 24)
            ButterKnife.findById(this, R.id.pause_resume_recording).setEnabled(false);

        try {
            recorder = new Recorder(MediaRecorder.AudioSource.MIC);
            recorder.startRecording();
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.show(this, getString(R.string.unexpected_error));
            finish();
        }
    }

    @OnClick(R.id.cancel_recording)
    public void onCancelClick() {
        ToastUtils.show(this, getString(R.string.recording_cancelled));
        recorder.stopRecording();
        recorder.deleteOutputFile();
        finish();
    }

    @OnClick(R.id.pause_resume_recording)
    public void onPauseResumeClick(Button button) {
        togglePauseResume(button);
    }

    @OnClick(R.id.save_recording)
    public void onSaveClick() {
        recorder.saveOutputFile();
        recorder.deleteOutputFile();
        ToastUtils.show(this, getString(R.string.recording_saved));
        finish();
    }

    private void togglePauseResume(Button button) {
        if (button.getText().equals(getString(R.string.pause_button))) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play_circle_filled_grey, 0, 0);
            button.setText(getString(R.string.resume_button));
            recorder.pauseRecording();
            ToastUtils.show(this, getString(R.string.recording_paused));
        }
        else if (button.getText().equals(getString(R.string.resume_button))) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pause_circle_filled_grey, 0, 0);
            button.setText(getString(R.string.pause_button));
            recorder.resumeRecording();
            ToastUtils.show(this, getString(R.string.recording_resumed));
        }
    }
}
