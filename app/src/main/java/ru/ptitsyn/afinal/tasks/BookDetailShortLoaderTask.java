package ru.ptitsyn.afinal.tasks;

import android.content.Context;
import android.os.AsyncTask;

import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Book;
import ru.ptitsyn.afinal.utils.BookStore;

public class BookDetailShortLoaderTask extends AsyncTask<Void,Void,Book> {

    private int bookId;
    private Context mContext;
    private AsyncResponse mDelegate;

    public BookDetailShortLoaderTask(int id, Context context, AsyncResponse delegate) {
        bookId = id;
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    protected Book doInBackground(Void... voids) {
        return BookStore.fetchBookDetailShort(bookId, mContext);
    }

    @Override
    protected void onPostExecute(Book item) {
        super.onPostExecute(item);
        mDelegate.onTaskCompleted(item);
    }

}
