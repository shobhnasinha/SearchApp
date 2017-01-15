package com.shobhna.searchapp.model;

import com.google.api.services.customsearch.model.Result;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.verify;

public class InMemorySearchRepository {

    private static SearchQuery IMAGE_SEARCH_QUERY = new SearchQuery("Batman", true, false, true);

    private static SearchQuery WEB_SEARCH_QUERY = new SearchQuery("Batman", true, false, false);

    @Mock
    private SearchServiceApi mSearchServiceApi;

    @Mock
    private List<Result> mResults;

    @Mock
    private SearchRepository.LoadSearchResultsCallback mSearchRepoCallback;

    @Captor
    private ArgumentCaptor<SearchRepository.LoadSearchResultsCallback> mSearchRepoCallbackCaptor;

    @Captor
    private ArgumentCaptor<SearchServiceApi.LoadSearchResultsCallback> mSearchServiceCallbackCaptor;

    @Captor
    private ArgumentCaptor<SearchQuery> mSearchQuery;

    private SearchRepositoryImpl mSearchRepoImpl;

    @Before
    public void setupCityWeatherRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mSearchRepoImpl = new SearchRepositoryImpl(mSearchServiceApi);
    }

    @Test
    public void getImageSearchResults() {
        mSearchRepoImpl.getSearchResults(IMAGE_SEARCH_QUERY, mSearchRepoCallback);

        // Callback is captured and invoked with stubbed results
        verify(mSearchServiceApi).getSearchResults(mSearchQuery.capture(), mSearchServiceCallbackCaptor.capture());
        mSearchServiceCallbackCaptor.getValue().onResultsLoaded(mResults);

        verify(mSearchRepoCallback).onResultsLoaded(mResults);
    }

    @Test
    public void getWebSearchResults() {
        mSearchRepoImpl.getSearchResults(WEB_SEARCH_QUERY, mSearchRepoCallback);

        // Callback is captured and invoked with stubbed results
        verify(mSearchServiceApi).getSearchResults(mSearchQuery.capture(), mSearchServiceCallbackCaptor.capture());
        mSearchServiceCallbackCaptor.getValue().onResultsLoaded(mResults);

        verify(mSearchRepoCallback).onResultsLoaded(mResults);
    }
}
