package ru.ptitsyn.afinal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.ptitsyn.afinal.fragments.BookPageFragment;
import ru.ptitsyn.afinal.interfaces.AsyncResponse;
import ru.ptitsyn.afinal.models.Book;
import ru.ptitsyn.afinal.services.MediaPlayerService;
import ru.ptitsyn.afinal.tasks.BookDetailLoaderTask;
import ru.ptitsyn.afinal.tasks.BookDetailShortLoaderTask;
import ru.ptitsyn.afinal.utils.Audio;
import ru.ptitsyn.afinal.utils.BookItem;
import ru.ptitsyn.afinal.utils.DBHelper;
import ru.ptitsyn.afinal.utils.PlayerInfo;
import ru.ptitsyn.afinal.utils.StorageUtil;

public class BookPageActivity extends BaseDeepActivity implements AsyncResponse {

    private int bookId;
    private Context context;

    private int mainMenu = R.menu.book_main_menu;

    private MediaPlayerService player;
    boolean serviceBound = false;

    ArrayList<Audio> audioList;
    int audioIndex = 0;
    int autoBookmarkPosition = 0;
    private Handler myHandler = new Handler();
    private Handler searchHandler = new Handler();

    public static final String Broadcast_PLAY_NEW_AUDIO = "ru.ptitsyn.afinal.PlayNewAudio";
    public static final String Broadcast_PAUSE_AUDIO = "ru.ptitsyn.afinal.PauseAudio";
    public static final String Broadcast_RESUME_AUDIO = "ru.ptitsyn.afinal.PesumeAudio";

    protected int getContentViewName() {
        return R.layout.activity_book;
    }

