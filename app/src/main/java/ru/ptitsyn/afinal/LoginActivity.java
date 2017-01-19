package ru.ptitsyn.afinal;

import android.os.Bundle;

public class LoginActivity extends BasePrimeActivity {

    protected int getContentViewName() {
        return R.layout.activity_login;
    }

    protected String getToolbarTitle() {
        return getResources().getString(R.string.action_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
