package ru.ptitsyn.afinal.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "zvukislov", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // таблица с книгами
        db.execSQL(createBookSQL());

        // таблица с авторами
        db.execSQL(
                "CREATE TABLE author (id INTEGER PRIMARY KEY, cover_name TEXT NOT NULL);"
        );

        // связь авторов и книг
        db.execSQL(
                "CREATE TABLE book_author (id INTEGER PRIMARY KEY, book_id INTEGER, author_id INTEGER, FOREIGN KEY(book_id) REFERENCES book(id), FOREIGN KEY(author_id) REFERENCES author(id));"
        );

        // таблица с аудиофайлами
        db.execSQL(
                "CREATE TABLE bookfile (id INTEGER PRIMARY KEY, url TEXT NOT NULL, seconds INTEGER DEFAULT 0, bytes INTEGER DEFAULT 0, _order INTEGER DEFAULT 0, book_id INTEGER, FOREIGN KEY(book_id) REFERENCES book(id));"
        );

        // таблица с автозакладками
        db.execSQL(
                "CREATE TABLE autobookmark (id INTEGER PRIMARY KEY, book_id INTEGER, file_id INTEGER, position INTEGER NOT NULL, FOREIGN KEY(book_id) REFERENCES book(id), FOREIGN KEY(file_id) REFERENCES bookfile(id));"
        );

        // таблица с нишами
        db.execSQL(createNicheSQL());

        // таблица с жанрами
        db.execSQL(createGenreSQL());

        // связь жанров и книг
        db.execSQL(createBookGenreSQL());

        // таблица с подборками
        db.execSQL(createBooksetSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion == 1 && newVersion == 2) {
            db.beginTransaction();
            try {
                // таблица с нишами
                db.execSQL(createNicheSQL());

                // таблица с жанрами
                db.execSQL(createGenreSQL());

                // связь жанров и книг
                db.execSQL(createBookGenreSQL());

                // таблица с подборками
                db.execSQL(createBooksetSQL());

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } else if (oldVersion == 2 && newVersion == 3) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TEMPORARY TABLE temp_book(id INTEGER, name TEXT, cover TEXT, annotation TEXT);");
                db.execSQL("INSERT INTO temp_book SELECT id, name, cover, annotation FROM book;");
                db.execSQL("DROP TABLE book;");
                db.execSQL(createBookSQL());
                db.execSQL("INSERT INTO book (id, name, cover, annotation) SELECT id, name, cover, annotation FROM temp_book;");
                db.execSQL("DROP TABLE temp_book;");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

    }

    // 1 migration

    private String createNicheSQL() {
        return "CREATE TABLE niche (id INTEGER PRIMARY KEY, name TEXT NOT NULL, _order INTEGER DEFAULT 0, image TEXT NOT NULL, book_count INTEGER DEFAULT 0);";
    }

    private String createGenreSQL() {
        return "CREATE TABLE genre (id INTEGER PRIMARY KEY, name TEXT NOT NULL, niche_id INTEGER, book_count INTEGER DEFAULT 0, FOREIGN KEY(niche_id) REFERENCES niche(id));";
    }

    private String createBookGenreSQL() {
        return "CREATE TABLE book_genre (id INTEGER PRIMARY KEY, book_id INTEGER, genre_id INTEGER, FOREIGN KEY(book_id) REFERENCES book(id), FOREIGN KEY(genre_id) REFERENCES genre(id));";
    }

    private String createBooksetSQL() {
        return "CREATE TABLE bookset (id INTEGER PRIMARY KEY, name TEXT NOT NULL, description TEXT, image TEXT NOT NULL, book_count INTEGER DEFAULT 0);";
    }

    // 2 migration

    private String createBookSQL() {
        return "CREATE TABLE book (id INTEGER PRIMARY KEY, name TEXT NOT NULL, cover TEXT NOT NULL, annotation TEXT NOT NULL, background_color TEXT DEFAULT 'ffffff', font_color TEXT DEFAULT '000000', link_color TEXT DEFAULT '000000');";
    }

}
