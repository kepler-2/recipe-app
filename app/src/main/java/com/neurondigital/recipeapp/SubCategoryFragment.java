package com.neurondigital.recipeapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by melvin on 08/09/2016.
 * <p>
 * Shows a list of the favorite Recipes. The favorite recipes are stored by id locally in preferences.
 * The content is however obtained from server.
 */
public class SubCategoryFragment extends Fragment {
    Context context;

    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    RelativeLayout empty;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_wth_swapable, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.recipesList);
        empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();

        //set title
        ((MainActivity)context).setTitle(getArguments().getString(Params.CATEGORY_NAME));

        //set RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manage
        mLayoutManager = new LinearLayoutManager(context);

        mRecyclerView.setLayoutManager(mLayoutManager);

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
    }

    /**
     * Refresh Favorite Recipes.
     */
    public void refresh() {
        SubCategory.loadSubCategories(getArguments().getString(Params.SUB_CATEGORY_JSON_STRING), new SubCategory.onSubCategoriesDownloadedListener() {
            @Override
            public void onSubCategoriesDownloaded(List<SubCategory> subCategories) {
                swipeLayout.setRefreshing(false);
                setSubCategories(subCategories);
            }
        });
    }


    /**
     * Show recipes on screen after refresh
     *
     * @param subCategories
     */
    public void setSubCategories(final List<SubCategory> subCategories) {
        mAdapter = new SubCategoryAdapter(subCategories, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                b.putInt(Params.CATEGORY_ID, subCategories.get(i).category);
                b.putInt(Params.SUB_CATEGORY_ID, subCategories.get(i).id);
                b.putInt(Params.CATEGORY_NAME, getArguments().getInt(Params.CATEGORY_NAME));
                Fragment f = new RecipesFragment();
                f.setArguments(b);
                ((MainActivity)context).changeFragment(f, true);
            }
        }, context);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }

}
