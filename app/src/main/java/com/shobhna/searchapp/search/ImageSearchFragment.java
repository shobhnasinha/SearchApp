package com.shobhna.searchapp.search;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shobhna.searchapp.R;
import com.shobhna.searchapp.model.SearchQuery;
import com.shobhna.searchapp.util.ImageCaches;
import com.shobhna.searchapp.util.Utils;
import com.google.api.services.customsearch.model.Result;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/*
* Subclass of BaseSearchFragment which implements SearchContract.View.
* It gets its data from Presenter.
* It is responsible for displaying 'Image Result' (image) search results
* It sets image mode as true in its newInstance method
*/
public class ImageSearchFragment extends BaseSearchFragment {

    private ImageSearchAdapter mListAdapter;

    private static final String TAG = "ImageSearchFragment";

    /*
    * It sets image mode as true in its newInstance method
    */
    public static ImageSearchFragment newInstance() {
        ImageSearchFragment imageSearchFragment = new ImageSearchFragment();
        imageSearchFragment.setIsImageSearchMode(true);
        return imageSearchFragment;
    }

    public ImageSearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new ImageSearchAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // The super method inflates layout and initiates 'Next' and 'Previous' search page UI
        View root = super.onCreateView(inflater, container, savedInstanceState);

        if (root != null) {
            // Use RecyclerView to display search results
            mRecyclerView = (RecyclerView) root.findViewById(R.id.web_search_list);
            mRecyclerView.setAdapter(mListAdapter);
            // Set Fixed Size to true for optimization
            mRecyclerView.setHasFixedSize(true);
            // Grid Layout Manager for Web Search Result
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            mRecyclerView.setLayoutManager(gridLayoutManager);
            // Initially make RecyclerView Gone to show empty page icon
            mRecyclerView.setVisibility(View.GONE);
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Prepare SearchQuery object to do image search
        SearchQuery searchQuery = new SearchQuery(mQueryString, true, false, true);
        setQueryString(searchQuery);
    }

    @Override
    public void setResultItems(SearchQuery searchQuery, List<Result> resultList) {
        if (resultList != null) {
            if (resultList.size() > 0) {
                // Make RecyclerView visible to display search results
                mRecyclerView.setVisibility(View.VISIBLE);
                // Set Adapter with search results
                mListAdapter.setResultList(resultList);
            }
            // Set next and previous page setting by calling super class method
            super.setResultItems(searchQuery, resultList);
        }
    }

    private static class ImageSearchAdapter extends RecyclerView.Adapter<ImageSearchAdapter.ViewHolder> {

        private Context mContext;

        private List<Result> mSearchResultList;

        // Place holder bitmap while the weather icon is downloaded
        private Bitmap mPlaceHolderBitmap;

        public ImageSearchAdapter(Context context) {
            mContext = context;
            mSearchResultList = new ArrayList<>(10);
            mPlaceHolderBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.placeholder_image);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View searchItemView = inflater.inflate(R.layout.search_grid_item, parent, false);
            return new ViewHolder(searchItemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Result result = mSearchResultList.get(position);
            if (result != null && result.getImage() != null) {
                String resUrl = result.getImage().getThumbnailLink();
                loadBitmap(position, resUrl, viewHolder.image);
            }
        }

        @Override
        public int getItemCount() {
            return mSearchResultList.size();
        }

        public void setResultList(List<Result> results) {
            mSearchResultList = results;
            notifyDataSetChanged();
        }

        public Result getItem(int position) {
            return mSearchResultList.get(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.search_result_image);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                Result result = getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(result.getLink()));
                mContext.startActivity(intent);
            }
        }

        private void loadBitmap(Integer position, String resUrl, ImageView imageView) {
            if (cancelPotentialWork(position, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView, resUrl);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(position);
            }
        }

        public static boolean cancelPotentialWork(Integer position, ImageView imageView) {
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (bitmapWorkerTask != null) {
                final Integer bitmapPosition = bitmapWorkerTask.position;
                // If bitmapPosition is not yet set or it differs from the new position
                if (bitmapPosition == null || bitmapPosition == -1 || !bitmapPosition.equals(position)) {
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                } else {
                    // The same work is already in progress
                    return false;
                }
            }
            // No task associated with the ImageView, or an existing task was cancelled
            return true;
        }

        private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
            if (imageView != null) {
                final Drawable drawable = imageView.getDrawable();
                if (drawable instanceof AsyncDrawable) {
                    final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                    return asyncDrawable.getBitmapWorkerTask();
                }
            }
            return null;
        }

        static class AsyncDrawable extends BitmapDrawable {
            private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

            public AsyncDrawable(Resources res, Bitmap bitmap,
                                 BitmapWorkerTask bitmapWorkerTask) {
                super(res, bitmap);
                bitmapWorkerTaskReference =
                        new WeakReference<>(bitmapWorkerTask);
            }

            public BitmapWorkerTask getBitmapWorkerTask() {
                return bitmapWorkerTaskReference.get();
            }
        }

        class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

            private final WeakReference<ImageView> imageViewReference;
            // Adapter position
            private Integer position = -1;

            private String imageUrl;

            public BitmapWorkerTask(ImageView imageView, String url) {
                // Use a WeakReference to ensure the ImageView can be garbage collected
                imageViewReference = new WeakReference<>(imageView);
                imageUrl = url;
            }

            @Override
            protected Bitmap doInBackground(Integer... params) {
                position = params[0];
                Bitmap bm = ImageCaches.getBitmapFromMemCache(position);
                if (bm == null) {
                    if (imageUrl == null) {
                        return null;
                    }
                    bm = downloadBitmap(imageUrl);
                    ImageCaches.addBitmapToMemoryCache(position, bm);
                }
                return bm;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if (imageViewReference != null && bitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                    if (this == bitmapWorkerTask && imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }

        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap;
            if (imageUrl == null || imageUrl.isEmpty() || !Utils.isNetworkAvailable(mContext)) {
                return null;
            } else {
                bitmap = Utils.getBitmapFromURL(imageUrl);
            }
            return bitmap;
        }
    }
}
