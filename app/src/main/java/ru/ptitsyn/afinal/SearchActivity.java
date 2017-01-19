package ru.ptitsyn.afinal;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import ru.ptitsyn.afinal.fragments.SearchFragment;

public class SearchActivity extends BaseDeepActivity {

    protected String query;
    protected Menu mMenu;

    protected int getContentViewName() {
        return R.layout.activity_search;
    }

    protected boolean collapseSearchAfterSubmit() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Устанавливаю SearchFragment первым при создании активити

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.searchContainerView, new SearchFragment(), "search_tag").commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);

            SearchFragment fragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("search_tag");
            fragment.run_search(query);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMenu != null) {
            MenuItem searchItem = mMenu.findItem(R.id.action_search);
            (searchItem.getActionView()).clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mMenu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        if (query != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQuery(query, false);
            searchView.clearFocus();
        }

        // что-бы когда нажимал назад с экрана поиска, попадал на предыдущую активити
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return true;
            }
        });

        return true;
    }
}
