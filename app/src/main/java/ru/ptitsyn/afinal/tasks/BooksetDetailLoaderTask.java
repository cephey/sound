package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Bookset;
import ru.ptitsyn.afinal.utils.BooksetStore;

public class BooksetDetailLoaderTask extends AsyncTask<Void,Void,Bookset> {

    private int booksetId;
    private Context mContext;
    private AsyncResponse mDelegate;

    public BooksetDetailLoaderTask(int id, Context context, AsyncResponse delegate) {
        booksetId = id;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected Bookset doInBackground(Void... voids) {
        return BooksetStore.fetchBooksetDetail(booksetId, mContext);
    }

    @Override
    protected void onPostExecute(Bookset item) {
        super.onPostExecute(item);
        mDelegate.onTaskCompleted(item);
    }

}
