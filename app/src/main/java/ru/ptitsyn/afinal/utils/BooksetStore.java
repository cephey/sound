package ru.ptitsyn.afinal.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ru.ptitsyn.afinal.models.Bookset;

public class BooksetStore {

    public static List<Bookset> fetchBooksetList(String path, Context context) {

        List<Bookset> booksetsDB = new ArrayList<>();
        HashSet<Integer> bookset_ids = new HashSet<>();

        URL url = null;
        try {
            url = new URL(AuthUtil.domain + path);
        } catch (MalformedURLException e) {
            Log.e("EA_DEMO", "Error build niche url", e);
        }
        path = null;

        try{
            JSONObject response = new JSONObject(StoreUtil.fetch(url));

            String next_page = response.getString("next");
            if (!next_page.equals("null")) {
                path = next_page;
            }

            JSONArray booksets = response.getJSONArray("results");

            for (int i = 0; i < booksets.length(); i++) {
                JSONObject bookset = booksets.getJSONObject(i);

                int bookset_id = bookset.getInt("id");
                String bookset_name = bookset.getString("name");
                String bookset_description = bookset.getString("description");
                String bookset_image = bookset.getString("image");
                int bookset_book_count = bookset.getInt("book_count");

                if (!bookset_ids.contains(bookset_id)) {
                    booksetsDB.add(new Bookset(bookset_id, bookset_name, bookset_description, bookset_image, bookset_book_count));
                    bookset_ids.add(bookset_id);
                }
            }

        } catch (Exception e){
            Log.e("EA_DEMO","Error fetching data", e);
        }

        // объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (booksetsDB.size() > 0) {
            StoreUtil.storeBooksets(booksetsDB, "bookset", db);
        }

//        if (path != null) {
//            nichesDB.addAll(fetchNiches(path));
//        }

        return booksetsDB;
    }

    public static Bookset fetchBooksetDetail(int booksetId, Context context) {

        Bookset item = null;

        // создаем объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // вытаскиваю из базы жанры, которые относятся к этой нише
        Cursor c = db.rawQuery(
                "SELECT id, name, description, image, book_count FROM bookset WHERE id = " + booksetId,
                new String[] {}
        );
        if (c.moveToFirst()) {
            item = new Bookset(
                    c.getInt(c.getColumnIndex("id")),
                    c.getString(c.getColumnIndex("name")),
                    c.getString(c.getColumnIndex("description")),
                    c.getString(c.getColumnIndex("image")),
                    c.getInt(c.getColumnIndex("book_count"))
            );
        }
        c.close();

        return item;
    }

}
