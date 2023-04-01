package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

public class SingleMovieActivity extends AppCompatActivity {

    public static Movie thisMovie;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);
        TextView title = findViewById(R.id.title);
        TextView year = findViewById(R.id.year);
        TextView rating = findViewById(R.id.rating);
        TextView director = findViewById(R.id.director);
        TextView genres = findViewById(R.id.genres);
        TextView stars = findViewById(R.id.stars);

        title.setText(thisMovie.getTitle());
        year.setText(thisMovie.getYear() + "");
        rating.setText(thisMovie.getRating());
        director.setText(thisMovie.getDirector());
        genres.setText(thisMovie.getGenreString(-1));
        stars.setText(thisMovie.getStarString(-1));

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());
    }

}
