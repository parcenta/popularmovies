package com.peterark.popularmovies.popularmovies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peterark.popularmovies.popularmovies.models.MovieItem;
import com.peterark.popularmovies.popularmovies.utils.MovieHelperUtils;
import com.peterark.popularmovies.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    // Loading Movies AsyncTask
    private LoadMoviesTask loadMoviesTask;

    // Layout Items
    private RecyclerView moviesRecyclerView;
    private ProgressBar progressBar;
    private TextView errorOccurredTextView;
    private TextView noMoviesAvailableTextView;

    // Values
    private String orderMode;
    private MoviesAdapter adapter;
    private List<MovieItem> moviesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Get Layout items
        moviesRecyclerView          = (RecyclerView) findViewById(R.id.movies_recycler_view);
        progressBar                 = (ProgressBar) findViewById(R.id.loading_movies_progress_bar);
        errorOccurredTextView       = (TextView) findViewById(R.id.error_text_view);
        noMoviesAvailableTextView   = (TextView)  findViewById(R.id.no_movies_found_text_view);

        // Setting adapter
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setHasFixedSize(true);
        adapter = new MoviesAdapter(this);
        moviesRecyclerView.setAdapter(adapter);

        // Check if there is a savedInstanceState. If there is then we recover the list.
        /*if(savedInstanceState != null) {

        }else{*/
            // Set the Order Mode initially as Most Popular.
            orderMode = Constants.ORDER_BY_MOST_POPULAR;

            // If not, then load the movies from server
            loadMoviesTask = new LoadMoviesTask();
            loadMoviesTask.execute();
        //}

        // Show in the Action Bar Title the OrderMode selected.
        refreshActionBarTitle();

    }

    private void refreshActionBarTitle(){
        String orderModeDescription = Constants.orderModeDescription(this,orderMode);
        getSupportActionBar().setTitle(orderModeDescription); // I manage the possible NullPointer inside the above method.
    }

    // --------------------------------------------------------
    //  Loading Movies AsyncTask
    // --------------------------------------------------------
    private class LoadMoviesTask extends AsyncTask<Void,Void,List<MovieItem>> {

        @Override
        protected void onPreExecute() {
            // First Hide the RecyclerView and the Error Message.
            moviesRecyclerView.setVisibility(View.INVISIBLE);
            errorOccurredTextView.setVisibility(View.INVISIBLE);
            noMoviesAvailableTextView.setVisibility(View.INVISIBLE);

            // Show Progress Bar.
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<MovieItem> doInBackground(Void... params) {

            // Get the Url depending in the request mode (movies ordered by most_popular or top_rated).
            URL weatherRequestUrl = NetworkUtils.buildUrl(orderMode);

            try {
                String response = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl);

                Log.d(TAG,"JsonString Response: " + response);

                return MovieHelperUtils.getMovieListFromJson(response);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<MovieItem> itemList) {

            // Hide the Progress Bar
            progressBar.setVisibility(View.INVISIBLE);

            // Set the Adapter List.
            moviesList = itemList;
            adapter.setItemList(moviesList);

            // Depending if the list was loaded correctly, we show it o
            if(moviesList!=null) {
                Log.d(TAG,"ItemList size: " + itemList.size());
                if (moviesList.size()>0)
                    moviesRecyclerView.setVisibility(View.VISIBLE);
                else
                    noMoviesAvailableTextView.setVisibility(View.VISIBLE);
            }
            else
                errorOccurredTextView.setVisibility(View.VISIBLE);

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(loadMoviesTask!=null)
            loadMoviesTask.cancel(true);
    }

}
