package edu.uci.ics.fabflixmobile.ui.movielist;

import static java.lang.Math.max;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;

public class MovieListActivity extends AppCompatActivity {
    public static String queryContent;
    public static int page = 1;
    private TextView pageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        final ArrayList<Movie> movies = new ArrayList<>();
        getMovies(movies);

        Button prevButton = findViewById(R.id.prev_button);
        Button nextButton = findViewById(R.id.next_button);
        pageView = findViewById(R.id.page);
        pageView.setText(String.format(Locale.ENGLISH, "%d", page));
        prevButton.setOnClickListener(view -> flipOver(-1));
        nextButton.setOnClickListener(view -> flipOver(1));

        Button backButton = findViewById(R.id.backToSearch);
        backButton.setOnClickListener(view -> finish());
    }

    private void getMovies(ArrayList<Movie> movieList) {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest getMovieRequest = new StringRequest(
                Request.Method.POST,
                LoginActivity.baseURL + "/api/movies",
                response -> {
                    JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject obj = jsonArray.get(i).getAsJsonObject();
                        Movie movie = new Movie();
                        movie.setId(obj.get("movie_id").getAsString());
                        movie.setTitle(obj.get("title").getAsString());
                        movie.setYear(obj.get("year").getAsShort());
                        movie.setDirector(obj.get("director").getAsString());
                        movie.setRating(obj.get("rating").getAsString());
                        List<String> stars = obj.getAsJsonArray("star_name").asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
                        List<String> genres = obj.getAsJsonArray("genres").asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
                        movie.setGenres((ArrayList<String>) genres);
                        movie.setStars((ArrayList<String>) stars);
                        movieList.add(movie);
                    }
                    onResponse(movieList);
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("title", queryContent);
                params.put("page", Integer.toString(page));
                params.put("size", "20");
                return params;
            }
        };
        // important: queue.add is where the request is actually sent
        queue.add(getMovieRequest);
    }

    private void onResponse(ArrayList<Movie> movies) {
        if (movies.isEmpty()){
            page = max(page - 1, 1);
            return;
        }
        pageView.setText(String.format(Locale.ENGLISH, "%d", page));
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SingleMovieActivity.thisMovie = movies.get(position);
            Intent singleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            startActivity(singleMoviePage);
        });
    }

    private void flipOver(int direction) {
        page += direction;
        if (page <= 0) {
            page -= direction;
            return;
        }
        ArrayList<Movie> result = new ArrayList<>();
        getMovies(result);
    }
}