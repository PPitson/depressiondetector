package pl.agh.depressiondetector.analytics.voice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;


public class VoiceDiaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_diary);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.cancel_recording)
    public void onCancelClick() {
        finish();
    }

    @OnClick(R.id.pause_resume_recording)
    public void onPauseResumeClick(Button button) {
        togglePauseResumeButtonImage(button);
    }

    @OnClick(R.id.save_recording)
    public void onSaveClick() {

    }

    private void togglePauseResumeButtonImage(Button button) {
        if (button.getText().equals(getString(R.string.pause_button))) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play_circle_filled_grey, 0, 0);
            button.setText(getString(R.string.resume_button));
        }
        else if (button.getText().equals(getString(R.string.resume_button))) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pause_circle_filled_grey, 0, 0);
            button.setText(getString(R.string.pause_button));
        }
    }
}
