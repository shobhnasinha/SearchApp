package com.shobhna.searchapp.search;

import com.shobhna.searchapp.model.SearchQuery;
import com.google.api.services.customsearch.model.Result;

import java.util.List;

/*
* MVP Architecture is used for implementing Search list.
* This Contract specifies different components of the architecture.
*/
public interface SearchContract {

    interface SearchModeListener {

        String getQueryString();
    }

    interface View {

        /*
        * Query string to search on Google for
        */
        void setQueryString(SearchQuery searchQuery);

        /*
        * Set whether the search type is Image
        */
        void setIsImageSearchMode(boolean withImage);

        /*
        * Show Progress Indicator indicating background fetching of information
        */
        void setProgressIndicator(boolean active, int msgId);

        /*
        * Set search results to be displayed as search query result
        */
        void setResultItems(SearchQuery searchQuery, List<Result> resultList);
    }

    interface Presenter {

        /*
        * load search results from Google Custom Search Engine
        */
        void loadSearchResults(SearchQuery searchQuery);
    }
}
