package com.shobhna.searchapp.model;

import com.google.api.services.customsearch.model.Result;

import java.util.List;

public interface TasksContract {

    interface LoadSearchResultsCallback {
        void onResultsLoaded(List<Result> items);
    }
}
