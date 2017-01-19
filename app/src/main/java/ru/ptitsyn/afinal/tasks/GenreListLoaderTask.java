package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Genre;
import ru.ptitsyn.afinal.utils.GenreStore;

public class GenreListLoaderTask extends AsyncTask<Void,Void,List<Genre>> {

    private int nicheId;
    private Context mContext;
    private AsyncResponse mDelegate;

    public GenreListLoaderTask(int id, Context context, AsyncResponse delegate) {
        nicheId = id;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected List<Genre> doInBackground(Void... voids) {
        return GenreStore.fetchGenreList(nicheId, mContext);
    }

    @Override
    protected void onPostExecute(List<Genre> items) {
        super.onPostExecute(items);
        mDelegate.onTaskCompleted(items);
    }

}
