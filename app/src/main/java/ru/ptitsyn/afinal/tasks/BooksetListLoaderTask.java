package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;
import java.util.List;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Bookset;
import ru.ptitsyn.afinal.utils.BooksetStore;

public class BooksetListLoaderTask extends AsyncTask<Void,Void,List<Bookset>> {

    private String mPath;
    private Context mContext;
    private AsyncResponse mDelegate;

    public BooksetListLoaderTask(String path, Context context, AsyncResponse delegate) {
        mPath = path;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected List<Bookset> doInBackground(Void... voids) {
        return BooksetStore.fetchBooksetList(mPath, mContext);
    }

    @Override
    protected void onPostExecute(List<Bookset> items) {
        super.onPostExecute(items);
        mDelegate.onTaskCompleted(items);
    }

}
