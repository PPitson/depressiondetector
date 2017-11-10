package pl.agh.depressiondetector.ui.tabs.smses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.API;
import pl.agh.depressiondetector.utils.ResultAcquirer;


public class TextMessagesResultsFragment extends Fragment {
    private static final String TAG = "TextMessagesResFragment";

    @BindView(R.id.text_messages_results_textview)
    TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_messages_results, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        new ResultAcquirer(TAG, getContext(), new TextMessagesResultsInjector(textView), API.PATH_TEXT_RESULTS).execute();
    }
}
