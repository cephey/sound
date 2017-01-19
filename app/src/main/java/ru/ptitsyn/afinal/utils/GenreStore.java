package ru.ptitsyn.afinal.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.ptitsyn.afinal.models.Genre;

public class GenreStore {

    public static List<Genre> fetchGenreList(int nicheId, Context context) {

        List<Genre> items = new ArrayList<>();

        // создаем объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // вытаскиваю из базы жанры, которые относятся к этой нише
        Cursor c = db.rawQuery(
                "SELECT id, name, book_count FROM genre where niche_id = " + nicheId + " and book_count > 0",
                new String[] {}
        );
        if (c.moveToFirst()) {
            do {
                items.add(
                        new Genre(
                                c.getInt(c.getColumnIndex("id")),
                                c.getString(c.getColumnIndex("name")),
                                nicheId,
                                c.getInt(c.getColumnIndex("book_count"))
                        )
                );
            } while (c.moveToNext());
        }
        c.close();

        return items;
    }

}
