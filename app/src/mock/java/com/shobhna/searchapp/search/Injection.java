package com.shobhna.searchapp.search;

import com.shobhna.searchapp.model.FakeSearchServiceApiImpl;
import com.shobhna.searchapp.model.SearchListRepository;
import com.shobhna.searchapp.model.SearchRepository;

public class Injection {

    public static SearchRepository provideSearchRepository() {
        return SearchListRepository.getProdRepoInstance(new FakeSearchServiceApiImpl());
    }

}
