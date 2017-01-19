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

import ru.ptitsyn.afinal.BookPageActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.adapters.BookAdapter;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.tasks.BookListLoaderTask;
import ru.ptitsyn.afinal.utils.BookItem;


public class BestFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse {

    BookAdapter ad;
    ListView lvBest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_best_layout, null);

        lvBest = (ListView) view.findViewById(R.id.lvBest);
        lvBest.setOnItemClickListener(this);

        ad = new BookAdapter(getActivity().getApplicationContext());
        lvBest.setAdapter(ad);

        BookListLoaderTask t = new BookListLoaderTask(
                "/api/audiobooks/?o=popular&limit=20", getActivity().getApplicationContext(), this
        );
        t.execute();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BookItem book = ad.getItem(position);

        if (book != null) {
            // Стартую BookPageActivity
            Intent intent = new Intent(getActivity(), BookPageActivity.class);
            intent.putExtra("book_id", book.id);
            intent.putExtra("book_name", book.name);
            startActivity(intent);
        }
    }

    @Override
    public void onTaskCompleted(Object object) {
        List<BookItem> items = (List<BookItem>) object;

        for (BookItem item : items) {
            ad.add(item);
        }
        ad.notifyDataSetChanged();
    }

}