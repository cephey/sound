package ru.ptitsyn.afinal;

import android.os.Bundle;

public class GenreBookListActivity extends BaseDeepActivity {

    protected int getContentViewName() {
        return R.layout.activity_genre_book;
    }

    protected String getToolbarTitle() {
        return getIntent().getStringExtra("genre_name");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
