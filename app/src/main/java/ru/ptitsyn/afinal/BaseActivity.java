package ru.ptitsyn.afinal;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    Menu toolbarMenu;
    Toolbar toolbar;

    private int mainMenu = R.menu.main_menu;

    protected int getContentViewName() {
        throw new UnsupportedOperationException();
    }

    protected boolean collapseSearchAfterSubmit() {
        return true;
    }

    protected String getToolbarTitle() {
        return getResources().getString(R.string.app_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewName());

        // Устанавливаю DrawerLayout and NavigationView
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);

        // при клике на пункте боковой навигации запускаю нужную активити
        mNavigationView.setNavigationItemSelectedListener(this);

        // Инициазизация тулбара
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getToolbarTitle());
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawers();

        Class activity_class = null;

        switch (item.getItemId()) {
            case R.id.nav_item_user_book:
                activity_class = UserBookActivity.class;
                break;
            case R.id.nav_item_catalog:
                activity_class = CatalogActivity.class;
                break;
            case R.id.nav_item_settings:
                activity_class = SettingsActivity.class;
                break;
            case R.id.nav_item_login:
                activity_class = LoginActivity.class;
                break;
        }
        if (activity_class != null) {
            Intent intent = new Intent(getApplicationContext(), activity_class);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbarMenu = menu;
        getMenuInflater().inflate(mainMenu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (collapseSearchAfterSubmit()) {
                    (toolbarMenu.findItem(R.id.action_search)).collapseActionView();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchActivity.class)));
        searchView.setIconifiedByDefault(false);

        return true;
    }
}
