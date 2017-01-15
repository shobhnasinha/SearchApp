package com.shobhna.searchapp.search;

import com.shobhna.searchapp.model.SearchListRepository;
import com.shobhna.searchapp.model.SearchRepository;
import com.shobhna.searchapp.model.SearchServiceApiImpl;

public class Injection {

    public static SearchRepository provideSearchRepository() {
        return SearchListRepository.getProdRepoInstance(new SearchServiceApiImpl());
    }

}