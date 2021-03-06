package com.peterark.popularmovies.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.peterark.popularmovies.popularmovies.models.MovieItem;
import com.peterark.popularmovies.popularmovies.utils.MovieHelperUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder>{

    private final Context mContext;
    private final OnMovieClickHandler mOnClickHandler;
    private List<MovieItem> mItemList;

    public MoviesAdapter(Context context,OnMovieClickHandler handler ){
        this.mContext = context;
        this.mOnClickHandler = handler;
    }

    public interface OnMovieClickHandler{
        void onClick(MovieItem item);
    }


    public void setItemList(List<MovieItem> itemList){
        this.mItemList = itemList;
        notifyDataSetChanged();
    }


    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context         = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        int layoutId  = R.layout.list_item_movie;

        View view = inflater.inflate(layoutId, parent, false);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MoviesAdapterViewHolder holder, int position) {
        MovieItem item = mItemList.get(position);
        Picasso.with(mContext)
                .load(MovieHelperUtils.imageBaseUrl.concat(item.moviePosterUrl()))                    // Loading ImageUrl
                .placeholder(R.drawable.ic_image_placeholder)   // PlaceHolder Image (until loading finishes)
                .error(R.drawable.ic_loading_error)             // Error Image (if loading fails)
                .into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ImageView posterImageView;

        private MoviesAdapterViewHolder(View view){
            super(view);

            // Get Layout items reference
            posterImageView             = (ImageView) view.findViewById(R.id.poster_holder);

            // Set On Click Listener
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            MovieItem item = mItemList.get(adapterPosition);
            mOnClickHandler.onClick(item);
        }
    }

}
