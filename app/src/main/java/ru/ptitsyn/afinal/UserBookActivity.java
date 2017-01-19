package ru.ptitsyn.afinal;

import android.os.Bundle;
import android.view.MenuItem;

public class UserBookActivity extends BasePrimeActivity {

    protected int getContentViewName() {
        return R.layout.activity_user_book;
    }

    protected String getToolbarTitle() {
        return getResources().getString(R.string.action_user_book);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
