package com.shobhna.searchapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.api.services.customsearch.model.Result;

import java.util.List;

public class FakeSearchServiceApiImpl implements SearchServiceApi {

    private static List<Result> ITEMS;

    @Override
    public void getSearchResults(SearchQuery searchQuery, @NonNull LoadSearchResultsCallback callback) {
        callback.onResultsLoaded(ITEMS);
    }

    @VisibleForTesting
    public static void setResultList(List<Result> items) {
        ITEMS = items;
    }
}
