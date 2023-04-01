package edu.uci.ics.fabflixmobile.ui.mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import edu.uci.ics.fabflixmobile.databinding.ActivityMainpageBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class MainPageActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainpageBinding binding = ActivityMainpageBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        SearchView searchContent = binding.simpleSearchView;
        searchContent.setIconified(false);
        searchContent.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                MovieListActivity.queryContent = s;
                Intent MovieList = new Intent(MainPageActivity.this, MovieListActivity.class);
                startActivity(MovieList);
                return false;
            }

            // for auto fill
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
}