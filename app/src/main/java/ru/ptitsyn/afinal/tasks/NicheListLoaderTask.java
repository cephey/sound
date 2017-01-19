package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Niche;
import ru.ptitsyn.afinal.utils.NicheStore;

public class NicheListLoaderTask extends AsyncTask<Void,Void,List<Niche>> {

    private String mPath;
    private Context mContext;
    private AsyncResponse mDelegate;

    public NicheListLoaderTask(String path, Context context, AsyncResponse delegate) {
        mPath = path;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected List<Niche> doInBackground(Void... voids) {
        return NicheStore.fetchNicheList(mPath, mContext);
    }

    @Override
    protected void onPostExecute(List<Niche> items) {
        super.onPostExecute(items);
        mDelegate.onTaskCompleted(items);
    }

}
