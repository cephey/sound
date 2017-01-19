package ru.ptitsyn.afinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.ptitsyn.afinal.utils.BookItem;
import ru.ptitsyn.afinal.R;

public class BookAdapter extends ArrayAdapter<BookItem> {

    private final static int layout = R.layout.item_book;

    public BookAdapter(Context context) {
        super(context, layout);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BookItem bookItem = getItem(position);
        if (bookItem == null) {
            throw new RuntimeException("Book item is null");
        }

        Holder h;

        if (convertView == null){
            h = new Holder();

            convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);

            h.cover = (ImageView) convertView.findViewById(R.id.cover);
            h.name = (TextView) convertView.findViewById(R.id.text1);
            h.author = (TextView) convertView.findViewById(R.id.text2);

            convertView.setTag(h);

        } else {
            h = (Holder) convertView.getTag();
        }

        Picasso.with(h.cover.getContext()).load(bookItem.cover).placeholder(R.drawable.ic_public).resize(330, 330).into(h.cover);
        h.name.setText(bookItem.name);
        h.author.setText(bookItem.author_name);

        return convertView;
    }

    private static class Holder{
        ImageView cover;
        TextView name;
        TextView author;
    }
}
