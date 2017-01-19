package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.utils.BookItem;
import ru.ptitsyn.afinal.utils.BookStore;

public class BookDetailLoaderTask extends AsyncTask<Void,Void,BookItem> {

    private String mPath;
    private Context mContext;
    private AsyncResponse mDelegate;

    public BookDetailLoaderTask(String path, Context context, AsyncResponse delegate) {
        mPath = path;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected BookItem doInBackground(Void... voids) {
        return BookStore.fetchBookDetail(mPath, mContext);
    }

    @Override
    protected void onPostExecute(BookItem item) {
        super.onPostExecute(item);
        mDelegate.onTaskCompleted(item);
    }

}
