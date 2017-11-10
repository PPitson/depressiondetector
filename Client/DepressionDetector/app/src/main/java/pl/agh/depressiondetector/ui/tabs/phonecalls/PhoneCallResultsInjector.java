package pl.agh.depressiondetector.ui.tabs.phonecalls;

import android.widget.TextView;

import pl.agh.depressiondetector.utils.ResultInjector;


public class PhoneCallResultsInjector implements ResultInjector {
    // only a draft
    private TextView textView;

    PhoneCallResultsInjector(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void injectResults(String results) {
        //TODO: process results and make plots
        if (textView != null) {
            textView.setText("The results:\n\t" + results);
        }
    }
}
