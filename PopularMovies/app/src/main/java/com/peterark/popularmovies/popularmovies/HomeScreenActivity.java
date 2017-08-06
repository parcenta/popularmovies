package com.peterark.popularmovies.popularmovies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.peterark.popularmovies.popularmovies.detailPanel.MovieDetailActivity;
import com.peterark.popularmovies.popularmovies.models.MovieItem;
import com.peterark.popularmovies.popularmovies.utils.MovieHelperUtils;
import com.peterark.popularmovies.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity implements MoviesAdapter.OnMovieClickHandler {

    //
    private final String TAG = this.getClass().getSimpleName();
    private final String MOVIES_LIST  = "ITEM_LIST";
    private final String ORDER_BY   = "ORDER_BY";


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
    private ArrayList<MovieItem> moviesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Get Layout items
        moviesRecyclerView          = (RecyclerView) findViewById(R.id.movies_recycler_view);
        progressBar                 = (ProgressBar) findViewById(R.id.loading_movies_progress_bar);
        errorOccurredTextView       = (TextView) findViewById(R.id.error_text_view);
        noMoviesAvailableTextView   = (TextView)  findViewById(R.id.no_movies_found_text_view);

        // Set Extra Behaviour to Layout items
        errorOccurredTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMovies();
            }
        });

        // Setting adapter
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setHasFixedSize(true);
        adapter = new MoviesAdapter(this,this);
        moviesRecyclerView.setAdapter(adapter);

        // Check if there is a savedInstanceState. If there is then we recover the list.
        if(savedInstanceState != null
                && savedInstanceState.containsKey(ORDER_BY)
                && savedInstanceState.containsKey(MOVIES_LIST)) {

            Log.d(TAG,"savedInstance variable will be recovered");

            // Get SavedInstance variables
            orderMode   = savedInstanceState.getString(ORDER_BY);
            moviesList  = savedInstanceState.getParcelableArrayList(MOVIES_LIST);

            // Now with the restored variables an update the UI (Action Bar and Recycler View)
            refreshActionBarTitle();
            adapter.setItemList(moviesList);
        }else{
            Log.d(TAG,"savedInstance variables are not available. We load the data from WS...");
            // Set the Order Mode initially as Most Popular.
            orderMode = Constants.ORDER_BY_MOST_POPULAR;

            // If not, then load the movies from server
            loadMovies();
        }

    }


    private void loadMovies(){
        // Set in the ActionBar title, by what order are the movies.
        refreshActionBarTitle();

        // Cancel previous request
        cancelLoadingMovies();

        // Load Movies.
        loadMoviesTask = new LoadMoviesTask();
        loadMoviesTask.execute();
    }

    private void refreshActionBarTitle(){
        String orderModeDescription = Constants.orderModeDescription(this,orderMode);
        getSupportActionBar().setTitle(orderModeDescription); // I manage the possible NullPointer inside the above method.
    }

    // --------------------------------------------------------
    //  Menu Stuff
    // --------------------------------------------------------


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.order_by_most_popular:
                orderMode = Constants.ORDER_BY_MOST_POPULAR;
                loadMovies();
                break;
            case R.id.order_by_top_rated:
                orderMode = Constants.ORDER_BY_TOP_RATED;
                loadMovies();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(MovieItem item) {

        if (item == null) {
            Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            return;
        }

        // Launch the MovieDetailActivity with the selected movieId.
        MovieDetailActivity.launch(this,item.movieId());
    }

    // --------------------------------------------------------
    //  Loading Movies AsyncTask
    // --------------------------------------------------------
    private class LoadMoviesTask extends AsyncTask<Void,Void,ArrayList<MovieItem>> {

        @Override
        protected void onPreExecute() {

            // Set the MoviesList to null.
            moviesList = null;

            // Empty (null) the Item List inside the adapter.
            adapter.setItemList(null);

            // First Hide the RecyclerView and the Error Message.
            moviesRecyclerView.setVisibility(View.INVISIBLE);
            errorOccurredTextView.setVisibility(View.INVISIBLE);
            noMoviesAvailableTextView.setVisibility(View.INVISIBLE);

            // Show Progress Bar.
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<MovieItem> doInBackground(Void... params) {

            // Get the Url depending in the request mode (movies ordered by most_popular or top_rated).
            URL weatherRequestUrl = NetworkUtils.buildUrl(orderMode,null);

            try {
                String response = NetworkUtils
                        .getResponseFromHttpUrl(HomeScreenActivity.this,weatherRequestUrl);

                if(response==null)
                    return null;

                Log.d(TAG,"JsonString Response: " + response);

                List<MovieItem> itemList = MovieHelperUtils.getMovieListFromJson(response);

                return new ArrayList<>(itemList);

            } catch (Exception e) {
                Log.d(TAG,"Exception was thrown when loading movies...");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> itemList) {

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
        cancelLoadingMovies();
    }

    private void cancelLoadingMovies(){
        if(loadMoviesTask!=null)
            loadMoviesTask.cancel(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Avoid saving in the instanceState the resultantMovieList.
        if(moviesList==null) {
            Log.d(TAG,"Avoiding saving moviesList in savedInstanceState...");
            super.onSaveInstanceState(outState);
            return;
        }

        outState.putString(ORDER_BY,orderMode);
        outState.putParcelableArrayList(MOVIES_LIST,moviesList);
        super.onSaveInstanceState(outState);
    }
}
