package ru.ptitsyn.afinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.models.Bookset;

public class BooksetAdapter extends ArrayAdapter<Bookset> {

    public BooksetAdapter(Context context) {
        super(context, android.R.layout.two_line_list_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Bookset bs = getItem(position);

        BooksetAdapter.Holder h;

        if (convertView == null){
            h = new BooksetAdapter.Holder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bookset_list_item, parent, false);

            h.i1 = (ImageView) convertView.findViewById(R.id.image);
            h.t1 = (TextView) convertView.findViewById(R.id.text1);
            h.t2 = (TextView) convertView.findViewById(R.id.text2);

            convertView.setTag(h);

        } else {
            h = (BooksetAdapter.Holder) convertView.getTag();
        }

        Picasso.with(h.i1.getContext()).load(bs.image).placeholder(R.drawable.ic_public).resize(360, 200).into(h.i1);
        h.t1.setText(bs.name);
        h.t2.setText(bs.book_count + " книг в подборке");

        return convertView;
    }

    private static class Holder{
        ImageView i1;
        TextView t1;
        TextView t2;
    }

}
