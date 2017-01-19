package ru.ptitsyn.afinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import ru.ptitsyn.afinal.BookPageActivity;
import ru.ptitsyn.afinal.adapters.BookAdapter;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.tasks.BookListLoaderTask;
import ru.ptitsyn.afinal.utils.BookItem;
import ru.ptitsyn.afinal.R;

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse {

    BookAdapter ad;
    ListView lvResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_layout, null);

        lvResult = (ListView) view.findViewById(R.id.lvSearch);
        lvResult.setOnItemClickListener(this);

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

    public void run_search(String query) {
        ad = new BookAdapter(getActivity().getApplicationContext());
        lvResult.setAdapter(ad);

        String mQuery;
        try {
            mQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("EA_DEMO","Error encode query string", e);
            mQuery = "";
        }
        BookListLoaderTask t = new BookListLoaderTask(
                "/api/search/?q=" + mQuery, getActivity().getApplicationContext(), this
        );
        t.execute();
    }

    @Override
    public void onTaskCompleted(Object object) {
        List<BookItem> items = (List<BookItem>) object;

        for (BookItem item : items){
            ad.add(item);
        }
        ad.notifyDataSetChanged();
    }

}