    protected String getToolbarTitle() {
        return getIntent().getStringExtra("book_name");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Log.e("EA_DEMO", "Service Bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        bookId = getIntent().getIntExtra("book_id", 0);

        BookDetailShortLoaderTask t1 = new BookDetailShortLoaderTask(
                bookId, getApplicationContext(), this
        );
        t1.execute();

        BookDetailLoaderTask t2 = new BookDetailLoaderTask(
                "/api/audiobooks/" + bookId + "/", getApplicationContext(), this
        );
        t2.execute();

        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onTaskCompleted(Object object) {

        if (object instanceof BookItem) {
            BookItem item = (BookItem) object;
            buildAudioList(item);

        } else if (object instanceof Book) {
            Book book = (Book) object;

            int backgroundColor = Color.parseColor("#" + book.backgroundColor);
            int fontColor = Color.parseColor("#" + book.fontColor);

            // set actionBar background color
            ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(backgroundColor));

            // set back button color
            final Drawable backArrow = ContextCompat.getDrawable(this, R.drawable.back);
            backArrow.setColorFilter(fontColor, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(backArrow);

            // set title color
            toolbar.setTitleTextColor(fontColor);

            // вытащить из базы имя автора
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor c = db.rawQuery(
                    "SELECT cover_name FROM author INNER JOIN book_author ON author.id = book_author.author_id where book_author.book_id = " + book.id,
                    new String[] {}
            );
            String authorName = "";
            if (c.moveToFirst()) {
                authorName = c.getString(c.getColumnIndex("cover_name"));
            }
            c.close();
            toolbar.setSubtitle(authorName);
            toolbar.setSubtitleTextColor(fontColor);

            // set book cover background color
            BookPageFragment fragment = (BookPageFragment) getSupportFragmentManager().findFragmentById(R.id.bookDetail);
            fragment.cover_layout.setBackgroundColor(backgroundColor);

            // set search icon color
            final Drawable search = ContextCompat.getDrawable(this, R.drawable.ic_search_book);
            search.setColorFilter(fontColor, PorterDuff.Mode.SRC_ATOP);
            searchHandler.postDelayed(new Runnable() {
                public void run() {
                    if (toolbarMenu == null) {
                        searchHandler.postDelayed(this, 100);
                    } else {
                        toolbarMenu.findItem(R.id.action_search).setIcon(search);
                    }
                }
            }, 100);
        }
    }

    public void buildAudioList(BookItem item) {

        // обновляю информацию о книге во фрагменте
        BookPageFragment fragment = (BookPageFragment) getSupportFragmentManager().findFragmentById(R.id.bookDetail);
        fragment.fillBookData(item);

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        audioIndex = 0;

        // нужно заполнить очередь урлами из базы попорядку
        Cursor c = db.rawQuery(
                "SELECT id, url, seconds FROM bookfile WHERE book_id = " + item.id + " ORDER BY _order ASC",
                new String[] {}
        );
        audioList = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                audioList.add(
                        new Audio(
                                c.getString(c.getColumnIndex("url")),
                                c.getInt(c.getColumnIndex("id")),
                                c.getInt(c.getColumnIndex("seconds")),
                                item.id,
                                item.name,
                                item.author_name,
                                item.cover
                        )
                );
            } while (c.moveToNext());
        }
        c.close();

        // Если у этой книги нет файлов, то нечего и воспроизводить. Просто выходим
        if (audioList.size() == 0) {
            fillPlayerData(new PlayerInfo(0, 0));
            return;
        }

        // Пробую вытащить автозакладку по книге
        c = db.rawQuery(
                "SELECT file_id, position FROM autobookmark WHERE book_id = " + item.id,
                new String[] {}
        );
        int autobookmarkFileId = 0;
        autoBookmarkPosition = 0;
        if (c.moveToFirst()) {
            do {
                autobookmarkFileId = c.getInt(c.getColumnIndex("file_id"));
                autoBookmarkPosition = c.getInt(c.getColumnIndex("position"));
            } while (c.moveToNext());
        }
        c.close();

        int duration = 0; // длина отрывка в секундах

        if (autobookmarkFileId > 0) {
            // если автозакладка в базе была, пытаюсь получить индекс файла на который она указывает
            for (Audio audio : audioList) {

                if (audio.getFileId() == autobookmarkFileId) {
                    duration = audio.getDuration();
                    break;
                }
                audioIndex += 1;
            }

        } else {
            // если автозакладки нет, сохраняю ее с позицией 0 TODO: вынести в отдельный таск
            int file_id = audioList.get(0).getFileId();
            db.execSQL(
                    "INSERT INTO autobookmark (book_id, file_id, position) VALUES (" + item.id + ", " + file_id + ", 0)",
                    new String[] {}
            );
            duration = audioList.get(0).getDuration();
        }

        fillPlayerData(new PlayerInfo(autoBookmarkPosition, duration * 1000 - 1));

    }

    public void playAudio() {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudio(audioList);
        storage.storeAudioIndex(audioIndex);
        storage.storeAudioBookmarkPosition(autoBookmarkPosition);

        Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
        sendBroadcast(broadcastIntent);
        myHandler.postDelayed(UpdateSongTime, 200);
    }

    public void pauseAudio() {
        Intent broadcastIntent = new Intent(Broadcast_PAUSE_AUDIO);
        sendBroadcast(broadcastIntent);
        myHandler.removeCallbacks(UpdateSongTime);
    }

    public void resumeAudio() {
        Intent broadcastIntent = new Intent(Broadcast_RESUME_AUDIO);
        sendBroadcast(broadcastIntent);
        myHandler.postDelayed(UpdateSongTime, 200);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }

    public void fillPlayerData(PlayerInfo info) {

        BookPageFragment fragment = (BookPageFragment) getSupportFragmentManager().findFragmentById(R.id.bookDetail);
        if (fragment == null) {
            return;
        }

        if (info == null) {
            info = player.info();
        }
        fragment.fillPlayerData(info);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (context == null || !serviceBound) {
                return;
            }
            fillPlayerData(null);
            myHandler.postDelayed(this, 200);
        }
    };

}
