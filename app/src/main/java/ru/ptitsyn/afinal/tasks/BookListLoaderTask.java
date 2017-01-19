package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.utils.BookItem;
import ru.ptitsyn.afinal.utils.BookStore;

public class BookListLoaderTask extends AsyncTask<Void,Void,List<BookItem>> {

    private String mPath;
    private Context mContext;
    private AsyncResponse mDelegate;

    public BookListLoaderTask(String path, Context context, AsyncResponse delegate) {
        mPath = path;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected List<BookItem> doInBackground(Void... voids) {
        return BookStore.fetchBookList(mPath, mContext);
    }

    @Override
    protected void onPostExecute(List<BookItem> items) {
        super.onPostExecute(items);
        mDelegate.onTaskCompleted(items);
    }

}
