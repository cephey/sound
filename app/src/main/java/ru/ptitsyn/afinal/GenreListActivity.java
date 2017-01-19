package ru.ptitsyn.afinal;

import android.os.Bundle;

public class GenreListActivity extends BaseDeepActivity {

    protected int getContentViewName() {
        return R.layout.activity_genre_list;
    }

    protected String getToolbarTitle() {
        return getIntent().getStringExtra("niche_name");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
