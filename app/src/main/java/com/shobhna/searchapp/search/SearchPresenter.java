package com.shobhna.searchapp.search;

import android.support.annotation.NonNull;

import com.google.api.services.customsearch.model.Result;
import com.shobhna.searchapp.R;
import com.shobhna.searchapp.model.SearchQuery;
import com.shobhna.searchapp.model.SearchRepository;

import java.util.List;

/*
* MVP Architecture is used for implementing Search list.
* This class forms the Presenter component of the architecture.
* It implements SearchContract.ActionsListener.
*
* The presenter sits between the model and view:
* it coordinates the UI with the data, ensuring they are in sync.
* Specifically, it updates the view and acts upon user events that are forwarded by the view.
* The presenter also retrieves data from the model, prepares it for display (by the view)
* and updates the model as necessary.
*/
public class SearchPresenter implements SearchContract.Presenter {

    private final SearchRepository mSearchListRepository;

    private final SearchContract.View mSearchListView;

    public SearchPresenter(@NonNull SearchRepository searchListRepository, @NonNull SearchContract.View searchListView) {
        mSearchListRepository = searchListRepository;
        mSearchListView = searchListView;
    }

    /*
    * load search results from Google Custom Search Engine
    */
    @Override
    public void loadSearchResults(final SearchQuery searchQuery) {
        if (searchQuery.isWithImage()) {
            mSearchListView.setProgressIndicator(true, R.string.load_image_results);
        } else {
            mSearchListView.setProgressIndicator(true, R.string.load_web_results);
        }
        mSearchListRepository.getSearchResults(searchQuery, new SearchRepository.LoadSearchResultsCallback() {
            @Override
            public void onResultsLoaded(List<Result> items) {
                mSearchListView.setResultItems(searchQuery, items);
                mSearchListView.setProgressIndicator(false, -1);
            }
        });
    }
}
