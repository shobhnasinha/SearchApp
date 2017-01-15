package com.shobhna.searchapp.model;

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/*
* Sets correct repository for Prod Build Variant
*/
public class SearchListRepository {

    private SearchListRepository() {
    }

    private static SearchRepository repository = null;

    public synchronized static SearchRepository getProdRepoInstance(@NonNull SearchServiceApi searchListServiceApi) {
        checkNotNull(searchListServiceApi);
        if (null == repository) {
            repository = new SearchRepositoryImpl(searchListServiceApi);
        }
        return repository;
    }
}
