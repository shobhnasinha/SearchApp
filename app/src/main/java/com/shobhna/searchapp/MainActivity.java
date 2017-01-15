package com.shobhna.searchapp;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;
import android.widget.TextView;

import com.shobhna.searchapp.model.SearchQuery;
import com.shobhna.searchapp.search.ImageSearchFragment;
import com.shobhna.searchapp.search.SearchContract;
import com.shobhna.searchapp.search.WebSearchFragment;
import com.shobhna.searchapp.util.EspressoIdlingResource;

/*
* It holds 'WebSearchFragment' and 'ImageSearchFragment'.
* The switch between the fragment is done through ActionBar (TabLayout) tabs
* by implementing TabLayout.OnTabSelectedListener.
*
* The Activity holds the SearchView and implements SearchView.OnQueryTextListener
* Searching is done only when 'Submit' is done and not on text change.
*
* The search string is updated to fragments.
* This is done by the Activity by implementing  SearchContract.SearchModeListener
*/
public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,
        SearchView.OnQueryTextListener, SearchContract.SearchModeListener {

    // Holds 'Web Search' and 'Image Search' tabs
    private TabLayout mTabLayout;

    // It holds the current query string in search view
    private String mCurrentQueryString;

    /* The fragment to which this Activity updates the query string.
    *  It is updated to store the visible fragment (based on Tab selected)
    */
    private SearchContract.View mSearchFragmentView;

    // Search View common to both the fragments - WebSearch and ImageSearch
    private SearchView mSearchView;

    // Index of the currently selected tab
    private int mTabPos;

    private static final String QUERY_STRING = "query_string";

    private static final String TAB_POS = "tab_pos";

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.web_search));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.image_search));

        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
        // To set text color in SearchView
        int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null,
                null);
        TextView textView = (TextView) mSearchView.findViewById(id);
        textView.setTextColor(Color.WHITE);

        mTabLayout.setOnTabSelectedListener(this);
        if (savedInstanceState == null) {
            // Default visible fragment is WebSearchFragment
            addWebSearchFragment();
        } else {
            // Restore the last query string and tab position
            mCurrentQueryString = savedInstanceState.getString(QUERY_STRING);
            mTabPos = savedInstanceState.getInt(TAB_POS);
        }
        if (mTabLayout != null && mTabLayout.getTabAt(mTabPos) != null) {
            // Set the tab position
            mTabLayout.getTabAt(mTabPos).select();
            if (mTabPos == 0) {
                addWebSearchFragment();
            } else {
                addImageSearchFragment();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        clearSearchViewFocus();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the last query string and tab position
        outState.putString(QUERY_STRING, mCurrentQueryString);
        outState.putInt(TAB_POS, mTabPos);
        super.onSaveInstanceState(outState);
    }

    public void clearSearchViewFocus() {
        if (mSearchView != null) {
            mSearchView.setFocusable(false);
            mSearchView.clearFocus();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int pos = tab.getPosition();
        switch (pos) {
            case 0:
                mTabPos = 0;
                if (!(getFragmentManager().findFragmentById(R.id.search_fragment_container)
                        instanceof WebSearchFragment)) {
                    // Add WebSearchFragment
                    addWebSearchFragment();
                    if (mCurrentQueryString == null || mCurrentQueryString.isEmpty()) {
                        // Don't search for empty query strings
                        return;
                    }
                    // Prepare SearchQuery object to do web search
                    SearchQuery searchQuery = new SearchQuery(mCurrentQueryString, true, false, false);
                    loadSearchResult(searchQuery);
                }
                break;
            case 1:
                mTabPos = 1;
                if (!(getFragmentManager().findFragmentById(R.id.search_fragment_container)
                        instanceof ImageSearchFragment)) {
                    // Add ImageSearchFragment
                    addImageSearchFragment();
                    if (mCurrentQueryString == null || mCurrentQueryString.isEmpty()) {
                        // Don't search for empty query strings
                        return;
                    }
                    // Prepare SearchQuery object to do image search
                    SearchQuery searchQuery = new SearchQuery(mCurrentQueryString, true, false, true);
                    loadSearchResult(searchQuery);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (mCurrentQueryString != null && mCurrentQueryString.equals(query.trim())) {
            // Don't search is previous query string is same as current query string
            return false;
        }
        // Update the current query string
        mCurrentQueryString = query.trim();
        if (mCurrentQueryString == null || mCurrentQueryString.isEmpty()) {
            // Don't search for empty query strings
            return false;
        }
        // Prepare SearchQuery object to initiate searching.
        // 'Image' mode will be set depending on the currently visible 'mSearchFragmentView'
        SearchQuery searchQuery = new SearchQuery(mCurrentQueryString, true, false);
        loadSearchResult(searchQuery);

        mSearchView.clearFocus();

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Searching while query text change is not supported currently
        return false;
    }

    /*
    * mSearchFragmentView (currently visible fragment - SearchContract.View) can use this method
    * to get current query string (mCurrentQueryString)
    */
    @Override
    public String getQueryString() {
        return mCurrentQueryString;
    }

    /*
    * load search result by setting query string to the
    * currently visible fragment (SearchContract.View)
    */
    private void loadSearchResult(final SearchQuery searchQuery) {
        if (mSearchView != null) {
            // Use post to make sure that the view is attached to the window before searching begins
            mSearchView.post(new Runnable() {
                @Override
                public void run() {
                    if (mSearchFragmentView != null) {
                        mSearchFragmentView.setQueryString(searchQuery);
                    }
                }
            });
        }
    }

    /*
    * Add ImageSearchFragment. Correct 'Image Mode' is set by newInstance method of the fragment
    */
    private void addImageSearchFragment() {
        mSearchFragmentView = ImageSearchFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.search_fragment_container, (ImageSearchFragment) mSearchFragmentView);
        transaction.commit();
    }

    /*
    * Add WebSearchFragment. Correct 'Image Mode' is set by newInstance method of the fragment
    */
    private void addWebSearchFragment() {
        mSearchFragmentView = WebSearchFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.search_fragment_container, (WebSearchFragment) mSearchFragmentView);
        transaction.commit();
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
