package ru.ptitsyn.afinal;

import android.os.Bundle;

public class SettingsActivity extends BasePrimeActivity {

    protected int getContentViewName() {
        return R.layout.activity_settings;
    }

    protected String getToolbarTitle() {
        return getResources().getString(R.string.action_settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
