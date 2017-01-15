package com.shobhna.searchapp.search;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.shobhna.searchapp.MainActivity;
import com.shobhna.searchapp.R;
import com.shobhna.searchapp.model.SearchQuery;
import com.shobhna.searchapp.util.EspressoIdlingResource;
import com.shobhna.searchapp.util.ImageCaches;
import com.shobhna.searchapp.util.Utils;
import com.google.api.services.customsearch.model.Result;

import java.lang.ref.WeakReference;
import java.util.List;

/*
* Parent class of WebSearchFragment and ImageSearchFragment.
* It handles the common method and logic for Fragment lifecycle as well as
* logic related to SearchContract.View.
*
* Sub classes through 'newInstance' method sets whether the image type searching is needed
*
* It implements the SearchContract.View interface and gets its data from Presenter
*/
public abstract class BaseSearchFragment extends Fragment implements SearchContract.View {

    protected String mQueryString;

    protected SearchContract.SearchModeListener mSearchModeListener;

    protected ProgressDialog mProgressDialog;

    protected SearchContract.Presenter mActionsListener;

    protected long mStartIndex = 1L;

    protected CardView mPageChangeView;

    protected TextView mPrevPageTv, mNextPageTv;

    protected RecyclerView mRecyclerView;

    protected QueryHandler mHandler;

    private static final int SEARCH = 16;

    private static final int UPDATE_QUERY_INTERVAL = 200;

    private boolean mIsPreviousStateConnected = false;

    private boolean mIsImageSearchMode = false;

    private static final String TAG = "BaseSearchFragment";

    /*
    * Update the search results when network is connected
    */
    private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED && getActivity() != null) {
                if (!mIsPreviousStateConnected) {
                    SearchQuery searchQuery = new SearchQuery(mQueryString, true, false);
                    setQueryString(searchQuery);
                }
                mIsPreviousStateConnected = true;
            } else {
                mIsPreviousStateConnected = false;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSearchModeListener = (SearchContract.SearchModeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SearchModeListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        * NOTE:
        * We are using the Injection class to retrieve an instance of the books repository.
        * Using this class makes it easy to swap out the implementation during testing later on -
        * we can simply inject a different service layer in our tests,
        * perhaps one that only emulates parts of the functionality in a predefined way.
        */
        // Initialize presenter for the fragment
        mActionsListener = new SearchPresenter(Injection.provideSearchRepository(), this);
        mHandler = new QueryHandler(mActionsListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.search_layout, container, false);

        // Initialize Page Change View with previous and next page UI
        mPageChangeView = (CardView) root.findViewById(R.id.page_change_web_search_view);
        mPrevPageTv = (TextView) mPageChangeView.findViewById(R.id.prev_page_tv);
        mPrevPageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartIndex > 10) {
                    mStartIndex -= 10;
                    SearchQuery searchQuery = new SearchQuery(mQueryString, false, true);
                    setQueryString(searchQuery);
                    clearSearchViewFocus();
                }


            }
        });
        mNextPageTv = (TextView) mPageChangeView.findViewById(R.id.next_page_tv);
        mNextPageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartIndex += 10;
                SearchQuery searchQuery = new SearchQuery(mQueryString, false, true);
                setQueryString(searchQuery);
                clearSearchViewFocus();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set the fragment to retain state on orientation change
        setRetainInstance(true);

        if (mQueryString == null || mQueryString.isEmpty()) {
            mQueryString = ((MainActivity) getActivity()).getQueryString();
        }

        hideKeyboard();
    }

    @Override
    public void onStart() {
        super.onStart();
        clearSearchViewFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver to listen for network changes
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mNetworkStateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver to listen for network changes
        try {
            getActivity().unregisterReceiver(mNetworkStateReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove all pending messages to avoid memory leak problems
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void clearSearchViewFocus() {
        ((MainActivity) getActivity()).clearSearchViewFocus();
    }

    /*
    * QueryHandler to facilitate isolation and serialization between search queries.
    * The Handler is made static and holds weak reference of presenter to avoid memory leak
    */
    protected static final class QueryHandler extends Handler {

        private final WeakReference<SearchContract.Presenter> actionListenerRef;

        private QueryHandler(SearchContract.Presenter actionListener) {
            this.actionListenerRef = new WeakReference<>(actionListener);
        }

        @Override
        public void handleMessage(Message msg) {
            final SearchContract.Presenter actionsListener = actionListenerRef.get();
            if (actionsListener != null) {
                switch (msg.what) {
                    case SEARCH:
                        SearchQuery searchQuery = (SearchQuery) msg.obj;
                        actionsListener.loadSearchResults(searchQuery);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /*
    * Show Progress Indicator indicating background fetching of information
    */
    @Override
    public void setProgressIndicator(boolean active, int msgId) {
        if (active) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                return;
            }
            mProgressDialog = ProgressDialog.show(getActivity(),
                    getActivity().getString(msgId), "", true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        } else {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    /*
    * Query string to search on Google for
    */
    @Override
    public void setQueryString(SearchQuery searchQuery) {
        if (searchQuery.getQuery() == null || searchQuery.getQuery().isEmpty()) {
            return;
        }

        if (searchQuery.isNewSearch()) {
            mStartIndex = 1L;
        }
        searchQuery.setStartIndex(mStartIndex);

        if (mIsImageSearchMode) {
            ImageCaches.clearLruCache();
            searchQuery.setWithImage(true);
        } else {
            searchQuery.setWithImage(false);
        }

        mQueryString = searchQuery.getQuery().trim();

        if (getActivity() != null) {
            if (Utils.isNetworkAvailable(getActivity())) {

                EspressoIdlingResource.increment(); // App is busy until further notice

                // facilitate serialization by using Handler
                Message msg = mHandler.obtainMessage();
                msg.what = SEARCH;
                msg.obj = searchQuery;
                mHandler.removeMessages(SEARCH);
                mHandler.sendMessageDelayed(msg, UPDATE_QUERY_INTERVAL);

            } else {
                setUpSnackBar();
            }
        }
    }

    /*
    * Set whether the search type is Image
    */
    @Override
    public void setIsImageSearchMode(boolean withImage) {
        mIsImageSearchMode = withImage;
    }

    /*
    * Set search results to be displayed as search query result
    */
    @Override
    public void setResultItems(SearchQuery searchQuery, List<Result> resultList) {
        mPageChangeView.setVisibility(View.VISIBLE);
        if (resultList.size() < 10) {
            mNextPageTv.setVisibility(View.INVISIBLE);
        } else {
            mNextPageTv.setVisibility(View.VISIBLE);
            long nextPageNo = ((mStartIndex + 10) / 10) + 1;
            mNextPageTv.setText("Page " + nextPageNo + " >");
            long prevPageNo = nextPageNo - 2;
            if (prevPageNo >= 1) {
                mPrevPageTv.setVisibility(View.VISIBLE);
                mPrevPageTv.setText("< Page " + prevPageNo);
            } else {
                mPrevPageTv.setVisibility(View.INVISIBLE);
            }
        }
        EspressoIdlingResource.decrement(); // Set app as idle.
    }

    // Show SnackBar to user indicating Network is off.
    protected void setUpSnackBar() {
        if (getView() != null) {
            Snackbar snackbar = Snackbar.make(getView(), R.string.nw_off, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    private void hideKeyboard() {
        View view = this.getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
