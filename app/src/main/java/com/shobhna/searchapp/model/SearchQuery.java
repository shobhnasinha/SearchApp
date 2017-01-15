package com.shobhna.searchapp.model;

public class SearchQuery {

    private String mQuery;

    private boolean mNewSearch;

    private boolean mNextPageQuery;

    private boolean mWithImage;

    private long mStartIndex;

    public SearchQuery(String query, boolean newSearch, boolean nextPage) {
        mQuery = query;
        mNewSearch = newSearch;
        mNextPageQuery = nextPage;
    }

    public SearchQuery(String query, boolean newSearch, boolean nextPage, boolean withImage) {
        mQuery = query;
        mNewSearch = newSearch;
        mNextPageQuery = nextPage;
        mWithImage = withImage;
    }

    public SearchQuery(String query, boolean newSearch, boolean nextPage, boolean withImage, long startIndex) {
        mQuery = query;
        mNewSearch = newSearch;
        mNextPageQuery = nextPage;
        mWithImage = withImage;
        mStartIndex = startIndex;
    }

    public SearchQuery() {

    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        this.mQuery = query;
    }

    public long getStartIndex() {
        return mStartIndex;
    }

    public void setStartIndex(long startIndex) {
        this.mStartIndex = startIndex;
    }

    public boolean isWithImage() {
        return mWithImage;
    }

    public void setWithImage(boolean withImage) {
        this.mWithImage = withImage;
    }

    public boolean isNextPageQuery() {
        return mNextPageQuery;
    }

    public void setNextPageQuery(boolean nextPageQuery) {
        this.mNextPageQuery = nextPageQuery;
    }

    public boolean isNewSearch() {
        return mNewSearch;
    }

    public void setNewSearch(boolean newSearch) {
        this.mNewSearch = newSearch;
    }

}
