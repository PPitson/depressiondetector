package pl.agh.depressiondetector.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import pl.agh.depressiondetector.MainActivity;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.analytics.AnalysedDataType;
import pl.agh.depressiondetector.analytics.AnalyticsAdapter;
import pl.agh.depressiondetector.scheduler.UploadScheduler;
import pl.agh.depressiondetector.utils.ToastUtils;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.MOOD;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.PHONE_CALL;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.SMS;

public class FirstConfigurationActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @BindView(R.id.switch_firstConfiguration_mood)
    Switch switchMood;

    @BindView(R.id.switch_firstConfiguration_phoneCalls)
    Switch switchPhoneCalls;

    @BindView(R.id.switch_firstConfiguration_textMessages)
    Switch switchTextMessages;

    @BindView(R.id.button_firstConfiguration_goFurther)
    Button buttonGoFurther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_configuration);
        ButterKnife.bind(this);
        preferences = getDefaultSharedPreferences(this);
    }

    @OnClick(R.id.textView_firstConfiguration_mood)
    public void onMoodClick() {
        switchMood.setChecked(!switchMood.isChecked());
    }

    @OnCheckedChanged(R.id.switch_firstConfiguration_mood)
    public void onMoodSwitchClick(CompoundButton button, boolean checked) {
        preferences.edit().putBoolean(MOOD.preferenceName, checked).apply();
    }

    @OnClick(R.id.textView_firstConfiguration_phoneCalls)
    public void onPhoneCallsClick() {
        switchPhoneCalls.setChecked(!switchPhoneCalls.isChecked());
    }

    @OnCheckedChanged(R.id.switch_firstConfiguration_phoneCalls)
    public void onPhoneCallsSwitchClick(CompoundButton button, boolean checked) {
        preferences.edit().putBoolean(PHONE_CALL.preferenceName, checked).apply();
    }

    @OnClick(R.id.textView_firstConfiguration_textMessages)
    public void onTextMessagesClick() {
        switchTextMessages.setChecked(!switchTextMessages.isChecked());
    }

    @OnCheckedChanged(R.id.switch_firstConfiguration_textMessages)
    public void onTextMessagesSwitchClick(CompoundButton button, boolean checked) {
        preferences.edit().putBoolean(SMS.preferenceName, checked).apply();
    }

    @OnClick(R.id.button_firstConfiguration_goFurther)
    public void onGoFurtherClick() {
        List<String> permissions = new ArrayList<>();
        if (switchPhoneCalls.isChecked()) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
            permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (switchTextMessages.isChecked())
            permissions.add(Manifest.permission.READ_SMS);

        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);    // TODO Write to internal storage and remove this
        askForPermissionsIfNeeded(permissions);
    }

    private void askForPermissionsIfNeeded(List<String> permissions) {
        BaseMultiplePermissionsListener permissionsListener = new BaseMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    startAnalyticsAndFinish();
                } else {
                    List<PermissionDeniedResponse> deniedList = report.getDeniedPermissionResponses();
                    for (PermissionDeniedResponse denied : deniedList) {
                        String name = denied.getPermissionName();
                        switch (name) {
                            case Manifest.permission.READ_PHONE_STATE:
                            case Manifest.permission.PROCESS_OUTGOING_CALLS:
                            case Manifest.permission.RECORD_AUDIO:
                                switchPhoneCalls.setChecked(false);
                                break;

                            case Manifest.permission.READ_SMS:
                                switchTextMessages.setChecked(false);
                                break;

                            case Manifest.permission.WRITE_EXTERNAL_STORAGE:    // TODO Write to internal storage and remove this
                                switchPhoneCalls.setChecked(false);
                                switchTextMessages.setChecked(false);
                                break;
                        }
                    }
                    ToastUtils.show(FirstConfigurationActivity.this, "You should reconsider your choice or grant us with the permissions");
                }
            }
        };

        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(permissionsListener)
                .onSameThread()
                .check();
    }

    private void startAnalyticsAndFinish() {
        List<AnalysedDataType> types = new ArrayList<>();
        if (switchMood.isChecked())
            types.add(MOOD);
        if (switchPhoneCalls.isChecked())
            types.add(PHONE_CALL);
        if (switchTextMessages.isChecked())
            types.add(SMS);

        AnalyticsAdapter.startAnalytics(this, types.toArray(new AnalysedDataType[0]));
        UploadScheduler.schedule(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
