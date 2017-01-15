package com.shobhna.searchapp.model;

import android.support.annotation.NonNull;

import com.google.api.services.customsearch.model.Result;

import java.util.List;

public interface SearchServiceApi {

    /*
    * Callback to be invoked when search results are loaded
    */
    interface LoadSearchResultsCallback {
        void onResultsLoaded(List<Result> items);
    }

    /*
    * load search results matching the query string
    */
    void getSearchResults(SearchQuery searchQuery, @NonNull LoadSearchResultsCallback callback);
}
