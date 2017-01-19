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

import ru.ptitsyn.afinal.BooksetBookListActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.adapters.BooksetAdapter;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Bookset;
import ru.ptitsyn.afinal.tasks.BooksetListLoaderTask;

public class BooksetsFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse {

    BooksetAdapter ad;
    ListView lvBookset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_booksets_layout, null);

        lvBookset = (ListView) view.findViewById(R.id.lvBookset);
        lvBookset.setOnItemClickListener(this);

        ad = new BooksetAdapter(getActivity().getApplicationContext());
        lvBookset.setAdapter(ad);

        BooksetListLoaderTask t = new BooksetListLoaderTask(
                "/api/booksets/", getActivity().getApplicationContext(), this
        );
        t.execute();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bookset bookset = ad.getItem(position);

        if (bookset != null) {
            // Стартую BooksetBookListActivity
            Intent intent = new Intent(getActivity(), BooksetBookListActivity.class);
            intent.putExtra("bookset_id", bookset.id);
            startActivity(intent);
        }
    }

    @Override
    public void onTaskCompleted(Object object) {
        List<Bookset> items = (List<Bookset>) object;

        for (Bookset item : items){
            ad.add(item);
        }
        ad.notifyDataSetChanged();
    }

}
