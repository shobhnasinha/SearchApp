package com.shobhna.searchapp.search;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shobhna.searchapp.R;
import com.shobhna.searchapp.model.SearchQuery;
import com.google.api.services.customsearch.model.Result;

import java.util.ArrayList;
import java.util.List;

/*
* Subclass of BaseSearchFragment which implements SearchContract.View.
* It gets its data from Presenter.
* It is responsible for displaying 'Web Result' (non image) search results
* It sets image mode as false in its newInstance method
*/
public class WebSearchFragment extends BaseSearchFragment {

    private WebSearchAdapter mListAdapter;

    private static final String TAG = "WebSearchFragment";

    /*
    * It sets image mode as false in its newInstance method
    */
    public static WebSearchFragment newInstance() {
        WebSearchFragment webSearchFragment = new WebSearchFragment();
        webSearchFragment.setIsImageSearchMode(false);
        return webSearchFragment;
    }

    public WebSearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new WebSearchAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // The super method inflates layout and initiates 'Next' and 'Previous' search page UI
        View root = super.onCreateView(inflater, container, savedInstanceState);

        if (root != null) {
            // Use RecyclerView to display search results
            mRecyclerView = (RecyclerView) root.findViewById(R.id.web_search_list);
            mRecyclerView.setAdapter(mListAdapter);
            // Set Fixed Size to true for optimization
            mRecyclerView.setHasFixedSize(true);
            // Linear Layout Manager for Web Search Result
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            // Initially make RecyclerView Gone to show empty page icon
            mRecyclerView.setVisibility(View.GONE);
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Prepare SearchQuery object to do web search
        SearchQuery searchQuery = new SearchQuery(mQueryString, true, false, false);
        setQueryString(searchQuery);
    }

    @Override
    public void setResultItems(SearchQuery searchQuery, List<Result> resultList) {
        if (resultList != null) {
            if (resultList.size() > 0) {
                // Make RecyclerView visible to display search results
                mRecyclerView.setVisibility(View.VISIBLE);
                // Set Adapter with search results
                mListAdapter.setResultList(resultList);
                // Set next and previous page setting by calling super class method
                super.setResultItems(searchQuery, resultList);
            }

        }
    }

    private static class WebSearchAdapter extends RecyclerView.Adapter<WebSearchAdapter.ViewHolder> {

        private Context mContext;

        private List<Result> mSearchResultList;

        public WebSearchAdapter(Context context) {
            mContext = context;
            mSearchResultList = new ArrayList<>(10);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View searchItemView = inflater.inflate(R.layout.web_search_item, parent, false);
            return new ViewHolder(searchItemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Result result = mSearchResultList.get(position);
            viewHolder.title.setText(result.getTitle());
            viewHolder.link.setText(result.getDisplayLink());
            viewHolder.snippet.setText(result.getSnippet());
        }

        @Override
        public int getItemCount() {
            return mSearchResultList.size();
        }

        public void setResultList(List<Result> results) {
            mSearchResultList = results;
            notifyDataSetChanged();
        }

        public Result getItem(int position) {
            return mSearchResultList.get(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TextView title;

            public TextView link;

            public TextView snippet;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title_web_search_item);
                link = (TextView) itemView.findViewById(R.id.link_web_search_item);
                snippet = (TextView) itemView.findViewById(R.id.snippet_web_search_item);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                Result result = getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(result.getLink()));
                mContext.startActivity(intent);
            }
        }
    }

}
