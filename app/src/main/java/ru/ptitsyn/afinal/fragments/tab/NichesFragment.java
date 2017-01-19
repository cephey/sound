package ru.ptitsyn.afinal.fragments.tab;

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

import ru.ptitsyn.afinal.GenreListActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.adapters.NicheAdapter;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Niche;
import ru.ptitsyn.afinal.tasks.NicheListLoaderTask;

public class NichesFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse {

    NicheAdapter ad;
    ListView lvNiche;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_niches_layout, null);

        lvNiche = (ListView) view.findViewById(R.id.lvNiche);
        lvNiche.setOnItemClickListener(this);

        ad = new NicheAdapter(getActivity().getApplicationContext());
        lvNiche.setAdapter(ad);

        NicheListLoaderTask t = new NicheListLoaderTask(
                "/api/niches/", getActivity().getApplicationContext(), this
        );
        t.execute();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Niche niche = ad.getItem(position);

        if (niche != null) {
            // Стартую GenreListActivity
            Intent intent = new Intent(getActivity(), GenreListActivity.class);
            intent.putExtra("niche_id", niche.id);
            intent.putExtra("niche_name", niche.name);
            startActivity(intent);
        }
    }

    @Override
    public void onTaskCompleted(Object object) {
        List<Niche> items = (List<Niche>) object;

        for (Niche item : items){
            ad.add(item);
        }
        ad.notifyDataSetChanged();
    }

}
