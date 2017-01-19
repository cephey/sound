package ru.ptitsyn.afinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import ru.ptitsyn.afinal.GenreBookListActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.adapters.GenreAdapter;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Genre;
import ru.ptitsyn.afinal.tasks.GenreListLoaderTask;

public class GenreListFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse {

    GenreAdapter ad;
    ListView lvGenre;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.genre_list_layout, null);

        lvGenre = (ListView) view.findViewById(R.id.lvGenres);
        lvGenre.setOnItemClickListener(this);

        ad = new GenreAdapter(getActivity().getApplicationContext());
        lvGenre.setAdapter(ad);

        int nicheId = getActivity().getIntent().getIntExtra("niche_id", 0);
        GenreListLoaderTask t = new GenreListLoaderTask(nicheId, getActivity().getApplicationContext(), this);
        t.execute();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Genre genre = ad.getItem(position);

        if (genre != null) {
            // Стартую GenreBookListActivity
            Intent intent = new Intent(getActivity(), GenreBookListActivity.class);
            intent.putExtra("genre_id", genre.id);
            intent.putExtra("genre_name", genre.name);
            startActivity(intent);
        }
    }

    @Override
    public void onTaskCompleted(Object object) {
        List<Genre> items = (List<Genre>) object;

        for (Genre item : items){
            ad.add(item);
        }
        ad.notifyDataSetChanged();
    }

}
