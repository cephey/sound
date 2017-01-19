package ru.ptitsyn.afinal.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import ru.ptitsyn.afinal.BookPageActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.utils.BookItem;
import ru.ptitsyn.afinal.utils.PlayerInfo;

public class BookPageFragment extends Fragment {

    ImageView cover;

    private boolean needSetSongs = true;
    private boolean pauseSong = false;
    private Button b3;
    private SeekBar seekbar;
    private TextView tx1, tx2;
    public LinearLayout cover_layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_detail_loyout, null);

        cover_layout = (LinearLayout) view.findViewById(R.id.cover_layout);

        cover = (ImageView) view.findViewById(R.id.cover);

        b3 = (Button) view.findViewById(R.id.button3);
        tx1 = (TextView) view.findViewById(R.id.textView2);
        tx2 = (TextView) view.findViewById(R.id.textView3);

        needSetSongs = true;
        pauseSong = false;

        seekbar = (SeekBar) view.findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        // play
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pauseSong) {
                    ((BookPageActivity) getActivity()).pauseAudio();
                    b3.setBackgroundResource(R.drawable.play);
                } else {
                    if (needSetSongs) {
                        ((BookPageActivity) getActivity()).playAudio();
                    } else {
                        ((BookPageActivity) getActivity()).resumeAudio();
                    }
                    needSetSongs = false;
                    b3.setBackgroundResource(R.drawable.pause);
                }
                pauseSong = !pauseSong;
            }
        });

        return view;
    }

    public void fillBookData(BookItem item) {
        Picasso.with(cover.getContext()).load(item.cover).placeholder(R.drawable.ic_public).resize(330, 330).into(cover);
    }

    public void fillPlayerData(PlayerInfo info) {

        if (info == null) {
            return;
        }

        seekbar.setMax((int) info.finalTime);
        seekbar.setProgress((int) info.startTime);

        long finalTimeMinutes = TimeUnit.MILLISECONDS.toMinutes((long) info.finalTime);
        long finalTimeSeconds = TimeUnit.MILLISECONDS.toSeconds((long) info.finalTime) - TimeUnit.MINUTES.toSeconds(finalTimeMinutes);

        tx2.setText(String.format("%d min, %d sec", finalTimeMinutes, finalTimeSeconds));

        long startTimeMinutes = TimeUnit.MILLISECONDS.toMinutes((long) info.startTime);
        long startTimeSeconds = TimeUnit.MILLISECONDS.toSeconds((long) info.startTime) - TimeUnit.MINUTES.toSeconds(startTimeMinutes);

        tx1.setText(String.format("%d min, %d sec", startTimeMinutes, startTimeSeconds));

    }
}
