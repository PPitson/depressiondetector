package pl.agh.depressiondetector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.agh.depressiondetector.analytics.AnalysedDataType;
import pl.agh.depressiondetector.ui.settings.ProfileActivity;
import pl.agh.depressiondetector.ui.settings.SettingsActivity;
import pl.agh.depressiondetector.ui.tabs.overview.OverviewFragment;
import pl.agh.depressiondetector.ui.tabs.phonecalls.PhoneCallResultsFragment;
import pl.agh.depressiondetector.ui.tabs.smses.TextMessagesResultsFragment;

public class MainActivity extends AppCompatActivity {

    private static List<AnalysedDataType> TAB_TYPES;

    @BindView(R.id.main_view_pager)
    ViewPager viewPager;

    @BindView(R.id.main_tab_layout)
    TabLayout tabLayout;

    private MainPagerAdapter mainPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TAB_TYPES = setupTabTypes(getApplicationContext());

        ButterKnife.bind(this);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        if (TAB_TYPES.size() == 1) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    tabLayout.setScrollPosition(tab.getPosition(), 0, true);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_main_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.item_main_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {
        MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new OverviewFragment();
                default:
                    if (position > TAB_TYPES.size())
                        return null;
                    return getItemFromType(TAB_TYPES.get(position - 1));
            }
        }

        @Override
        public int getCount() {
            return TAB_TYPES.size() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position > TAB_TYPES.size())
                return null;
            switch (position) {
                case 0:
                    return getString(R.string.main_overview);
                default:
                    return getPageTitleFromType(TAB_TYPES.get(position - 1));
            }
        }
    }

    private List<AnalysedDataType> setupTabTypes(Context appContext) {
        List<AnalysedDataType> tabTypes = new ArrayList<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        for (AnalysedDataType type : AnalysedDataType.values()) {
            if (preferences.getBoolean(type.preferenceName, true)) {
                tabTypes.add(type);
            }
        }

        return tabTypes;
    }

    private Fragment getItemFromType(AnalysedDataType type) {
        switch (type) {
            case PHONE_CALL:
                return new PhoneCallResultsFragment();
            case SMS:
                return new TextMessagesResultsFragment();
            default:
                return null;
        }
    }

    private CharSequence getPageTitleFromType(AnalysedDataType type) {
        switch (type) {
            case PHONE_CALL:
                return getString(R.string.main_phone_calls_results);
            case SMS:
                return getString(R.string.main_text_messages_results);
            default:
                return null;
        }
    }
}
