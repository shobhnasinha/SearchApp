package com.shobhna.searchapp.model;

import android.support.annotation.NonNull;

import com.shobhna.searchapp.tasks.CustomSearchTask;
import com.google.api.services.customsearch.model.Result;

import java.util.List;

public class SearchServiceApiImpl implements SearchServiceApi {

    /*
    * load search results matching the query string
    */
    @Override
    public void getSearchResults(SearchQuery searchQuery, @NonNull final LoadSearchResultsCallback callback) {
        new CustomSearchTask(new TasksContract.LoadSearchResultsCallback() {
            @Override
            public void onResultsLoaded(List<Result> items) {
                callback.onResultsLoaded(items);
            }
        }).execute(searchQuery);
    }
}
