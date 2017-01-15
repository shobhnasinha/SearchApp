package com.shobhna.searchapp.model;

import android.support.annotation.NonNull;

import com.google.api.services.customsearch.model.Result;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class SearchRepositoryImpl implements SearchRepository {

    private final SearchServiceApi mSearchListServiceApi;

    public SearchRepositoryImpl(@NonNull SearchServiceApi searchListServiceApi) {
        mSearchListServiceApi = searchListServiceApi;
    }

    /*
    * load search results matching the query string
    */
    @Override
    public void getSearchResults(SearchQuery searchQuery, @NonNull final LoadSearchResultsCallback callback) {
        checkNotNull(callback);
        mSearchListServiceApi.getSearchResults(searchQuery, new SearchServiceApi.LoadSearchResultsCallback() {
            @Override
            public void onResultsLoaded(List<Result> items) {
                callback.onResultsLoaded(items);
            }
        });
    }
}
