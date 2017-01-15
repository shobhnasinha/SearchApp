package com.shobhna.searchapp.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.shobhna.searchapp.model.SearchQuery;
import com.shobhna.searchapp.model.TasksContract;
import com.shobhna.searchapp.util.Utils;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import java.io.IOException;
import java.util.List;

public class CustomSearchTask extends AsyncTask<SearchQuery, Void, List<Result>> {

    private TasksContract.LoadSearchResultsCallback mCallback;

    private static final String TAG = "CustomSearchTask";

    public CustomSearchTask(TasksContract.LoadSearchResultsCallback callback) {
        mCallback = callback;
    }

    @Override
    protected List<Result> doInBackground(SearchQuery... params) {
        SearchQuery searchQuery = params[0];
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();
        Customsearch customsearch = new Customsearch(transport, jsonFactory, null);
        List<Result> listResult = null;
        try {
            Customsearch.Cse.List list = customsearch.cse().list(searchQuery.getQuery());
            list.setCx(Utils.CX);
            list.setKey(Utils.API_KEY);
            if (searchQuery.isWithImage()) {
                list.setSearchType("image");
            }
            list.setStart(searchQuery.getStartIndex());
            Search searchResult = list.execute();
            listResult = (List) searchResult.getItems();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException");
        }
        return listResult;
    }

    @Override
    protected void onPostExecute(List<Result> results) {
        mCallback.onResultsLoaded(results);
    }
}
