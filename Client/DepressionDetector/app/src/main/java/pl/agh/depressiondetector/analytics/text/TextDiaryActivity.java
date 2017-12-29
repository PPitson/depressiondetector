package pl.agh.depressiondetector.analytics.text;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.utils.ToastUtils;


public class TextDiaryActivity extends AppCompatActivity {
    @BindView(R.id.diary_edit_text)
    EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_diary);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.cancel_text_diary)
    public void onCancelClick() {
        ToastUtils.show(this, getString(R.string.writing_cancelled));
        finish();
    }

    @OnClick(R.id.save_text_diary)
    public void onSaveClick() {
        TextFileWriter textFileWriter = new TextFileWriter(editText.getText().toString(), new Date(), this);
        try {
            textFileWriter.saveText();
            ToastUtils.show(this, getString(R.string.writing_saved));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            ToastUtils.show(this, getString(R.string.unexpected_error));
        } finally {
            finish();
        }
    }
}
