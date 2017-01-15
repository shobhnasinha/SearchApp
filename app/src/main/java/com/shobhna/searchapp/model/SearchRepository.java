package com.shobhna.searchapp.model;

import android.support.annotation.NonNull;

import com.google.api.services.customsearch.model.Result;

import java.util.List;

public interface SearchRepository {

    /*
    * Callback to be invoked when search results are loaded
    */
    interface LoadSearchResultsCallback {
        void onResultsLoaded(List<Result> books);
    }

    /*
    * load search results matching the query string
    */
    void getSearchResults(SearchQuery searchQuery, @NonNull LoadSearchResultsCallback callback);
}
