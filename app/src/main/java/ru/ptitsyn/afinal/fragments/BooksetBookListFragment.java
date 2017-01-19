package ru.ptitsyn.afinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.ptitsyn.afinal.BookPageActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.adapters.BookAdapter;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Bookset;
import ru.ptitsyn.afinal.tasks.BookListLoaderTask;
import ru.ptitsyn.afinal.tasks.BooksetDetailLoaderTask;
import ru.ptitsyn.afinal.utils.BookItem;

public class BooksetBookListFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse {

    BookAdapter ad;
    ListView lvResult;

    ImageView image;
    TextView text1;
    TextView text2;
    TextView text3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookset_book_layout, null);

        image = (ImageView) view.findViewById(R.id.image);
        text1 = (TextView) view.findViewById(R.id.text1);
        text2 = (TextView) view.findViewById(R.id.text2);
        text3 = (TextView) view.findViewById(R.id.text3);

        lvResult = (ListView) view.findViewById(R.id.lvBooksetBook);
        lvResult.setOnItemClickListener(this);

        ad = new BookAdapter(getActivity().getApplicationContext());
        lvResult.setAdapter(ad);

        int booksetId = getActivity().getIntent().getIntExtra("bookset_id", 0);

        BooksetDetailLoaderTask t1 = new BooksetDetailLoaderTask(booksetId, getActivity().getApplicationContext(), this);
        t1.execute();

        BookListLoaderTask t2 = new BookListLoaderTask(
                "/api/audiobooks/?bookset=" + booksetId, getActivity().getApplicationContext(), this
        );
        t2.execute();

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

        if(object instanceof List<?>) {

            List<BookItem> items = (List<BookItem>) object;

            for (BookItem item : items) {
                ad.add(item);
            }
            ad.notifyDataSetChanged();

        } else {

            Bookset item = (Bookset) object;

            Picasso.with(image.getContext()).load(item.image).placeholder(R.drawable.ic_public).resize(360, 200).into(image);
            text1.setText(item.name);
            text2.setText(item.book_count + " книг в подборке");
            text3.setText(item.description);
        }
    }

}
