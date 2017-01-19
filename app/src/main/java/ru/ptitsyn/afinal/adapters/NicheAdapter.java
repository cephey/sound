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
import ru.ptitsyn.afinal.models.Niche;

public class NicheAdapter extends ArrayAdapter<Niche> {

    public NicheAdapter(Context context) {
        super(context, android.R.layout.two_line_list_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Niche n = getItem(position);

        NicheAdapter.Holder h;

        if (convertView == null){
            h = new NicheAdapter.Holder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.niche_list_item, parent, false);

            h.i1 = (ImageView) convertView.findViewById(R.id.image);
            h.t1 = (TextView) convertView.findViewById(R.id.text1);

            convertView.setTag(h);

        } else {
            h = (NicheAdapter.Holder) convertView.getTag();
        }

        Picasso.with(h.i1.getContext()).load(n.image).placeholder(R.drawable.ic_public).resize(370, 130).into(h.i1);
        h.t1.setText(n.name);

        return convertView;
    }

    private static class Holder{
        ImageView i1;
        TextView t1;
    }

}
