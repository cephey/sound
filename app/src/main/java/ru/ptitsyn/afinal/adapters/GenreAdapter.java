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
import ru.ptitsyn.afinal.models.Genre;
import ru.ptitsyn.afinal.models.Niche;

public class GenreAdapter extends ArrayAdapter<Genre> {

    public GenreAdapter(Context context) {
        super(context, android.R.layout.two_line_list_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Genre g = getItem(position);

        GenreAdapter.Holder h;

        if (convertView == null){
            h = new GenreAdapter.Holder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.genre_list_item, parent, false);

            h.t1 = (TextView) convertView.findViewById(R.id.text1);
            h.t2 = (TextView) convertView.findViewById(R.id.text2);

            convertView.setTag(h);

        } else {
            h = (GenreAdapter.Holder) convertView.getTag();
        }

        h.t1.setText(g.name);
        h.t2.setText(g.book_count + " книг");

        return convertView;
    }

    private static class Holder{
        TextView t1;
        TextView t2;
    }

}
