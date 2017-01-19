package ru.ptitsyn.afinal.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ru.ptitsyn.afinal.models.Genre;
import ru.ptitsyn.afinal.models.Niche;

public class NicheStore {

    public static List<Niche> fetchNicheList(String path, Context context) {

        List<Niche> nichesDB = new ArrayList<>();
        HashSet<Integer> niche_ids = new HashSet<>();

        List<Genre> genresDB = new ArrayList<>();
        HashSet<Integer> genre_ids = new HashSet<>();

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

            JSONArray niches = response.getJSONArray("results");

            for (int i = 0; i < niches.length(); i++) {
                JSONObject niche = niches.getJSONObject(i);

                int niche_id = niche.getInt("id");
                String niche_name = niche.getString("name");
                int niche_order = niche.getInt("order");
                String niche_image = niche.getString("image");
                int niche_book_count = niche.getInt("book_count");

                if (!niche_ids.contains(niche_id)) {
                    nichesDB.add(new Niche(niche_id, niche_name, niche_order, niche_image, niche_book_count));
                    niche_ids.add(niche_id);
                }

                JSONArray genres = niche.getJSONArray("genres");
                for (int j = 0; j < genres.length(); j++) {
                    JSONObject genre = genres.getJSONObject(j);

                    int genre_id = genre.getInt("id");
                    String genre_name = genre.getString("name");
                    int genre_book_count = genre.getInt("book_count");

                    if (!genre_ids.contains(genre_id)) {
                        genresDB.add(new Genre(genre_id, genre_name, niche_id, genre_book_count));
                        genre_ids.add(genre_id);
                    }
                }
            }

        } catch (Exception e){
            Log.e("EA_DEMO","Error fetching data", e);
        }

        // объект для создания и управления версиями БД
        DBHelper dbHelper = new DBHelper(context);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (nichesDB.size() > 0) {
            StoreUtil.storeNiches(nichesDB, "niche", db);
        }
        if (genresDB.size() > 0) {
            StoreUtil.storeGenres(genresDB, "genre", db);
        }

        if (path != null) {
            nichesDB.addAll(fetchNicheList(path, context));
        }
        return nichesDB;
    }

}
