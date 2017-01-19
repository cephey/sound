package ru.ptitsyn.afinal.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.fragments.tab.BestFragment;
import ru.ptitsyn.afinal.fragments.tab.BooksetsFragment;
import ru.ptitsyn.afinal.fragments.tab.NichesFragment;


public class CatalogFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    public static int int_items = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View x = inflater.inflate(R.layout.catalog_layout, null);
        tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);


        MyAdapter pagerAdapter = new MyAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        return x;
    }

    class MyAdapter extends FragmentPagerAdapter {

        private MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position){
                case 0 : return new BestFragment();
                case 1 : return new NichesFragment();
                case 2 : return new BooksetsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return "Лучшее";
                case 1 :
                    return "Жанры";
                case 2 :
                    return "Подборки";
            }
            return null;
        }
    }
}
