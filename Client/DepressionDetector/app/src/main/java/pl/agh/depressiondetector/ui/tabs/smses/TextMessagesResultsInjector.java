package pl.agh.depressiondetector.ui.tabs.smses;

import android.widget.TextView;

import pl.agh.depressiondetector.utils.ResultInjector;


public class TextMessagesResultsInjector implements ResultInjector {
    // only a draft
    private TextView textView;

    TextMessagesResultsInjector(TextView textView) {
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
